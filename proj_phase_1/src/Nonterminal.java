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
    }

    public String getName() {
        return name;
    }

    public Rule getExpansionAtIndex(int expansionNo) {
        return expansions.get(expansionNo);
    }

    public List<Rule> getDerivations() {
        return expansions;
    }

    public void addExpansion(Rule expansion) {
        expansions.add(expansion);
    }

    public boolean equals(Object o) {
        return (o != null
                && o instanceof Nonterminal
                && ((Nonterminal) o).getName().equals(this.getName()));
    }
}
