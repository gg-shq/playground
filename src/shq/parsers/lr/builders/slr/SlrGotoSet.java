package shq.parsers.lr.builders.slr;

import shq.parsers.lr.builders.GotoSet;

import java.util.LinkedHashSet;

public class SlrGotoSet extends GotoSet<SlrItem> {

    public SlrGotoSet(LinkedHashSet<SlrItem> items, String name) {
        super(items, name);
    }
}
