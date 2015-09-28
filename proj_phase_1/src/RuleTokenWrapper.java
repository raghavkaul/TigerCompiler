/**
 * Created by Raghav K on 9/26/15.
 */
public class RuleTokenWrapper {
    private ParseRule rule;
    private Token token;


    public RuleTokenWrapper(ParseRule rule, Token token) {
        this.rule = rule;
        this.token = token;
    }

    public ParseRule getRule() {
        return rule;
    }

    public void setRule(ParseRule rule) {
        this.rule = rule;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }
}
