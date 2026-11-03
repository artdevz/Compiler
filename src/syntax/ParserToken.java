package syntax;

public record ParserToken(
    String terminal,
    String lexeme,
    int line,
    int column
) {}
