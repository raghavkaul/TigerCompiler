import java.util.*;

/**
 * Class representing a single expansion rule for a nonterminal
 * First and follow sets must belong to a rule
 */
public class Rule {
    private List<Lexeme> expansion;
    private Nonterminal parent;
    private Set<TerminalRuleWrapper> firstSet, followSet;
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

    public Set<TerminalRuleWrapper> getFirstSet() {
        return firstSet;
    }

    public void addToFirstSet(Set<TerminalRuleWrapper> set) {firstSet.addAll(set);}

    public void addToFollowSet(Set<TerminalRuleWrapper> set) {followSet.addAll(set);}

    public Set<TerminalRuleWrapper> getFollowSet() {
        return followSet;
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

    @Override
    public String toString() {
        return "Rule num : " + ruleNo + " of type " + this.getParent();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Rule) {
            Rule rule = (Rule) o;
            if (rule.parent.equals(this.parent))
                return true;
        }
        return false;
    }


}
