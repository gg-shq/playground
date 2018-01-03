package shq.parsers.lr.lrparser;

import shq.parsers.grammar.Grammar;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LrParserTable {

    public abstract static class Action {
        protected final String info;

        public Action(String info) {
            this.info = info;
        }

        public String info() {
            return info;
        }
    }

    public static class ShiftAction extends Action {

        private final String newState;

        public ShiftAction(String newState, String info) {
            super(info);
            this.newState = newState;
        }

        public String newState() {
            return newState;
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ShiftAction action = (ShiftAction)o;
            return Objects.equals(newState, action.newState);
        }

        @Override public int hashCode() {

            return Objects.hash(newState);
        }

        @Override public String toString() {
            return "s" + newState + (info.isEmpty() ? "" : ": " + info);
        }
    }

    public static class ReduceAction extends Action {
        private final String ruleName;

        public ReduceAction(String ruleName, String info) {
            super(info);
            this.ruleName = ruleName;
        }

        public String ruleName() {
            return ruleName;
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ReduceAction action = (ReduceAction)o;
            return Objects.equals(ruleName, action.ruleName);
        }

        @Override public int hashCode() {
            return Objects.hash(ruleName);
        }

        @Override public String toString() {
            return "r" + ruleName + (info.isEmpty() ? "" : ": " + info);
        }
    }

    public static class AcceptAction extends Action {

        public AcceptAction(String info) {
            super(info);
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            return true;
        }

        @Override public int hashCode() {
            return 1;
        }

        @Override public String toString() {
            return "acc";
        }
    }

    public static class TransitionAction extends Action {
        private final String toState;

        public TransitionAction(String toState, String info) {
            super(info);
            this.toState = toState;
        }

        public String toState() {
            return toState;
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TransitionAction action = (TransitionAction)o;
            return Objects.equals(toState, action.toState);
        }

        @Override public int hashCode() {
            return Objects.hash(toState);
        }

        @Override public String toString() {
            return "t" + toState + (info.isEmpty() ? "" : ": " + info);
        }
    }

    private final Grammar grammar;
    private final Map<String, Map<Character, Action>> actions;

    public LrParserTable(Grammar grammar, Map<String, Map<Character, Action>> actions) {
        this.grammar = grammar;
        this.actions = actions;
    }

    public Grammar grammar() {
        return grammar;
    }

    public Map<String, Map<Character, Action>> actions() {
        return actions;
    }

    public Set<String> states() {
        return actions.keySet();
    }

}