package shq.parsers.grammar;

public class Grammars {

    public static final char END_MARK = '$';

    public static final char EMPTY_MARK = 'ε';

    public static boolean isTerm(char c) {
        return !Character.isUpperCase(c) && !isAuxiliary(c);
    }

    public static boolean isNonTerm(char c) {
        return Character.isUpperCase(c) && !isAuxiliary(c);
    }

    public static boolean isAuxiliary(char c) {
        return isEmptyMark(c) || isEndMark(c);
    }

    public static boolean isEndMark(char c) {
        return c == END_MARK;
    }

    public static boolean isEmptyMark(char c) {
        return c == EMPTY_MARK;
    }

    public static String prettySymbol(char symbol) {
        return isTerm(symbol) ? "'" + symbol + "'" : Character.toString(symbol);
    }
}
