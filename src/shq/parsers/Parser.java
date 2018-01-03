package shq.parsers;

import java.util.function.Supplier;

public abstract class Parser<S> {

    public enum Result {
        CONTINUE, ACCEPTED, ERROR
    }

    public abstract void reset();

    public abstract Result push(char term);

    public Result push(String s) {
        Result r = Result.CONTINUE;

        for (char c : s.toCharArray()) {
            r = push(c);
            if (r != Result.CONTINUE)
                break;
        }

        return r;
    }

    public Result pull(Supplier<Character> lexer) {
        Result r;

        do {
            r = push(lexer.get());
        } while (r == Result.CONTINUE);

        return r;
    }
}
