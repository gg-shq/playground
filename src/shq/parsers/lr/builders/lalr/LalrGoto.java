package shq.parsers.lr.builders.lalr;

import shq.parsers.lr.builders.Goto;

import java.util.Map;
import java.util.Set;

public class LalrGoto extends Goto<LalrGotoSet> {

    public LalrGoto(Set<LalrGotoSet> sets,
        Map<String, Map<Character, String>> transitions) {
        super(sets, transitions);
    }
}
