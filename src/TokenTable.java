import java.util.ArrayList;
import java.util.List;

public class TokenTable {
    
    private  final List<Token> tokens = new ArrayList<>();

    public void Add(Token token) { tokens.add(token); }

    public  void Print() {
        System.out.println("Tabela de Tokens:");

        for (Token token : tokens) {
            System.out.printf("%-20s %-20s %s%n", token.GetType(), token.GetLexeme(), token.GetAttribute());
        }
    }

}
