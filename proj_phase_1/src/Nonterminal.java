import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a lexeme matched by more than one token in a series
 * or many possible tokens
 */
public class Nonterminal implements Lexeme {
    private String name;
    private List<Rule> expansions;

    public Nonterminal(String name) {
        this.name = name;
        this.expansions = new ArrayList<>();
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
        return expansions.toString();
    }
}
