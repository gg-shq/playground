package shq.parsers.lr.builders.clr;

import shq.parsers.lr.builders.Goto;

import java.util.Map;
import java.util.Set;

public class ClrGoto extends Goto<ClrGotoSet> {

    public ClrGoto(Set<ClrGotoSet> sets,
        Map<String, Map<Character, String>> transitions) {
        super(sets, transitions);
    }
}
