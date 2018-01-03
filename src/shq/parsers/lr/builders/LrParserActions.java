package shq.parsers.lr.builders;

import shq.parsers.grammar.GrammarNotSupportedException;
import shq.parsers.lr.lrparser.LrParserTable;

import java.util.Map;

public class LrParserActions {

    public static void putAction(String fromState, Map<Character, LrParserTable.Action> acMap, char term,
        LrParserTable.Action newAction) throws GrammarNotSupportedException {

        LrParserTable.Action oldAction = acMap.get(term);

        if (oldAction == null)
            acMap.put(term, newAction);
        else if (newAction.equals(oldAction))
            ; // skip
        else {

            System.out.println("Conflicting actions at "
                + GotoSet.gotoString(fromState, term) + ":"
                + "\nOld: " + oldAction
                + "\nNew: " + newAction);

            throw new GrammarNotSupportedException("Can't build LR for this grammar");
        }
    }
}
