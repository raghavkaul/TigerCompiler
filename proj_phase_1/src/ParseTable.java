import java.util.*;

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

    public Rule matchRule(Nonterminal nonterminal, Token token) {
        return row.get(nonterminal).get(new Terminal(token.getType()));
    }

    protected HashMap<Nonterminal, HashMap<Terminal, Rule>> row;


    public ParseTable(List<Rule> rules) {

    }
}

