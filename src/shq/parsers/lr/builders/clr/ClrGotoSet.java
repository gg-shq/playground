package shq.parsers.lr.builders.clr;

import shq.parsers.lr.builders.GotoSet;

import java.util.LinkedHashSet;

public class ClrGotoSet extends GotoSet<ClrItem> {

    public ClrGotoSet(LinkedHashSet<ClrItem> items, String name) {
        super(items, name);
    }
}
