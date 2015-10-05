import java.util.LinkedList;
import java.util.List;

/**
 * Created by Raghav K on 9/28/15.
 */
public class Rule {
    private List<Lexeme> expansion;
    private int ruleNo;

    public Rule() {
        expansion = new LinkedList<Lexeme>();
    }

    public List<Lexeme> getExpansion() {
        return expansion;
    }

    public void setRuleNo(int ruleNo) {
        this.ruleNo = ruleNo;
    }

    public void addLexeme(Lexeme l) {
        expansion.add(l);
    }
}
