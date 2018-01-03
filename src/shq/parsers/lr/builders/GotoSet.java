package shq.parsers.lr.builders;

import shq.parsers.grammar.Grammars;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class GotoSet<I> {
    private final Map<I, I> items;
    private final String name;

    public GotoSet(Set<I> items, String name) {
        this.items = items.stream()
            .collect(Collectors.toMap(i -> i, i -> i));
        this.name = name;
    }

    public String name() {
        return name;
    }

    public Set<I> items() {
        return items.keySet();
    }

    public I item(I equalItem) { return items.get(equalItem); }

    public boolean addItem(I item) { return items.put(item, item) == null; }

    public boolean removeItem(I item) { return items.remove(item) != null; }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GotoSet set = (GotoSet)o;
        return Objects.equals(items, set.items);
    }

    @Override public int hashCode() {
        return Objects.hash(items);
    }

    @Override public String toString() {
        return "GotoSet{" + name + ": " + Arrays.toString(items.keySet().toArray()) + "}";
    }

    public String prettyPrint() {
        StringBuilder sb = new StringBuilder("==== ");

        sb.append(name()).append(":\n");

        for (I i : items()) {
            sb.append("  ").append(i).append("\n");
        }
        return sb.toString();
    }

    public static String gotoString(String sourceName, char symbol) {
        return "goto(" + sourceName + ", " + Grammars.prettySymbol(symbol) + ")";
    }
}
