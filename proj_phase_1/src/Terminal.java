/**
 * Created by Raghav K on 9/28/15.
 */
public class Terminal implements Lexeme {
    public TokenType getTokenType() {
        return tokenType;
    }

    private TokenType tokenType;

    public Terminal(String termName) {
        this.tokenType = TokenType.valueOf(termName);
    }

    public Terminal(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public boolean matches(TokenType token) {
        return this.tokenType.equals(token);
    }

    @Override
    public String toString() {
        return tokenType.toString();
    }
}
