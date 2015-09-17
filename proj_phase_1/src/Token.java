/**
 * Created by Raghav K on 9/17/15.
 */
public class Token {
    private TokenType type;
    private String token;

    public Token(TokenType type, String token) {
        this.type = type;
        this.token = token;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public TokenType getType() {

        return type;
    }

    public String getToken() {
        return token;
    }
}
