package shq.parsers.lr.builders.clr;

import shq.parsers.lr.builders.slr.SlrItem;

import java.util.Objects;

public class ClrItem {

    private final SlrItem item;
    private final char followTerm;

    public ClrItem(SlrItem item, char followTerm) {
        this.item = item;
        this.followTerm = followTerm;
    }

    public SlrItem item() {
        return item;
    }

    public char followTerm() {
        return followTerm;
    }

    public ClrItem withMarkMovedRight() {
        return new ClrItem(item.withMarkMovedRight(), followTerm);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ClrItem item1 = (ClrItem)o;
        return followTerm == item1.followTerm &&
            Objects.equals(item, item1.item);
    }

    @Override public int hashCode() {
        return Objects.hash(item, followTerm);
    }

    @Override public String toString() {
        return "[" + item + ", " + followTerm + "]";
    }
}
