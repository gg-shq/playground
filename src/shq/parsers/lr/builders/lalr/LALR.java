package shq.parsers.lr.builders.lalr;

import shq.etc.Util;
import shq.parsers.grammar.Grammar;
import shq.parsers.grammar.GrammarNotSupportedException;
import shq.parsers.grammar.Grammars;
import shq.parsers.lr.builders.LrParserActions;
import shq.parsers.lr.builders.clr.CLR;
import shq.parsers.lr.builders.clr.ClrGoto;
import shq.parsers.lr.builders.clr.ClrGotoSet;
import shq.parsers.lr.builders.clr.ClrItem;
import shq.parsers.lr.builders.slr.SlrItem;
import shq.parsers.lr.lrparser.LrParserTable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static shq.etc.Util.putToMapAndThrowIfNotSame;
import static shq.parsers.grammar.Grammars.END_MARK;

public class LALR {

    private static class LalrKernel {
        private LalrGotoSet gotoSet;
        private final Set<String> names;
        private final Map<Character, String> transitions;

        public LalrKernel(LalrGotoSet gotoSet, Set<String> names,
            Map<Character, String> transitions) {
            this.gotoSet = gotoSet;
            this.names = names;
            this.transitions = transitions;
        }

        public LalrGotoSet gotoSet() {
            return gotoSet;
        }

        public Set<String> names() {
            return names;
        }

        public String joinedSetName() {
            return String.join("+", names());
        }

        public Map<Character, String> transitions() {
            return transitions;
        }

        @Override public String toString() {
            return names.toString();
        }
    }

    public static LalrGoto buildGotoTable(Grammar g) {

        ClrGoto clrGoto = CLR.buildGotoTable(g);

        Map<LalrGotoSet, LalrKernel> kernelSets = new LinkedHashMap<>();
        Map<String, LalrKernel> oldToNewMap = new LinkedHashMap<>();

        for (ClrGotoSet set : clrGoto.sets()) {

            LalrGotoSet newKernelSet = clrToLalrGotoSet(set);

            Map<Character, String> clrTransitions = clrGoto.transitions().get(set.name());

            if (kernelSets.containsKey(newKernelSet)) {

                kernelSets.get(newKernelSet).names().add(set.name());

                LalrKernel toKernel = kernelSets.get(newKernelSet);

                joinLalrGotoSets(toKernel, newKernelSet);
                joinLalrTransitions(toKernel.transitions(), newKernelSet, clrTransitions);

                oldToNewMap.put(set.name(), toKernel);
            }
            else {
                Map<Character, String> lalrTransitions = new LinkedHashMap<>();

                joinLalrTransitions(lalrTransitions, newKernelSet, clrTransitions);

                LalrKernel newKernel = new LalrKernel(newKernelSet,
                    Util.modifiableSingletonSet(set.name(), LinkedHashSet::new), lalrTransitions);

                kernelSets.put(newKernelSet, newKernel);

                oldToNewMap.put(set.name(), newKernel);
            }
        }

        Map<String, Map<Character, String>> lalrTransitions = new LinkedHashMap<>();
        Set<LalrGotoSet> lalrGotoSets = new LinkedHashSet<>();

        for (LalrKernel kernel : kernelSets.values()) {

            LinkedHashMap<Character, String> thisKernelTrans = new LinkedHashMap<>();

            for (Map.Entry<Character, String> trans : kernel.transitions().entrySet())
                putToMapAndThrowIfNotSame(thisKernelTrans, trans.getKey(), oldToNewMap.get(trans.getValue()).joinedSetName());

            for (String clrSetName : kernel.names()) {
                Map<Character, String> clrTransitions = clrGoto.transitions().get(clrSetName);
                if (clrTransitions == null)
                    continue;

                for (Map.Entry<Character, String> clrTran : clrTransitions.entrySet()) {
                    if (!Grammars.isNonTerm(clrTran.getKey()))
                        continue;

                    putToMapAndThrowIfNotSame(thisKernelTrans, clrTran.getKey(), oldToNewMap.get(clrTran.getValue()).joinedSetName());
                }
            }

            putToMapAndThrowIfNotSame(lalrTransitions, kernel.joinedSetName(), thisKernelTrans);
            lalrGotoSets.add(new LalrGotoSet(kernel.gotoSet().items(), kernel.joinedSetName()));
        }

        return new LalrGoto(lalrGotoSets, lalrTransitions);
    }


