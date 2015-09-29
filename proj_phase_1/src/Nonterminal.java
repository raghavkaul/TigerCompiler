import java.util.List;

/**
 * Created by Raghav K on 9/26/15.
 */
public class Nonterminal implements Lexeme {
    private List<List<Lexeme>> expansions;

    public Nonterminal() {

    }

    public List<Lexeme> getExpansion(int expansionNo) {
        return expansions.get(expansionNo);
    }

    public void setExpansion(int expansionNo, List<Lexeme> expansion) {
        expansions.add(expansionNo, expansion);
    }
}
