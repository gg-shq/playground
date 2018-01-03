package shq.parsers.lr.builders.slr;

import shq.parsers.grammar.Rule;

import java.util.Objects;

public class SlrItem {

    public static final char MARKER = '.';

    public static boolean isMarker(char c) {
        return c == MARKER;
    }

    private final Rule rule;

    private final int markPos;

    public SlrItem(Rule rule, int markPos) {
        this.rule = rule;
        this.markPos = markPos;
    }

    public int markPos() {
        return markPos;
    }

    public Rule rule() {
        return rule;
    }

    public char prodAfterMark() {
        return rule.prod(markPos);
    }

    public char prodBeforeMark() {
        assert ! isMarkAtBeginning();
        return rule.prod(markPos - 1);
    }

    public String seqAfterMark(int shift) {
        return rule.prod().substring(markPos() + shift);
    }

    private boolean isMarkAtBeginning() {
        return markPos == 0;
    }

    public boolean isMarkAtEnd() {
        return markPos == rule.prod().length();
    }

    public SlrItem withMarkMovedRight() {
        assert ! isMarkAtEnd();
        return new SlrItem(rule, markPos + 1);
    }

    @Override public String toString() {
        return ""
            + rule.nonterm()
            + ':'
            + rule.prod().substring(0, markPos)
            + MARKER
            + rule.prod().substring(markPos);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SlrItem item = (SlrItem)o;
        return markPos == item.markPos &&
            Objects.equals(rule, item.rule);
    }

    @Override public int hashCode() {
        return Objects.hash(markPos, rule);
    }
}
