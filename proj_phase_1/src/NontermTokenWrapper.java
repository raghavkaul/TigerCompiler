/**
 * Created by Raghav K on 9/26/15.
 */
public class NontermTokenWrapper {
    private Nonterminal rule;
    private Token token;

    public NontermTokenWrapper(Nonterminal rule, Token token) {
        this.rule = rule;
        this.token = token;
    }

    public Nonterminal getRule() {
        return rule;
    }

    public void setRule(Nonterminal rule) {
        this.rule = rule;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
