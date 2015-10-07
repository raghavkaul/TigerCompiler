/**
 * Wrapper class around token types that allows us to treat them as lexemes from the grammar
 */
public class Terminal implements Lexeme {
    private TokenType tokenType;

    public Terminal(String termName) {
        this.tokenType = TokenType.valueOf(termName);
    }

    public Terminal(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public boolean matches(Token token) {
        return this.tokenType.equals(token.getType());
    }

    public boolean matches(TokenType tokenType) { return this.tokenType.equals(tokenType); }

    @Override
    public String toString() {
        return tokenType.toString();
    }

    @Override
    public int hashCode() {
        return tokenType.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof  Terminal) {
            Terminal temp = (Terminal) o;
            if (temp.matches(this.tokenType))
                return true;
        }
        return false;
    }
}
