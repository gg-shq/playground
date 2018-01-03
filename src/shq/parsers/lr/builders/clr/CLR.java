package shq.parsers.lr.builders.clr;

import shq.parsers.grammar.Grammar;
import shq.parsers.grammar.GrammarNotSupportedException;
import shq.parsers.grammar.Grammars;
import shq.parsers.grammar.Rule;
import shq.parsers.lr.builders.LrParserActions;
import shq.parsers.lr.lrparser.LrParserTable;
import shq.parsers.lr.builders.slr.SlrItem;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static shq.parsers.grammar.Grammars.END_MARK;

public class CLR {

    public static LinkedHashSet<ClrItem> closure(Grammar g, Set<ClrItem> sourceItems) {
        LinkedHashSet<ClrItem> result = new LinkedHashSet<>();

        Set<ClrItem> itemsToProcess = sourceItems;

        for (;;) {
            Set<ClrItem> newItems = new LinkedHashSet<>();

            for (ClrItem i : itemsToProcess) {

                if (i.item().isMarkAtEnd() || Grammars.isTerm(i.item().prodAfterMark()))
                    continue;

                for (Rule r : g.rules()) {

                    if (r.nonterm() != i.item().prodAfterMark())
                        continue;

                    Set<Character> firstSet = g.firstSet(i.item().seqAfterMark(1) + i.followTerm());

                    for (char term : firstSet) {

                        ClrItem newItem = new ClrItem(new SlrItem(r, 0), term);

                        if (!result.contains(newItem) && !itemsToProcess.contains(newItem))
                            newItems.add(newItem);
                    }
                }
            }

            result.addAll(itemsToProcess);

            if (newItems.isEmpty())
                break;

            itemsToProcess = newItems;
        }

        return result;
    }

    public static ClrGotoSet gotoSet(Grammar g, ClrGotoSet sourceItems, char symbol, String name) {
        LinkedHashSet<ClrItem> result = new LinkedHashSet<>();

        for (ClrItem i : sourceItems.items()) {

            if (!i.item().isMarkAtEnd() && i.item().prodAfterMark() == symbol)
                result.add(i.withMarkMovedRight());
        }

        return new ClrGotoSet(closure(g, result), name);
    }

    public static ClrGoto buildGotoTable(Grammar g) {

        Map<ClrGotoSet, ClrGotoSet> result = new LinkedHashMap<>();
        Map<ClrGotoSet, ClrGotoSet> setsToProcess = new LinkedHashMap<>();

        Map<String, Map<Character, String>> transitions = new LinkedHashMap<>();

        int curIdx = 0;

        ClrGotoSet initialGotoSet = new ClrGotoSet(
            closure(g, Collections.singleton(new ClrItem(new SlrItem(g.getAugmentedMainRule(), 0), END_MARK))),
            "I" + curIdx);

        ++curIdx;

        setsToProcess.put(initialGotoSet, initialGotoSet);

        for (; ; ) {
            Map<ClrGotoSet, ClrGotoSet> newSets = new LinkedHashMap<>();

            for (ClrGotoSet sourceSet : setsToProcess.keySet()) {

                for (char symbol : g.symbols()) {

                    ClrGotoSet newGotoSet = gotoSet(g, sourceSet, symbol, "I" + curIdx);

                    if (newGotoSet.items().isEmpty())
                        continue;

                    Map<Character, String> m = transitions.computeIfAbsent(sourceSet.name(), k -> new LinkedHashMap<>());

                    ClrGotoSet existingSet;

                    if ((existingSet = result.get(newGotoSet)) != null
                        || (existingSet = setsToProcess.get(newGotoSet)) != null
                        || (existingSet = newSets.get(newGotoSet)) != null) {

                        System.out.println("transition: " + newGotoSet.gotoString(sourceSet.name(), symbol) + " => " + existingSet);

                        m.put(symbol, existingSet.name());
                    }
                    else {
                        newSets.put(newGotoSet, newGotoSet);
                        m.put(symbol, newGotoSet.name());
                        ++curIdx;
                    }
                }
            }

            result.putAll(setsToProcess);

            if (newSets.isEmpty())
                break;

            setsToProcess = newSets;
        }

        return new ClrGoto(result.keySet(), transitions);
    }

    public static LrParserTable buildLrParserTable(Grammar g, ClrGoto gotoSets) throws GrammarNotSupportedException {
        Map<String, Map<Character, LrParserTable.Action>> actions = new LinkedHashMap<>();

        for (ClrGotoSet s : gotoSets.sets())
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

        for (ClrGotoSet gotoSet : gotoSets.sets()) {
            String fromState = gotoSet.name();

            Map<Character, LrParserTable.Action> acMap = actions.get(fromState);
            Map<Character, String> srcTranMap = gotoSets.transitions().get(fromState);

            for (ClrItem item : gotoSet.items()) {

                char curTerm;
                if (srcTranMap != null && !item.item().isMarkAtEnd() && Grammars.isTerm((curTerm = item.item().prodAfterMark()))) {
                    String toState = srcTranMap.get(curTerm);

                    if (toState != null)
                        LrParserActions.putAction(fromState, acMap, curTerm, new LrParserTable.ShiftAction(toState, fromState));
                }

                if (!item.item().isMarkAtEnd())
                    continue;

                if (item.item().rule().name().equals(g.getAugmentedMainRule().name()) && item.followTerm() == END_MARK) {
                    LrParserActions.putAction(fromState, acMap, END_MARK, new LrParserTable.AcceptAction(""));

                    continue;
                }

                LrParserTable.ReduceAction action = new LrParserTable.ReduceAction(item.item().rule().name(), item.toString());
                LrParserActions.putAction(fromState, acMap, item.followTerm(), action);
            }
        }

        return new LrParserTable(g, actions);
    }
}
