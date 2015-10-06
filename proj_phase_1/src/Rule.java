import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Raghav K on 9/28/15.
 */
public class Rule {
    private List<Lexeme> expansion;
    private Nonterminal parent;
    private Set<Terminal> firstSet, followSet;
    private int ruleNo;

    public Rule(int ruleNo) {
        expansion = new LinkedList<Lexeme>();
        firstSet = new HashSet<Terminal>();
        followSet = new HashSet<Terminal>();
        this.ruleNo = ruleNo;
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

    public void addToFirstSet(Terminal t) {
        firstSet.add(t);
    }

    public void addToFollowSet(Terminal t) {
        followSet.add(t);
    }

    public Nonterminal getParent() {
        return parent;
    }

    public void setParent(Nonterminal parent) {
        this.parent = parent;
    }

    public int getRuleNo() {
        return ruleNo;
    }

    public Set<Terminal> getFirstSet() {
        return firstSet;
    }

    public Set<Terminal> getFollowSet() {
        return followSet;
    }

    @Override
    public String toString() {
        String result = "";
        result += "Rule " + ruleNo;
        result += "\nExpansion:";
        for (Lexeme l : expansion) {
            if (l instanceof Nonterminal) {
                result += "\t" + ((Nonterminal) l).getName();
            } else {
                result += "\t" + l.toString();
            }
        }
        return result;
    }
}