    private static LalrGotoSet clrToLalrGotoSet(ClrGotoSet set) {
        Map<SlrItem, Set<Character>> slrFollowSets = new LinkedHashMap<>();

        for (ClrItem clrItem : set.items()) {
            slrFollowSets.merge(clrItem.item(), Util.modifiableSingletonSet(clrItem.followTerm(), LinkedHashSet::new),
                (oldVal, newVal) -> { oldVal.addAll(newVal); return oldVal; });
        }

        LinkedHashSet<LalrItem> lalrItems = slrFollowSets.entrySet().stream()
            .map(e -> new LalrItem(e.getKey(), e.getValue()))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        return new LalrGotoSet(lalrItems, set.name());
    }

    private static void joinLalrGotoSets(LalrKernel toKernel, LalrGotoSet fromSet) {

        LalrGotoSet toSet = toKernel.gotoSet();

        for (LalrItem fromItem : fromSet.items()) {

            LalrItem toItem = toSet.item(fromItem);

            if (toItem == null)
                toSet.items().add(fromItem);
            else {
                LalrItem joinedItem = new LalrItem(toItem.item(),
                    Stream.concat(
                        toItem.followTerms().stream(),
                        fromItem.followTerms().stream()).collect(Collectors.toCollection(LinkedHashSet::new)));

                toSet.removeItem(toItem);
                toSet.addItem(joinedItem);
            }
        }
    }

    private static void joinLalrTransitions(Map<Character, String> lalrTransitions, LalrGotoSet fromSet,
        Map<Character, String> clrTransitions) {

        if (clrTransitions == null)
            return;

        for (LalrItem fromItem : fromSet.items()) {
            if (!fromItem.item().isMarkAtEnd()) {
                char followTerm = fromItem.item().prodAfterMark();
                lalrTransitions.put(followTerm, clrTransitions.get(followTerm));
            }
        }
    }

    public static LrParserTable buildLrParserTable(Grammar g, LalrGoto gotoSets) throws GrammarNotSupportedException {
        Map<String, Map<Character, LrParserTable.Action>> actions = new LinkedHashMap<>();

        for (LalrGotoSet s : gotoSets.sets())
            actions.put(s.name(), new LinkedHashMap<>());

        for (Map.Entry<String, Map<Character, String>> gotoStateTrans : gotoSets.transitions().entrySet()) {
            String fromState = gotoStateTrans.getKey();

            Map<Character, LrParserTable.Action> trMap = actions.get(fromState);

            for (Map.Entry<Character, String> gt : gotoStateTrans.getValue().entrySet()) {
                Character symbol = gt.getKey();
                String toState = gt.getValue();

                if (Grammars.isNonTerm(symbol))
                    trMap.put(symbol, new LrParserTable.TransitionAction(toState, ""));
            }
        }

        for (LalrGotoSet gotoSet : gotoSets.sets()) {
            String fromState = gotoSet.name();

            Map<Character, LrParserTable.Action> acMap = actions.get(fromState);
            Map<Character, String> srcTranMap = gotoSets.transitions().get(fromState);

            for (LalrItem item : gotoSet.items()) {

                char curTerm;
                if (srcTranMap != null && !item.item().isMarkAtEnd() && Grammars.isTerm((curTerm = item.item().prodAfterMark()))) {
                    String toState = srcTranMap.get(curTerm);

                    if (toState != null)
                        LrParserActions.putAction(fromState, acMap, curTerm, new LrParserTable.ShiftAction(toState, fromState));
                }

                if (!item.item().isMarkAtEnd())
                    continue;

                if (item.item().rule().name().equals(g.getAugmentedMainRule().name())
                    && item.followTerms().equals(Collections.singleton(END_MARK))) {

                    LrParserActions.putAction(fromState, acMap, END_MARK, new LrParserTable.AcceptAction(""));

                    continue;
                }

                LrParserTable.ReduceAction action = new LrParserTable.ReduceAction(item.item().rule().name(), item.toString());
                for (char followTerm : item.followTerms())
                    LrParserActions.putAction(fromState, acMap, followTerm, action);
            }
        }

        return new LrParserTable(g, actions);
    }
}
