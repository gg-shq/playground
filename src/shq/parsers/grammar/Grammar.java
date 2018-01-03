package shq.parsers.grammar;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Grammar {

    private final Set<Character> terminals;
    private final Set<Character> nonterminals;
    private final List<Rule> rules;
    private final Map<Character, Set<Character>> firstSets;
    private final Map<Character, Set<Character>> followSets;

    public Grammar(Set<Character> terminals, Set<Character> nonterminals,
        List<Rule> rules,
        Map<Character, Set<Character>> firstSets,
        Map<Character, Set<Character>> followSets) {

        this.terminals = terminals;
        this.nonterminals = nonterminals;
        this.rules = rules;
        this.firstSets = firstSets;
        this.followSets = followSets;
    }

    public Set<Character> terminals() {
        return terminals;
    }

    public Set<Character> nonterminals() {
        return nonterminals;
    }

    public Set<Character> symbols() {
        return Stream
            .concat(nonterminals.stream(), terminals.stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<Rule> rules() {
        return rules;
    }

    public Optional<Rule> ruleByName(String name) {
        return rules.stream().filter(r -> r.name().equals(name)).findAny();
    }

    public Rule getAugmentedMainRule() {
        return rules.get(0);
    }

    public Set<Character> firstSet(char symbol) {
        return Collections.unmodifiableSet(firstSets.get(symbol));
    }

    public Set<Character> firstSet(String seq) {
        return FirstAndFollowSets.firstForSeq(seq, firstSets);
    }

    public Set<Character> followSet(char nonterm) {
        return Collections.unmodifiableSet(followSets.get(nonterm));
    }
}
