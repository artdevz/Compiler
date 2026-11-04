package syntax;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PredictiveParser {
    
    private final Grammar grammar;

    private final ParsingTable parsingTable;

    private final List<String> errors = new ArrayList<>();

    public PredictiveParser(Grammar grammar, ParsingTable parsingTable) {
        this.grammar = grammar;
        this.parsingTable = parsingTable;
    }

    public boolean Parse(List<ParserToken> tokens) {
        errors.clear();

        List<ParserToken> input = new ArrayList<>(tokens);

        input.add(new ParserToken(Grammar.EOF, Grammar.EOF, -1, -1));

        Deque<String> stack = new ArrayDeque<>();

        stack.push(Grammar.EOF);

        stack.push(grammar.GetStartSymbol());

        int index = 0;
        int step = 1;

        PrintHeader();

        while (!stack.isEmpty()) {
            String top = stack.peek();

            ParserToken currentToken = input.get(index);
            String currentTerminal = currentToken.terminal();

            String stackString = FormatStack(stack);
            String inputString = FormatInput(input, index);

            // ACEITAÇÃO

            if (top.equals(Grammar.EOF) && currentTerminal.equals(Grammar.EOF)) {
                PrintStep(step++, stackString, inputString, "Aceita: $ = $");                

                stack.pop();
    
                break;
            }

            // TERMINAL NO TOPO DA PILHA

            if (!grammar.GetNonTerminals().contains(top)) {
                if (top.equals(currentTerminal)) {
                    PrintStep(step++, stackString, inputString, "Casa terminal: " + top);

                    stack.pop();
                    index++;
                }
                else {
                    String message =
                            "Linha "
                            + currentToken.line()
                            + ": encontrado '"
                            + currentToken.lexeme()
                            + "', esperava-se '"
                            + top + "'.";

                    errors.add(message);

                    PrintStep(step++, stackString, inputString, "[ERRO]: " + message);

                    stack.pop();
                }

                continue;
            }

            // NÃO TERMINAL NO TOPO DA PILHA

            List<String> production = parsingTable.GetProduction(top, currentTerminal);

            if (production != null) {
                PrintStep(
                    step++,
                    stackString,
                    inputString,
                    top + " -> " + String.join(" ", production)
                );

                stack.pop();

                // Epsilon não empilha nada:
                if (!production.equals(List.of(Grammar.EPSILON))) {
                    for (int i = production.size() - 1; i >= 0; i--) {
                        stack.push(production.get(i));
                    }
                }
            }
            else {
                // CELULA VAZIA NA TABELA M
                
                Set<String> expected = new LinkedHashSet<>();

                for (String terminal : grammar.GetTerminals()) {
                    if (parsingTable.GetProduction(top, terminal) != null) {
                        expected.add(terminal);
                    }
                }

                String message = 
                    "Linha "
                    + currentToken.line()
                    + ": token inesperado '"
                    + currentToken.lexeme()
                    + "' ao analisar "
                    + top
                    + ". Esperado: "
                    + expected;

                errors.add(message);

                PrintStep(step++, stackString, inputString, "[ERRO]: " + message);

                // Encerra a análise sintática ao encontrar um erro de análise.
                break;
            }
        }

        // RESULTADO

        System.out.println("\n========== RESULTADO ==========");

        if (errors.isEmpty()) {
            System.out.println("Programa sintaticamente valido!");

            return true;
        } 
        else {
            System.out.println("Programa sintaticamente invalido!");

            for (String error : errors) {
                System.out.println(error);
            }

            return false;
        }
    }

    private void PrintHeader() {
        System.out.printf(
                "%-5s | %-45s | %-40s | %s%n",
                "Passo",
                "Pilha",
                "Entrada",
                "Acao"
        );
    }

    private void PrintStep(
            int step,
            String stack,
            String input,
            String action) {
        System.out.printf(
                "%-5d | %-45s | %-40s | %s%n",
                step,
                stack,
                input,
                action
        );
    }

    private String FormatStack(Deque<String> stack) {
        List<String> symbols = new ArrayList<>(stack);

        Collections.reverse(symbols);

        return String.join(" ", symbols);
    }

    private String FormatInput(List<ParserToken> input, int index) {
        List<String> remaining = new ArrayList<>();

        for (int i = index; i < input.size();i++) {
            remaining.add(input.get(i).terminal());
        }

        return String.join(" ", remaining);
    }

}
