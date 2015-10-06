import java.util.List;

/**
 * Created by Raghav K on 9/26/15.
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
