
package shq.parsers.lr.builders.slr;

import shq.parsers.lr.builders.GotoSet;
import shq.parsers.lr.builders.LrParserActions;
import shq.parsers.lr.lrparser.LrParserTable;
import shq.parsers.grammar.Rule;
import shq.parsers.grammar.Grammar;
import shq.parsers.grammar.GrammarNotSupportedException;
import shq.parsers.grammar.Grammars;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static shq.parsers.grammar.Grammars.END_MARK;

public class SLR {

    public static LinkedHashSet<SlrItem> closure(Grammar g, Set<SlrItem> sourceItems) {
        LinkedHashSet<SlrItem> result = new LinkedHashSet<>();

        Set<SlrItem> itemsToProcess = sourceItems;

        for (; ; ) {
            Set<SlrItem> newItems = new LinkedHashSet<>();

            for (SlrItem i : itemsToProcess) {

                if (i.isMarkAtEnd() || Grammars.isTerm(i.prodAfterMark()))
                    continue;

                for (Rule r : g.rules()) {

                    if (r.nonterm() != i.prodAfterMark())
                        continue;

                    SlrItem newItem = new SlrItem(r, 0);

                    if (!result.contains(newItem) && !itemsToProcess.contains(newItem))
                        newItems.add(newItem);
                }
            }

            result.addAll(itemsToProcess);

            if (newItems.isEmpty())
                break;

            itemsToProcess = newItems;
        }

        return result;
    }

    public static SlrGotoSet gotoSet(Grammar g, SlrGotoSet sourceItems, char symbol, String name) {
        LinkedHashSet<SlrItem> result = new LinkedHashSet<>();

        for (SlrItem i : sourceItems.items()) {
            if (!i.isMarkAtEnd() && i.prodAfterMark() == symbol)
                result.add(i.withMarkMovedRight());
        }

        return new SlrGotoSet(closure(g, result), name);
    }

    public static SlrGoto buildGotoTable(Grammar g) {

        Map<SlrGotoSet, SlrGotoSet> result = new LinkedHashMap<>();
        Map<SlrGotoSet, SlrGotoSet> setsToProcess = new LinkedHashMap<>();

        Map<String, Map<Character, String>> transitions = new LinkedHashMap<>();

        int curIdx = 0;

        SlrGotoSet initialGotoSet = new SlrGotoSet(
            closure(g, Collections.singleton(new SlrItem(g.getAugmentedMainRule(), 0))),
            "I" + curIdx);

        ++curIdx;

        System.out.println("initial set: " + initialGotoSet);

        setsToProcess.put(initialGotoSet, initialGotoSet);

        for (; ; ) {
            Map<SlrGotoSet, SlrGotoSet> newSets = new LinkedHashMap<>();

            for (SlrGotoSet sourceSet : setsToProcess.keySet()) {

                for (char symbol : g.symbols()) {

                    SlrGotoSet newGotoSet = gotoSet(g, sourceSet, symbol, "I" + curIdx);

                    if (newGotoSet.items().isEmpty())
                        continue;

                    Map<Character, String> m = transitions.computeIfAbsent(sourceSet.name(), k -> new LinkedHashMap<>());

                    SlrGotoSet existingSet;

                    if ((existingSet = result.get(newGotoSet)) != null
                        || (existingSet = setsToProcess.get(newGotoSet)) != null) {

                        System.out.println("transition: " + GotoSet.gotoString(sourceSet.name(), symbol) + " => " + existingSet);

                        m.put(symbol, existingSet.name());
                    }
                    else {
                        System.out.println("new set: " + GotoSet.gotoString(sourceSet.name(), symbol) + " => " + newGotoSet);

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

        return new SlrGoto(result.keySet(), transitions);
    }

    public static LrParserTable buildLrParserTable(Grammar g, SlrGoto gotoSets) throws GrammarNotSupportedException {
        Map<String, Map<Character, LrParserTable.Action>> actions = new LinkedHashMap<>();

        for (SlrGotoSet s : gotoSets.sets())
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

        for (SlrGotoSet gotoSet : gotoSets.sets()) {

            String fromState = gotoSet.name();

            Map<Character, String> transitions = gotoSets.transitions().get(fromState);
            Map<Character, LrParserTable.Action> acMap = actions.get(fromState);

            for (SlrItem item : gotoSet.items()) {

                char curTerm;
                if (transitions != null && !item.isMarkAtEnd() && Grammars.isTerm((curTerm = item.prodAfterMark()))) {
                    String toState = transitions.get(curTerm);

                    if (toState != null)
                        LrParserActions.putAction(fromState, acMap, curTerm, new LrParserTable.ShiftAction(toState,
                            "from " + fromState + " for " + item));
                }

                if (!item.isMarkAtEnd())
                    continue;

                if (item.rule().name().equals(g.getAugmentedMainRule().name())) {
                    LrParserActions.putAction(fromState, acMap, END_MARK, new LrParserTable.AcceptAction(item.toString()));
                    continue;
                }

                for (Character f : g.followSet(item.rule().nonterm()))
                    LrParserActions.putAction(fromState, acMap, f, new LrParserTable.ReduceAction(item.rule().name(), item.toString()));
            }
        }

        return new LrParserTable(g, actions);
    }
}