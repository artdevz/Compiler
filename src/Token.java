public class Token {
    
    private final TokenType type;
    private final String lexeme;
    private  final Object attribute;

    public Token(TokenType type, String lexeme, Object attribute) {
        this.type = type;
        this.lexeme = lexeme;
        this.attribute = attribute;
    }

    public TokenType GetType() { return type; }
    public String GetLexeme() { return lexeme; }
    public Object GetAttribute() { return attribute; }

}
