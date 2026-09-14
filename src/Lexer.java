import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Lexer {

    private final String source;

    private int begin = 0;
    private int forward = 0;

    private final SymbolTable symbolTable;
    private final TokenTable tokenTable;

    private static final Set<String> KEYWORDS = Set.of(
        "package",
        "import",
        "extends",
        "implements",
        "interface",
        "public",
        "private",
        "protected",
        "class",
        "static",
        "final",
        "void",
        "int",
        "float",
        "double",
        "char",
        "boolean",
        "long",
        "short",
        "byte",
        "if",
        "else",
        "while",
        "for",
        "return",
        "true",
        "false",
        "new",
        "this",
        "null"
    );

    public Lexer(String source, SymbolTable symbolTable, TokenTable tokenTable) {
        this.source = source;
        this.symbolTable = symbolTable;
        this.tokenTable = tokenTable;
    }

    public List<Token> Analyze() {
        List<Token> tokens = new ArrayList<>();

        while (forward < source.length()) {

            while (forward < source.length()
                    && Character.isWhitespace(source.charAt(forward))) {
                forward++;
            }

            if (forward >= source.length()) {
                break;
            }

            begin = forward;

            char current = source.charAt(forward);

            /*
             * Comentários
             */
            if (current == '/' && forward + 1 < source.length()) {

                char next = source.charAt(forward + 1);

                if (next == '/') {
                    SkipLineComment();
                    continue;
                }

                if (next == '*') {
                    Token token = SkipBlockComment();

                    if (token != null) {
                        tokens.add(token);
                        tokenTable.Add(token);
                    }

                    continue;
                }
            }

            /*
             * Identificador ou palavra reservada
             */
            if (Character.isLetter(current) || current == '_') {

                Token token = ReadIdentifier();

                tokens.add(token);
                tokenTable.Add(token);
            }

            /*
             * Número
             */
            else if (Character.isDigit(current)) {

                Token token = ReadNumber();

                tokens.add(token);
                tokenTable.Add(token);
            }

            /*
             * Caractere
             */
            else if (current == '\'') {

                Token token = ReadChar();

                tokens.add(token);
                tokenTable.Add(token);
            }

            /*
             * String
             */
            else if (current == '"') {

                Token token = ReadString();

                tokens.add(token);
                tokenTable.Add(token);
            }

            /*
             * Operadores e delimitadores
             */
            else {

                Token token = ReadOperator();

                tokens.add(token);
                tokenTable.Add(token);
            }
        }

        return tokens;
    }

    private Token ReadIdentifier() {

        while (forward < source.length()) {

            char current = source.charAt(forward);

            if (Character.isLetterOrDigit(current) || current == '_') {
                forward++;
            }
            else {
                break;
            }
        }

        String lexeme = source.substring(begin, forward);

        if (KEYWORDS.contains(lexeme)) {
            return new Token(
                TokenType.KEYWORD,
                lexeme,
                null
            );
        }

        symbolTable.Add(lexeme);

        return new Token(
            TokenType.IDENTIFIER,
            lexeme,
            null
        );
    }

    private Token ReadNumber() {

        /*
         * Parte inteira
         */
        while (forward < source.length()
                && Character.isDigit(source.charAt(forward))) {
            forward++;
        }

        /*
         * Número seguido de letra ou _
         *
         * Exemplo:
         * 123abc
         * 10teste
         */
        if (forward < source.length()) {

            char current = source.charAt(forward);

            if (Character.isLetter(current) || current == '_') {

                while (forward < source.length()) {

                    current = source.charAt(forward);

                    if (Character.isLetterOrDigit(current)
                            || current == '_') {
                        forward++;
                    }
                    else {
                        break;
                    }
                }

                String lexeme = source.substring(begin, forward);

                return new Token(
                    TokenType.ERROR,
                    lexeme,
                    null
                );
            }
        }

        /*
         * Decimal usando vírgula
         *
         * Exemplo:
         * 3,14
         */
        if (forward < source.length()
                && source.charAt(forward) == ','
                && forward + 1 < source.length()
                && Character.isDigit(source.charAt(forward + 1))) {

            forward++;

            while (forward < source.length()
                    && Character.isDigit(source.charAt(forward))) {
                forward++;
            }

            String lexeme = source.substring(begin, forward);

            return new Token(
                TokenType.ERROR,
                lexeme,
                null
            );
        }

        /*
         * Número real
         *
         * Exemplo:
         * 3.14
         * 1.70f
         */
        if (forward < source.length()
                && source.charAt(forward) == '.') {

            /*
             * Verifica se existe um dígito depois do ponto.
             *
             * 3.14 -> válido
             * 3.   -> erro
             */
            if (forward + 1 < source.length()
                    && Character.isDigit(source.charAt(forward + 1))) {

                forward++;

                while (forward < source.length()
                        && Character.isDigit(source.charAt(forward))) {
                    forward++;
                }

                /*
                 * Sufixo f/F
                 *
                 * 1.70f
                 */
                if (forward < source.length()
                        && (source.charAt(forward) == 'f'
                        || source.charAt(forward) == 'F')) {
                    forward++;
                }

                String lexeme = source.substring(begin, forward);

                String numericPart = lexeme;

                if (numericPart.endsWith("f")
                        || numericPart.endsWith("F")) {

                    numericPart = numericPart.substring(
                        0,
                        numericPart.length() - 1
                    );
                }

                return new Token(
                    TokenType.FLOAT,
                    lexeme,
                    Float.parseFloat(numericPart)
                );
            }

            /*
             * Caso:
             * 10.
             */
            forward++;

            String lexeme = source.substring(begin, forward);

            return new Token(
                TokenType.ERROR,
                lexeme,
                null
            );
        }

        /*
         * Inteiro
         */
        String lexeme = source.substring(begin, forward);

        return new Token(
            TokenType.INTEGER,
            lexeme,
            Integer.parseInt(lexeme)
        );
    }

    private Token ReadChar() {

        /*
         * Consome o primeiro '
         */
        forward++;

        /*
         * Fim do arquivo
         */
        if (forward >= source.length()) {

            return new Token(
                TokenType.ERROR,
                source.substring(begin, forward),
                null
            );
        }

        char current = source.charAt(forward);

        /*
         * Caractere escapado
         *
         * Exemplos:
         * '\n'
         * '\t'
         * '\\'
         * '\''
         */
        if (current == '\\') {

            forward++;

            if (forward >= source.length()) {

                return new Token(
                    TokenType.ERROR,
                    source.substring(begin, forward),
                    null
                );
            }

            forward++;
        }

        /*
         * Caractere normal
         *
         * Exemplo:
         * 'A'
         */
        else {

            if (current == '\n'
                    || current == '\r'
                    || current == '\'') {

                forward++;

                return new Token(
                    TokenType.ERROR,
                    source.substring(begin, forward),
                    null
                );
            }

            forward++;
        }

        /*
         * Deve existir o ' de fechamento
         */
        if (forward >= source.length()
                || source.charAt(forward) != '\'') {

            while (forward < source.length()
                    && source.charAt(forward) != '\''
                    && source.charAt(forward) != '\n'
                    && source.charAt(forward) != '\r') {
                forward++;
            }

            if (forward < source.length()
                    && source.charAt(forward) == '\'') {
                forward++;
            }

            return new Token(
                TokenType.ERROR,
                source.substring(begin, forward),
                null
            );
        }

        /*
         * Consome o ' de fechamento
         */
        forward++;

        String lexeme = source.substring(begin, forward);

        String value = DecodeChar(
            lexeme.substring(1, lexeme.length() - 1)
        );

        return new Token(
            TokenType.CHAR,
            lexeme,
            value
        );
    }

    private Token ReadString() {

        /*
         * Consome o primeiro "
         */
        forward++;

        while (forward < source.length()) {

            char current = source.charAt(forward);

            /*
             * Escape
             *
             * Exemplo:
             * "Arthur\nDantas"
             */
            if (current == '\\') {

                forward++;

                if (forward < source.length()) {
                    forward++;
                }

                continue;
            }

            /*
             * String terminou
             */
            if (current == '"') {

                forward++;

                String lexeme = source.substring(begin, forward);

                String value = DecodeString(
                    lexeme.substring(1, lexeme.length() - 1)
                );

                return new Token(
                    TokenType.STRING,
                    lexeme,
                    value
                );
            }

            /*
             * String não pode quebrar linha
             */
            if (current == '\n' || current == '\r') {

                String lexeme = source.substring(begin, forward);

                return new Token(
                    TokenType.ERROR,
                    lexeme,
                    null
                );
            }

            forward++;
        }

        /*
         * Chegou ao final sem encontrar "
         */
        String lexeme = source.substring(begin, forward);

        return new Token(
            TokenType.ERROR,
            lexeme,
            null
        );
    }

    private void SkipLineComment() {

        /*
         * Consome //
         */
        forward += 2;

        while (forward < source.length()
                && source.charAt(forward) != '\n'
                && source.charAt(forward) != '\r') {
            forward++;
        }
    }

    private Token SkipBlockComment() {

        /*
         * Consome /*
         */
        forward += 2;

        while (forward + 1 < source.length()) {

            if (source.charAt(forward) == '*'
                    && source.charAt(forward + 1) == '/') {

                /*
                 * Consome */
                forward += 2;

                /*
                 * Comentário é ignorado.
                 */
                return null;
            }

            forward++;
        }

        /*
         * Chegou ao final do arquivo sem encontrar */
        forward = source.length();

        String lexeme = source.substring(begin, forward);

        return new Token(
            TokenType.ERROR,
            lexeme,
            null
        );
    }

    private String DecodeString(String value) {

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {

            char current = value.charAt(i);

            if (current == '\\' && i + 1 < value.length()) {

                char next = value.charAt(++i);

                switch (next) {

                    case 'n':
                        result.append('\n');
                        break;

                    case 't':
                        result.append('\t');
                        break;

                    case 'r':
                        result.append('\r');
                        break;

                    case '\\':
                        result.append('\\');
                        break;

                    case '"':
                        result.append('"');
                        break;

                    case '\'':
                        result.append('\'');
                        break;

                    default:
                        result.append(next);
                        break;
                }
            }

            else {
                result.append(current);
            }
        }

        return result.toString();
    }

    private String DecodeChar(String value) {

        if (value.length() == 1) {
            return value;
        }

        switch (value) {

            case "\\n":
                return "\n";

            case "\\t":
                return "\t";

            case "\\r":
                return "\r";

            case "\\\\":
                return "\\";

            case "\\'":
                return "'";

            case "\\\"":
                return "\"";

            default:
                return value;
        }
    }

    private Token ReadOperator() {

        char current = source.charAt(forward);

        forward++;

        switch (current) {

            case '+':

                if (forward < source.length()
                        && source.charAt(forward) == '+') {

                    forward++;

                    return new Token(
                        TokenType.INCREMENT,
                        "++",
                        null
                    );
                }

                if (forward < source.length()
                        && source.charAt(forward) == '=') {

                    forward++;

                    return new Token(
                        TokenType.PLUS_ASSIGN,
                        "+=",
                        null
                    );
                }

                return new Token(
                    TokenType.PLUS,
                    "+",
                    null
                );

            case '-':

                if (forward < source.length()
                        && source.charAt(forward) == '-') {

                    forward++;

                    return new Token(
                        TokenType.DECREMENT,
                        "--",
                        null
                    );
                }

                if (forward < source.length()
                        && source.charAt(forward) == '=') {

                    forward++;

                    return new Token(
                        TokenType.MINUS_ASSIGN,
                        "-=",
                        null
                    );
                }

                return new Token(
                    TokenType.MINUS,
                    "-",
                    null
                );

            case '*':

                if (forward < source.length()
                        && source.charAt(forward) == '=') {

                    forward++;

                    return new Token(
                        TokenType.MULTIPLY_ASSIGN,
                        "*=",
                        null
                    );
                }

                return new Token(
                    TokenType.MULTIPLY,
                    "*",
                    null
                );

            case '/':

                if (forward < source.length()
                        && source.charAt(forward) == '=') {

                    forward++;

                    return new Token(
                        TokenType.DIVIDE_ASSIGN,
                        "/=",
                        null
                    );
                }

                return new Token(
                    TokenType.DIVIDE,
                    "/",
                    null
                );

            case '%':

                if (forward < source.length()
                        && source.charAt(forward) == '=') {

                    forward++;

                    return new Token(
                        TokenType.MOD_ASSIGN,
                        "%=",
                        null
                    );
                }

                return new Token(
                    TokenType.MOD,
                    "%",
                    null
                );

            case '=':

                if (forward < source.length()
                        && source.charAt(forward) == '=') {

                    forward++;

                    return new Token(
                        TokenType.EQUAL_EQUAL,
                        "==",
                        null
                    );
                }

                return new Token(
                    TokenType.ASSIGN,
                    "=",
                    null
                );

            case '!':

                if (forward < source.length()
                        && source.charAt(forward) == '=') {

                    forward++;

                    return new Token(
                        TokenType.NOT_EQUAL,
                        "!=",
                        null
                    );
                }

                return new Token(
                    TokenType.NOT,
                    "!",
                    null
                );

            case '<':

                if (forward < source.length()
                        && source.charAt(forward) == '=') {

                    forward++;

                    return new Token(
                        TokenType.LESS_EQUAL,
                        "<=",
                        null
                    );
                }

                return new Token(
                    TokenType.LESS,
                    "<",
                    null
                );

            case '>':

                if (forward < source.length()
                        && source.charAt(forward) == '=') {

                    forward++;

                    return new Token(
                        TokenType.GREATER_EQUAL,
                        ">=",
                        null
                    );
                }

                return new Token(
                    TokenType.GREATER,
                    ">",
                    null
                );

            case '&':

                if (forward < source.length()
                        && source.charAt(forward) == '&') {

                    forward++;

                    return new Token(
                        TokenType.AND,
                        "&&",
                        null
                    );
                }

                return new Token(
                    TokenType.ERROR,
                    "&",
                    null
                );

            case '|':

                if (forward < source.length()
                        && source.charAt(forward) == '|') {

                    forward++;

                    return new Token(
                        TokenType.OR,
                        "||",
                        null
                    );
                }

                return new Token(
                    TokenType.ERROR,
                    "|",
                    null
                );

            case ';':
                return new Token(
                    TokenType.SEMICOLON,
                    ";",
                    null
                );

            case ',':
                return new Token(
                    TokenType.COMMA,
                    ",",
                    null
                );

            case '.':
                return new Token(
                    TokenType.DOT,
                    ".",
                    null
                );

            case '(':
                return new Token(
                    TokenType.LEFT_PAREN,
                    "(",
                    null
                );

            case ')':
                return new Token(
                    TokenType.RIGHT_PAREN,
                    ")",
                    null
                );

            case '{':
                return new Token(
                    TokenType.LEFT_BRACE,
                    "{",
                    null
                );

            case '}':
                return new Token(
                    TokenType.RIGHT_BRACE,
                    "}",
                    null
                );

            case '[':
                return new Token(
                    TokenType.LEFT_BRACKET,
                    "[",
                    null
                );

            case ']':
                return new Token(
                    TokenType.RIGHT_BRACKET,
                    "]",
                    null
                );

            default:
                return new Token(
                    TokenType.ERROR,
                    String.valueOf(current),
                    null
                );
        }
    }
}