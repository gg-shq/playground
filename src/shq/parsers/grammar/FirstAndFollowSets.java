package shq.parsers.grammar;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static shq.parsers.grammar.Grammars.END_MARK;

public class FirstAndFollowSets {

    public static Map<Character, Set<Character>> computeFirstSets(Set<Character> terminals, List<Rule> rules) {

        Map<Character, Set<Character>> firstSets = new LinkedHashMap<>();

        LinkedHashSet<Character> firstSet = new LinkedHashSet<>();
        firstSet.add(END_MARK);
        firstSets.put(END_MARK, firstSet);

        for (char term : terminals) {
            firstSet = new LinkedHashSet<>();
            firstSet.add(term);
            firstSets.put(term, firstSet);
        }

        for (Rule r : rules) {
            if (r.prod().isEmpty()) {
                firstSet = new LinkedHashSet<>();
                firstSet.add(END_MARK);
                firstSets.put(r.nonterm(), firstSet);
            }
        }

        boolean continueProcessing;

        do {
            continueProcessing = false;

            for (Rule r : rules) {
                if (r.prod().isEmpty())
                    continue;

                if (r.prod().chars().anyMatch(p -> !firstSets.containsKey((char) p)))
                    continue;

                Set<Character> firstSetForProd = firstForSeq(r.prod(), firstSets);

                Set<Character> firstSetToUpdate = firstSets.computeIfAbsent(r.nonterm(), k -> new LinkedHashSet<>());

                if (firstSetToUpdate.addAll(firstSetForProd))
                    continueProcessing = true;
            }
        } while (continueProcessing);

        return firstSets;
    }

    public static Map<Character, Set<Character>> computeFollowSets(Map<Character, Set<Character>> firstSets,
        List<Rule> rules) {

        Map<Character, Set<Character>> followSets = new LinkedHashMap<>();

        Set<Character> emptyMarkSet = new LinkedHashSet<>();
        emptyMarkSet.add(END_MARK);

        followSets.put(rules.get(0).nonterm(), emptyMarkSet);

        boolean continueProcessing;

        do {
            continueProcessing = false;

            for (Rule r : rules) {
                Set<Character> ruleFollowSet = followSets.computeIfAbsent((char)r.nonterm(), k -> new LinkedHashSet<>());

                for (int p = 0; p < r.prod().length(); ++p) {
                    if (Grammars.isNonTerm(r.prod(p))) {
                        Set<Character> followSet = followSets.computeIfAbsent(r.prod(p), k -> new LinkedHashSet<>());

                        if (p == r.prod().length() - 1) {
                            if (followSet.addAll(ruleFollowSet))
                                continueProcessing = true;
                        }
                        else {
                            String rightSeq = r.prod().substring(p + 1);
                            Set<Character> firstSet = firstForSeq(rightSeq, firstSets);
                            if (firstSet.contains(END_MARK)) {
                                if (followSet.addAll(ruleFollowSet))
                                    continueProcessing = true;
                            }
                            else
                                if (followSet.addAll(firstSet))
                                    continueProcessing = true;
                        }
                    }
                }
            }
        } while (continueProcessing);

        return followSets;
    }

    public static Set<Character> firstForSeq(String seq, Map<Character, Set<Character>> firstSets) {

        Set<Character> result = new LinkedHashSet<>();

        List<Set<Character>> prodSets = seq.chars()
            .mapToObj(p -> {
                Set<Character> f = firstSets.get((char)p);
                assert f != null;
                return f;
            })
            .collect(Collectors.toList());

        boolean addEmptyMark = true;

        for (Set<Character> firstSet : prodSets) {
            result.addAll(firstSet);

            if (!firstSet.contains(END_MARK)) {
                addEmptyMark = false;
                break;
            }
        }

        if (addEmptyMark)
            result.add(END_MARK);

        return result;
    }
}
