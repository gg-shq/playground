package shq.parsers.grammar;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GrammarBuilder {

    public static Grammar parse(List<String> strRules) {
        List<Rule> rules = new ArrayList<>(strRules.size());
        Set<Character> nonterminals = new LinkedHashSet<>();
        Set<Character> terminals = new LinkedHashSet<>();

        int idx = 0;

        for (String strRule : strRules) {

            Rule r = Rule.parse(strRule, "R" + idx);
            ++idx;

            rules.add(r);

            nonterminals.add(r.nonterm());

            for (char c : r.prod().toCharArray()) {
                if (Grammars.isTerm(c))
                    terminals.add(c);
                else if (Grammars.isNonTerm(c))
                    nonterminals.add(c);
                else
                    assert false : "Symbol " + c + " not allowed";
            }
        }

        for (char nonterm : nonterminals) {
            assert rules.stream().anyMatch(r -> r.nonterm() == nonterm)
                : "Non-terminal " + nonterm + " doesn't have any rules";
        }

        Map<Character, Set<Character>> firstSets = FirstAndFollowSets.computeFirstSets(terminals, rules);
        Map<Character, Set<Character>> followSets = FirstAndFollowSets.computeFollowSets(firstSets, rules);

        return new Grammar(terminals, nonterminals, rules, firstSets, followSets);
    }

}
