import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Lexer {

    private final DoubleBuffer buffer;

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

    public Lexer(DoubleBuffer buffer, SymbolTable symbolTable, TokenTable tokenTable) {
        this.buffer = buffer;
        this.symbolTable = symbolTable;
        this.tokenTable = tokenTable;
    }

    public List<Token> Analyze() throws IOException {
        List<Token> tokens = new ArrayList<>();

        while (!buffer.IsEOF()) {
            while (!buffer.IsEOF() && Character.isWhitespace(buffer.Current())) {

                buffer.Advance();
            }

            if (buffer.IsEOF()) break;

            /*
            * Comentários
            */
            if (buffer.Current() == '/' && buffer.Peek() == '/') {
                SkipLineComment();

                continue;
            }

            if (buffer.Current() == '/' && buffer.Peek() == '*') {
                Token token = SkipBlockComment();

                if (token != null) {
                    tokens.add(token);
                    tokenTable.Add(token);
                }

                continue;
            }

            /*
            * Identificador / palavra reservada
            */
            if (Character.isLetter(buffer.Current()) || buffer.Current() == '_') {

                Token token = ReadIdentifier();

                tokens.add(token);
                tokenTable.Add(token);

                continue;
            }

            /*
            * Número
            */
            if (Character.isDigit(buffer.Current())) {
                Token token = ReadNumber();

                tokens.add(token);
                tokenTable.Add(token);

                continue;
            }

            /*
            * Caractere
            */
            if (buffer.Current() == '\'') {
                Token token = ReadChar();

                tokens.add(token);
                tokenTable.Add(token);

                continue;
            }

            /*
            * String
            */
            if (buffer.Current() == '"') {
                Token token = ReadString();

                tokens.add(token);
                tokenTable.Add(token);

                continue;
            }

            /*
            * Operadores / delimitadores
            */
            Token token = ReadOperator();

            tokens.add(token);
            tokenTable.Add(token);
        }

        return tokens;
    }

    private Token ReadIdentifier() throws IOException {
        StringBuilder lexeme = new StringBuilder();

        while (!buffer.IsEOF()) {

            char current = buffer.Current();

            if (Character.isLetterOrDigit(current) || current == '_') {
                lexeme.append(current);
                buffer.Advance();

            } 
            else break;
        }

        String value = lexeme.toString();

        if (KEYWORDS.contains(value)) {
            return new Token(
                TokenType.KEYWORD,
                value,
                null
            );
        }

        symbolTable.Add(value);

        return new Token(
            TokenType.IDENTIFIER,
            value,
            null
        );
    }

    private Token ReadNumber() throws IOException {
        StringBuilder lexeme = new StringBuilder();

        /*
        * Parte inteira
        */
        while (!buffer.IsEOF() && Character.isDigit(buffer.Current())) {

            lexeme.append(buffer.Current());
            buffer.Advance();
        }

        /*
        * Número seguido de letra ou _
        *
        * 123abc
        */
        if (!buffer.IsEOF()) {
            char current = buffer.Current();

            if (Character.isLetter(current) || current == '_') {
                while (!buffer.IsEOF()) {
                    current = buffer.Current();

                    if (Character.isLetterOrDigit(current) || current == '_') {
                        lexeme.append(current);
                        buffer.Advance();

                    } 
                    else break;
                }

                return new Token(
                    TokenType.ERROR,
                    lexeme.toString(),
                    null
                );
            }
        }

        /*
        * Decimal usando vírgula.
        *
        * 3,14
        */
        if (!buffer.IsEOF()
                && buffer.Current() == ','
                && Character.isDigit(buffer.Peek())
        ) {
            lexeme.append(buffer.Current());
            buffer.Advance();

            while (!buffer.IsEOF() && Character.isDigit(buffer.Current())) {

                lexeme.append(buffer.Current());
                buffer.Advance();
            }

            return new Token(
                TokenType.ERROR,
                lexeme.toString(),
                null
            );
        }

        /*
        * Número real
        *
        * 3.14
        * 1.70f
        */
        if (!buffer.IsEOF() && buffer.Current() == '.') {
            if (Character.isDigit(buffer.Peek())) {

                lexeme.append(buffer.Current());
                buffer.Advance();

                while (!buffer.IsEOF() && Character.isDigit(buffer.Current())) {
                    lexeme.append(buffer.Current());
                    buffer.Advance();
                }

                /*
                * Sufixo f/F
                */
                if (!buffer.IsEOF()
                        && (buffer.Current() == 'f'
                        || buffer.Current() == 'F')
                ) {
                    lexeme.append(buffer.Current());
                    buffer.Advance();
                }

                String value = lexeme.toString();

                String numericPart = value;

                if (numericPart.endsWith("f") || numericPart.endsWith("F")) {

                    numericPart = numericPart.substring(
                        0,
                        numericPart.length() - 1
                    );
                }

                return new Token(
                    TokenType.FLOAT,
                    value,
                    Float.parseFloat(numericPart)
                );
            }

            /*
            * 10.
            */
            lexeme.append(buffer.Current());
            buffer.Advance();

            return new Token(
                TokenType.ERROR,
                lexeme.toString(),
                null
            );
        }

        String value = lexeme.toString();

        return new Token(
            TokenType.INTEGER,
            value,
            Integer.parseInt(value)
        );
    }

    private Token ReadChar() throws IOException {
        StringBuilder lexeme = new StringBuilder();

        /*
        * Primeiro '
        */
        lexeme.append(buffer.Current());
        buffer.Advance();

        if (buffer.IsEOF()) {
            return new Token(
                TokenType.ERROR,
                lexeme.toString(),
                null
            );
        }

        char current = buffer.Current();

        /*
        * Escape
        */
        if (current == '\\') {
            lexeme.append(current);
            buffer.Advance();

            if (buffer.IsEOF()) {
                return new Token(
                    TokenType.ERROR,
                    lexeme.toString(),
                    null
                );
            }

            lexeme.append(buffer.Current());
            buffer.Advance();
        }

        /*
        * Caractere normal
        */
        else {
            if (current == '\n'
                    || current == '\r'
                    || current == '\'') {

                lexeme.append(current);
                buffer.Advance();

                return new Token(
                    TokenType.ERROR,
                    lexeme.toString(),
                    null
                );
            }

            lexeme.append(current);
            buffer.Advance();
        }

        /*
        * Precisa encontrar '
        */
        if (buffer.IsEOF() || buffer.Current() != '\'') {

            while (!buffer.IsEOF()
                    && buffer.Current() != '\''
                    && buffer.Current() != '\n'
                    && buffer.Current() != '\r'
            ) {
                lexeme.append(buffer.Current());
                buffer.Advance();
            }

            if (!buffer.IsEOF() && buffer.Current() == '\'') {

                lexeme.append(buffer.Current());
                buffer.Advance();
            }

            return new Token(
                TokenType.ERROR,
                lexeme.toString(),
                null
            );
        }

        /*
        * '
        */
        lexeme.append(buffer.Current());
        buffer.Advance();

        String value = DecodeChar(
            lexeme.substring(1, lexeme.length() - 1)
        );

        return new Token(
            TokenType.CHAR,
            lexeme.toString(),
            value
        );
    }

    private Token ReadString() throws IOException {
        StringBuilder lexeme = new StringBuilder();

        /*
        * Primeiro "
        */
        lexeme.append(buffer.Current());
        buffer.Advance();

        while (!buffer.IsEOF()) {
            char current = buffer.Current();

            /*
            * Escape
            */
            if (current == '\\') {
                lexeme.append(current);
                buffer.Advance();

                if (!buffer.IsEOF()) {
                    lexeme.append(buffer.Current());
                    buffer.Advance();
                }

                continue;
            }

            /*
            * Fechamento
            */
            if (current == '"') {
                lexeme.append(current);
                buffer.Advance();

                String value = DecodeString(
                    lexeme.substring(1, lexeme.length() - 1)
                );

                return new Token(
                    TokenType.STRING,
                    lexeme.toString(),
                    value
                );
            }

            /*
            * Quebra de linha
            */
            if (current == '\n' || current == '\r') {

                return new Token(
                    TokenType.ERROR,
                    lexeme.toString(),
                    null
                );
            }

            lexeme.append(current);
            buffer.Advance();
        }

        /*
        * EOF sem fechar "
        */
        return new Token(
            TokenType.ERROR,
            lexeme.toString(),
            null
        );
    }

    private void SkipLineComment() throws IOException {
        /*
        * //
        */
        buffer.Advance();
        buffer.Advance();

        while (!buffer.IsEOF()) {

            char current = buffer.Current();

            if (current == '\n'
                    || current == '\r') {

                break;
            }

            buffer.Advance();
        }
    }

    private Token SkipBlockComment() throws IOException {
        StringBuilder lexeme = new StringBuilder();

        /*
        * /*
        */
        lexeme.append(buffer.Current());
        buffer.Advance();

        lexeme.append(buffer.Current());
        buffer.Advance();

        while (!buffer.IsEOF()) {

            /*
            * Encontrou */
            if (buffer.Current() == '*'
                    && buffer.Peek() == '/') {

                lexeme.append(buffer.Current());
                buffer.Advance();

                lexeme.append(buffer.Current());
                buffer.Advance();

                /*
                * Comentário válido.
                */
                return null;
            }

            lexeme.append(buffer.Current());
            buffer.Advance();
        }

        /*
        * Comentário não fechado.
        */
        return new Token(
            TokenType.ERROR,
            lexeme.toString(),
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

    private Token ReadOperator() throws IOException {
        char current = buffer.Current();
        buffer.Advance();

        switch (current) {
            case '+':
                if (!buffer.IsEOF() && buffer.Current() == '+') {
                    buffer.Advance();

                    return new Token(
                        TokenType.INCREMENT,
                        "++",
                        null
                    );
                }

                if (!buffer.IsEOF() && buffer.Current() == '=') {
                    buffer.Advance();

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
                if (!buffer.IsEOF() && buffer.Current() == '-') {
                    buffer.Advance();

                    return new Token(
                        TokenType.DECREMENT,
                        "--",
                        null
                    );
                }

                if (!buffer.IsEOF() && buffer.Current() == '=') {
                    buffer.Advance();

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
                if (!buffer.IsEOF() && buffer.Current() == '=') {
                    buffer.Advance();

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
                if (!buffer.IsEOF() && buffer.Current() == '=') {
                    buffer.Advance();

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

                if (!buffer.IsEOF() && buffer.Current() == '=') {
                    buffer.Advance();

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

                if (!buffer.IsEOF() && buffer.Current() == '=') {
                    buffer.Advance();

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

                if (!buffer.IsEOF() && buffer.Current() == '=') {
                    buffer.Advance();

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

                if (!buffer.IsEOF() && buffer.Current() == '=') {
                    buffer.Advance();

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

                if (!buffer.IsEOF() && buffer.Current() == '=') {
                    buffer.Advance();

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

                if (!buffer.IsEOF() && buffer.Current() == '&') {
                    buffer.Advance();

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

                if (!buffer.IsEOF() && buffer.Current() == '|') {
                    buffer.Advance();

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