package shq.parsers.lr.builders.slr;

import shq.parsers.lr.builders.Goto;

import java.util.Map;
import java.util.Set;

public class SlrGoto extends Goto<SlrGotoSet> {

    public SlrGoto(Set<SlrGotoSet> sets,
        Map<String, Map<Character, String>> transitions) {
        super(sets, transitions);
    }
}
