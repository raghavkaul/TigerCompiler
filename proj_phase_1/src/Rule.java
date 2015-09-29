import java.util.List;

/**
 * Created by Raghav K on 9/28/15.
 */
public class Rule {
    private Nonterminal nonterminal;
    private int ruleNo;

    public List<Lexeme> getExpansion() {
        return nonterminal.getExpansion(ruleNo);
    }

    public void setNonterminal(Nonterminal nonterminal) {
        this.nonterminal = nonterminal;
    }

    public void setRuleNo(int ruleNo) {
        this.ruleNo = ruleNo;
    }
}
