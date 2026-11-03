package syntax;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParsingTable {
    
    private final Grammar grammar;

    private final FirstFollow firstFollow;

    private final Map<String, Map<String, List<String>>> table = new LinkedHashMap<>();

    public ParsingTable(Grammar grammar, FirstFollow firstFollow) {
        this.grammar = grammar;
        this.firstFollow = firstFollow;

        for (String nonTerminal : grammar.GetNonTerminals()) {
            table.put(nonTerminal, new LinkedHashMap<>());
        }

        BuildTable();
    }

    private void BuildTable() {
        for (String nonTerminal : grammar.GetNonTerminals()) {
            for (List<String> production : grammar.GetProductions().get(nonTerminal)) {
                Set<String> firstProduction = firstFollow.FirstOfSequence(production);

                for (String terminal : firstProduction) {
                    if (!terminal.equals(Grammar.EPSILON)) {
                        Insert(nonTerminal, terminal, production);
                    }
                }

                if (firstProduction.contains(Grammar.EPSILON)) {
                    Set<String> followProduction = firstFollow.GetFollowSets().get(nonTerminal);

                    for (String terminal : followProduction) {
                        Insert(nonTerminal, terminal, production);
                    }
                }
            }
        }
    }

    private void Insert(String nonTerminal, String terminal, List<String> production) {
        List<String> previous = table.get(nonTerminal).putIfAbsent(terminal, production);

        if (previous != null && !previous.equals(production)) {
            throw new IllegalStateException("Conflito LL(1) em M[" + nonTerminal + ", " + terminal + "]");
        }
    }

    public List<String> GetProduction(String nonTerminal, String terminal) {
        return table.getOrDefault(nonTerminal, Collections.emptyMap()).get(terminal);
    }

    public void PrintTable() {
        System.out.println("\n========== TABELA M ==========");

        for (String nonTerminal : grammar.GetNonTerminals()) {

            for (String terminal : grammar.GetTerminals()) {

                List<String> production = GetProduction(
                    nonTerminal,
                    terminal
            );

                if (production != null) {

                    System.out.println("M[" + nonTerminal + ", " + terminal + "] = " + nonTerminal + " -> " + String.join(" ", production));
                }
            }
        }
    }

}
