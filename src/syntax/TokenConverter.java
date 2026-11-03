package syntax;

import lexer.Token;

public class TokenConverter {
    
    public static ParserToken Convert(Token token) {
        String terminal;

        switch (token.GetType()) {

            case IDENTIFIER:
                terminal = "id";
                break;

            case INTEGER:
            case FLOAT:
                terminal = "num";
                break;

            case KEYWORD:
                terminal = token.GetLexeme();
                break;

            case PLUS:
                terminal = "+";
                break;

            case MINUS:
                terminal = "-";
                break;

            case MULTIPLY:
                terminal = "*";
                break;

            case DIVIDE:
                terminal = "/";
                break;

            case ASSIGN:
                terminal = "=";
                break;

            case EQUAL_EQUAL:
                terminal = "==";
                break;

            case NOT_EQUAL:
                terminal = "!=";
                break;

            case LESS:
                terminal = "<";
                break;

            case GREATER:
                terminal = ">";
                break;

            case LEFT_PAREN:
                terminal = "(";
                break;

            case RIGHT_PAREN:
                terminal = ")";
                break;

            case LEFT_BRACE:
                terminal = "{";
                break;

            case RIGHT_BRACE:
                terminal = "}";
                break;

            case SEMICOLON:
                terminal = ";";
                break;

            default:
                throw new IllegalArgumentException(
                        "Token nao suportado pela gramatica: "
                        + token.GetType()
                        + " ("
                        + token.GetLexeme()
                        + ")"
                );
        }

        return new ParserToken(
            terminal,
            token.GetLexeme(),
            -1,
            -1
        );
    }

}
