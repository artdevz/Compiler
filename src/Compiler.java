import java.io.IOException;
import java.nio.file.Path;

public class Compiler {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Uso: java Compiler <arquivo.java>");
            return;
        }

        if (!args[0].endsWith(".java")) {
            System.out.println("[Erro]: Arquivo inválido. O arquivo deve ser .java");
            return;
        }

        Path file = Path.of(args[0]);
        if (!file.toFile().exists()) {
            System.out.println("[Erro]: Arquivo não encontrado: " + args[0]);
            return;
        }

        String path = args[0];        
        System.out.println("Compilando: " + path);

        try {
            DoubleBuffer buffer = new DoubleBuffer(file);

            SymbolTable symbolTable = new SymbolTable();
            TokenTable tokenTable = new TokenTable();

            Lexer lexer = new Lexer(buffer, symbolTable, tokenTable);

            lexer.Analyze();

            symbolTable.Print();
            tokenTable.Print();
        }
        catch (IOException e) {
            System.out.println("[Erro]: Não foi possível ler o arquivo: " + args[0]);
            return;
        }
    }
}