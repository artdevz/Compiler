package syntax;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Grammar {
    
    public static final String EPSILON = "ε";
    public static final String EOF = "$";

    private final String startSymbol = "PROG";

    private final Set<String> nonTerminals = new LinkedHashSet<>();

    private final Set<String> terminals = new LinkedHashSet<>();

    private final Map<String, List<List<String>>> productions = new LinkedHashMap<>();

    public Grammar() {
        nonTerminals.addAll(List.of(
            "PROG",
            "CMDS",
            "CMD",
            "DECL",
            "TIPO",
            "ATRIB",
            "IF_STMT",
            "ELSE_STMT",
            "COND",
            "OP_REL",
            "E",
            "E'",
            "T",
            "T'",
            "F"
        ));

        Add("PROG",
                "package", "id", ";",
                "public", "class", "id", "{",
                "public", "static", "void", "id",
                "(", ")", "{", "CMDS", "}", "}"
        );


        Add("CMDS", "CMD", "CMDS");
        Add("CMDS", EPSILON);

        Add("CMD", "DECL");
        Add("CMD", "ATRIB");
        Add("CMD", "IF_STMT");

        Add("DECL", "TIPO", "id", ";");

        Add("TIPO", "int");
        Add("TIPO", "float");
        Add("TIPO", "char");

        Add("ATRIB", "id", "=", "E", ";");

        Add("IF_STMT",
                "if", "(", "COND", ")", "{",
                "CMDS", "}", "ELSE_STMT"
        );

        Add("ELSE_STMT", "else", "{", "CMDS", "}");
        Add("ELSE_STMT", EPSILON);

        Add("COND", "E", "OP_REL", "E");

        Add("OP_REL", "==");
        Add("OP_REL", "!=");
        Add("OP_REL", "<");
        Add("OP_REL", ">");

        Add("E", "T", "E'");

        Add("E'", "+", "T", "E'");
        Add("E'", "-", "T", "E'");
        Add("E'", EPSILON);

        Add("T", "F", "T'");

        Add("T'", "*", "F", "T'");
        Add("T'", "/", "F", "T'");
        Add("T'", EPSILON);

        Add("F", "(", "E", ")");
        Add("F", "id");
        Add("F", "num");

        for (List<List<String>> alternatives : productions.values()) {
            for (List<String> alternative : alternatives) {
                for (String symbol : alternative) {
                    if (!nonTerminals.contains(symbol) && !terminals.contains(symbol)) {
                        terminals.add(symbol);
                    }
                }
            }
        }

        terminals.add(EOF);
    }

    private void Add(String nonTerminal, String... symbols) {
        productions.computeIfAbsent(nonTerminal, key -> new ArrayList<>());

        productions.get(nonTerminal).add(List.of(symbols));
    }

    public String GetStartSymbol() { return startSymbol; }
    public Set<String> GetNonTerminals() { return nonTerminals; }
    public Set<String> GetTerminals() { return terminals; }
    public Map<String, List<List<String>>> GetProductions() { return productions; }

}
