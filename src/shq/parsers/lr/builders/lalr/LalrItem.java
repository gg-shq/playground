package shq.parsers.lr.builders.lalr;

import shq.parsers.lr.builders.slr.SlrItem;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class LalrItem {

    private final SlrItem item;
    private final Set<Character> followTerms;

    public LalrItem(SlrItem item, Set<Character> followTerms) {
        this.item = item;
        this.followTerms = followTerms;
    }

    public SlrItem item() {
        return item;
    }

    public Set<Character> followTerms() {
        return followTerms;
    }

    public LalrItem withMarkMovedRight() {
        return new LalrItem(item.withMarkMovedRight(), followTerms);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LalrItem item1 = (LalrItem)o;
        return Objects.equals(item, item1.item);
    }

    @Override public int hashCode() {
        return Objects.hash(item);
    }

    @Override public String toString() {
        return "[" + item + ", " + Arrays.toString(followTerms.toArray()) + "]";
    }
}
