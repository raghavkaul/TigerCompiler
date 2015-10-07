import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class representing a single expansion rule for a nonterminal
 * First and follow sets must belong to a rule
 */
public class Rule {
    private List<Lexeme> expansion;
    private Nonterminal parent;
    private Set<Terminal> firstSet, followSet;
    private int ruleNo;

    public Rule(int ruleNo) {
        expansion = new LinkedList<>();
        firstSet = new HashSet<>();
        followSet = new HashSet<>();
        this.ruleNo = ruleNo;
    }

    public List<Lexeme> getExpansion() {
        return expansion;
    }

    public void addLexeme(Lexeme l) {
        expansion.add(l);
    }

    public Set<Terminal> getFirstSet() {
        return firstSet;
    }

    public void addToFirstSet(Terminal t) {
        firstSet.add(t);
    }

    public Set<Terminal> getFollowSet() {
        return followSet;
    }

    public Nonterminal getParent() {
        return parent;
    }

    public void setParent(Nonterminal parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "Rule num : " + ruleNo;
    }
}
