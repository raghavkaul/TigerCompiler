/**
 * Created by Raghav K on 9/28/15.
 */
public class Terminal implements Lexeme {
    private Token token;

    public void setToken(Token token) {
        this.token = token;
    }

    public boolean matches(Token token) {
        return this.token.equals(token);
    }
    
}
