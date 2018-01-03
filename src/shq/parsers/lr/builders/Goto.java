package shq.parsers.lr.builders;

import java.util.Map;
import java.util.Set;

public class Goto<S extends GotoSet> {

    private final Set<S> sets;
    private final Map<String, Map<Character, String>> transitions;

    public Goto(Set<S> sets, Map<String, Map<Character, String>> transitions) {
        this.sets = sets;
        this.transitions = transitions;
    }

    public Set<S> sets() {
        return sets;
    }

    public S set(String name) {
        for (S s : sets) {
            if (s.name().equals(name))
                return s;
        }

        throw new RuntimeException("unknown goto set " + name);
    }

    public Map<String, Map<Character, String>> transitions() {
        return transitions;
    }
}