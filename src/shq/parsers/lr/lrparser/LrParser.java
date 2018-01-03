package shq.parsers.lr.lrparser;

import shq.parsers.Parser;
import shq.parsers.grammar.Grammars;
import shq.parsers.grammar.Rule;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LrParser extends Parser<LrParser.LrParserState> {


    public static class StateAndSymbol {
        private final char symbol;
        private final String state;

        public StateAndSymbol(char symbol, String state) {
            this.symbol = symbol;
            this.state = state;
        }

        public char symbol() {
            return symbol;
        }

        public String state() {
            return state;
        }

        @Override public String toString() {
            return Grammars.prettySymbol(symbol) + "," + state;
        }
    }

    public static class LrParserState {
        private final LinkedList<StateAndSymbol> stack = new LinkedList<>();
        private final LinkedList<Character> input = new LinkedList<>();

        public LinkedList<StateAndSymbol> stack() {
            return stack;
        }

        public LinkedList<Character> input() {
            return input;
        }

        @Override public String toString() {
            return "stack=" + stack + "; input=" + input;
        }
    }

    private final LrParserTable table;
    private final LrParserState state;
    private final Consumer<LrParserState> errorHandler;
    private final BiConsumer<LrParserState, LrParserTable.Action> tracer;

    public LrParser(LrParserTable table, Consumer<LrParserState> errorHandler,
        BiConsumer<LrParserState, LrParserTable.Action> tracer) {
        this.table = table;
        this.errorHandler = errorHandler;
        this.tracer = tracer;
        state = new LrParserState();
        reset();
    }

    public void reset() {
        state.input.clear();
        state.stack.clear();
        state.stack.add(new StateAndSymbol(Grammars.END_MARK, table.states().iterator().next()));
    }

    public Result push(char term) {
        state.input().addLast(term);

        for (;;) {
            LrParserTable.Action action = table.actions().get(state.stack().getLast().state()).get(term);

            if (tracer != null)
                tracer.accept(state, action);

            if (action == null) {
                if (errorHandler != null)
                    errorHandler.accept(state);

                return Result.ERROR;
            }

            if (action instanceof LrParserTable.AcceptAction)
                return Result.ACCEPTED;

            if (action instanceof LrParserTable.ShiftAction) {
                state.stack().addLast(new StateAndSymbol(term, ((LrParserTable.ShiftAction)action).newState()));
                state.input().removeLast();

                return Result.CONTINUE;
            }

            if (action instanceof LrParserTable.ReduceAction) {
                String ruleName = ((LrParserTable.ReduceAction)action).ruleName();

                Optional<Rule> rule = table.grammar().ruleByName(ruleName);

                assert rule.isPresent();

                assert state.stack().size() > rule.get().prod().length();

                for (int i = 0; i < rule.get().prod().length(); ++i)
                    state.stack().removeLast();

                String topState = state.stack().getLast().state();

                char newNonTerm = rule.get().nonterm();

                LrParserTable.TransitionAction transition = (LrParserTable.TransitionAction)
                    table.actions().get(topState).get(newNonTerm);

                String toState = transition.toState();

                this.state.stack().addLast(new StateAndSymbol(newNonTerm, toState));

                continue;
            }

            assert false : "Unsupported action";
        }
    }
}
