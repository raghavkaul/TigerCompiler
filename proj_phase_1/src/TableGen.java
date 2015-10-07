import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Raghav K on 10/6/15.
 */
public class TableGen {
    private List<Rule> rules;
    private Set<Nonterminal> nonterminals;
    private Map<Nonterminal, Set<Terminal>> firstSets, followSets;
    private ParseTable parseTable;

    // updates global list of rules
    public void parseGrammar(File infile) {

    }

    public void populateFirstFollow(List<Rule> rules) {

    }
}
