import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import lexer.DoubleBuffer;
import lexer.Lexer;
import lexer.SymbolTable;
import lexer.Token;
import lexer.TokenTable;
import lexer.TokenType;
import syntax.FirstFollow;
import syntax.Grammar;
import syntax.ParserToken;
import syntax.ParsingTable;
import syntax.PredictiveParser;
import syntax.TokenConverter;

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

        // Léxico

        SymbolTable symbolTable = new SymbolTable();
        TokenTable tokenTable = new TokenTable();

        try {
            DoubleBuffer buffer = new DoubleBuffer(file);

            Lexer lexer = new Lexer(buffer, symbolTable, tokenTable);

            lexer.Analyze();

            symbolTable.Print();
            tokenTable.Print();
        }
        catch (IOException e) {
            System.out.println("[Erro]: Não foi possível ler o arquivo: " + args[0]);
            return;
        }

        // Conversor de Tokens

        List<ParserToken> parserTokens = new ArrayList<>();
        for (Token token : tokenTable.GetTokens()) {
            if (token.GetType() == TokenType.ERROR) {
                System.out.println("[Erro]: Token inválido: " + token.GetLexeme());
                return;
            }
            
            if (token.GetType() == TokenType.EOF) continue;

            ParserToken parserToken = TokenConverter.Convert(token);
            parserTokens.add(parserToken);
        }

        // Sintático

        Grammar grammar = new Grammar();
        FirstFollow firstFollow = new FirstFollow(grammar);
        ParsingTable parsingTable = new ParsingTable(grammar, firstFollow);

        firstFollow.PrintFirstFollow();
        parsingTable.PrintTable();

        PredictiveParser parser = new PredictiveParser(grammar, parsingTable);
        parser.Parse(parserTokens);
    }
}