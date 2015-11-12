import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class representing a lexeme matched by more than one token in a series
 * or many possible tokens
 */
public class Nonterminal implements Lexeme {
    private String name;
    private List<Rule> expansions;
    private Set<TerminalRuleWrapper> firstSet, followSet;

    public Nonterminal(String name) {
        this.name = name;
        this.expansions = new ArrayList<>();
        this.firstSet = new HashSet<>();
        this.followSet = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public List<Rule> getDerivations() {
        return expansions;
    }

    public void addExpansion(Rule expansion) {
        expansions.add(expansion);
    }

    public void addFirstSet(Set<TerminalRuleWrapper> set) {firstSet.addAll(set);}

    public void addFollowSet(Set<TerminalRuleWrapper> set) {followSet.addAll(set);}

    public Set<TerminalRuleWrapper> getFirstSet () {return firstSet;}
    public Set<TerminalRuleWrapper> getFollowSet () {return followSet;}

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o != null
                && o instanceof Nonterminal
                && ((Nonterminal) o).getName().equals(this.getName()));
    }

    @Override
    public String toString() {
        return name;
    }
}
