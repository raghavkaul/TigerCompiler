/**
 * Created by Raghav K on 9/26/15.
 */
public class NontermTokenWrapper {
    private Nonterminal rule;
    private TokenType token;

    public NontermTokenWrapper(Nonterminal rule, TokenType token) {
        this.rule = rule;
        this.token = token;
    }

    public Nonterminal getRule() {
        return rule;
    }

    public void setRule(Nonterminal rule) {
        this.rule = rule;
    }

    public TokenType getToken() {
        return token;
    }

    public void setToken(TokenType token) {
        this.token = token;
    }
}
