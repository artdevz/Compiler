package syntax;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FirstFollow {
    
    private final Grammar grammar;

    private final Map<String, Set<String>> firstSets = new LinkedHashMap<>();
    private final Map<String, Set<String>> followSets = new LinkedHashMap<>();

    public FirstFollow(Grammar grammar) {
        this.grammar = grammar;

        for (String nonTerminal : grammar.GetNonTerminals()) {
            firstSets.put(nonTerminal, new java.util.LinkedHashSet<>());
            followSets.put(nonTerminal, new java.util.LinkedHashSet<>());
        }

        CalculateFirst();
        CalculateFollow();
    }

    private void CalculateFirst() {
        boolean changed;

        do {
            changed = false;

            for (String nonTerminal : grammar.GetNonTerminals()) {
                List<List<String>> productions = grammar.GetProductions().get(nonTerminal);

                if (productions == null) {
                    throw new IllegalStateException(
                            "Não existem produções para o não terminal: "
                            + nonTerminal
                    );
                }

                for (List<String> production : grammar.GetProductions().get(nonTerminal)) {
                    Set<String> productionFirstSet = FirstOfSequence(production);

                    if (firstSets.get(nonTerminal).addAll(productionFirstSet)) changed = true;
                }
            }
        }
        while (changed);
    }

    public Set<String> FirstOfSequence(List<String> sequence) {
        Set<String> result = new LinkedHashSet<>();

        if (sequence.isEmpty()) {
            result.add(Grammar.EPSILON);
            return result;
        }

        boolean allNullable = true;

        for (String symbol : sequence) {
            Set<String> symbolFirstSet;

            if (symbol.equals(Grammar.EPSILON)) {
                symbolFirstSet = Set.of(Grammar.EPSILON);
            }
            else if (grammar.GetNonTerminals().contains(symbol)) {
                symbolFirstSet = firstSets.get(symbol);
            }
            else {
                symbolFirstSet = Set.of(symbol);
            }

            for (String terminal : symbolFirstSet) {
                if (!terminal.equals(Grammar.EPSILON)) {
                    result.add(terminal);
                }
            }

            if (!symbolFirstSet.contains(Grammar.EPSILON)) {
                allNullable = false;
                break;
            }
        }

        if (allNullable) {
            result.add(Grammar.EPSILON);
        }

        return result;
    }

    private void CalculateFollow() {
        followSets.get(grammar.GetStartSymbol()).add(Grammar.EOF);

        boolean changed;

        do {
            changed = false;

            for (String nonTerminal : grammar.GetNonTerminals()) {
                for (List<String> production : grammar.GetProductions().get(nonTerminal)) {
                    for (int i = 0; i < production.size(); i++) {
                        String symbol = production.get(i);

                        if (!grammar.GetNonTerminals().contains(symbol)) continue;

                        List<String> beta = production.subList(i + 1, production.size());

                        Set<String> firstBeta = FirstOfSequence(beta);

                        for (String terminal : firstBeta) {
                            if (!terminal.equals(Grammar.EPSILON)) {
                                if (followSets.get(symbol).add(terminal)) changed = true;
                            }
                        }

                        if (firstBeta.contains(Grammar.EPSILON)) {
                            if (followSets.get(symbol).addAll(followSets.get(nonTerminal))) changed = true;
                        }
                    }
                }
            }
        }
        while (changed);
    }

    public void PrintFirstFollow() {
        System.out.println("\n========== FIRST ==========");

        for (String nonTerminal
                : grammar.GetNonTerminals()) {

            System.out.println(
                    "FIRST(" + nonTerminal + ") = "
                    + firstSets.get(nonTerminal)
            );
        }

        System.out.println("\n========== FOLLOW ==========");

        for (String nonTerminal
                : grammar.GetNonTerminals()) {

            System.out.println(
                    "FOLLOW(" + nonTerminal + ") = "
                    + followSets.get(nonTerminal)
            );
        }
    }

    public Map<String, Set<String>> GetFirstSets() { return firstSets; }
    public Map<String, Set<String>> GetFollowSets() { return followSets; }

}
