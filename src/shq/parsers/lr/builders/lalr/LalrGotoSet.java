package shq.parsers.lr.builders.lalr;

import shq.parsers.lr.builders.GotoSet;

import java.util.Set;

public class LalrGotoSet extends GotoSet<LalrItem> {

    public LalrGotoSet(Set<LalrItem> items, String name) {
        super(items, name);
    }
}
