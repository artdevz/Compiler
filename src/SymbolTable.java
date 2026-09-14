import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {
    
    private final Map<String, Integer> symbols = new LinkedHashMap<>();

    public void Add(String identifier) {
        symbols.put(identifier, symbols.getOrDefault(identifier, 0) + 1);
    }

    public void Print() {
        System.out.println("Tabela de Símbolos:");

        for (Map.Entry<String, Integer> entry : symbols.entrySet()) {
            System.out.printf("%-20s %d%n", entry.getKey(), entry.getValue());
        }
    }

}
