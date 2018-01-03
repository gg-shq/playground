package shq.parsers.grammar;

import java.util.Objects;

public class Rule {

    private final char nonterm;
    private final String prod;
    private final String name;

    public Rule(char nonterm, String prod, String name) {
        assert !Grammars.isTerm(nonterm);
        this.nonterm = nonterm;
        this.prod = prod;
        this.name = name;
    }

    public static Rule parse(String rule, String name) {
        String[] parts = rule.split(":");
        assert parts.length == 2;
        assert parts[0].length() == 1;
        return new Rule(parts[0].charAt(0), parts[1], name);
    }

    public char nonterm() {
        return nonterm;
    }

    public String prod() {
        return prod;
    }

    public char prod(int pos) {
        assert pos < prod.length();
        return prod.charAt(pos);
    }

    public String name() {
        return name;
    }

    @Override public String toString() {
        return name + "> " + nonterm + ':' + prod;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Rule rule = (Rule)o;
        return Objects.equals(nonterm, rule.nonterm) &&
            Objects.equals(prod, rule.prod);
    }

    @Override public int hashCode() {
        return Objects.hash(nonterm, prod);
    }
}
