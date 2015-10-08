import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class representing the parse table
 * Wrapper around Map function from a tuple (Nonterminal top of stack, Token read from input)
 * to applicable rule, which is a specific expansion of the nonterminal
 * the application of which would match the token read from input
 */
public class ParseTable {
    protected Map<NontermTokenWrapper, Rule> ruleTable;

    public ParseTable() {
        ruleTable = new HashMap<>();
    }

    public void addRule(Nonterminal nonterminal, Token token, Rule rule) {
        ruleTable.put(new NontermTokenWrapper(nonterminal, token), rule);
    }

    public Rule matchRule(Nonterminal nonterminal, Token token) {
        return ruleTable.get(new NontermTokenWrapper(nonterminal, token));
    }
}
