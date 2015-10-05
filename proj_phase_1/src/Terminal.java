/**
 * Created by Raghav K on 9/28/15.
 */
public class Terminal implements Lexeme {
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

    public boolean matches(Token token) {
        return this.tokenType.equals(token);
    }
    
}
