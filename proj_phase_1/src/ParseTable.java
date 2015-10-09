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

    public ParseTable(Collection<Nonterminal> ntlist) {
        row = new HashMap<Nonterminal, HashMap<Terminal, Rule>>();
        for (Nonterminal nt : ntlist) {
            row.put(nt, new HashMap<Terminal, Rule>());
            HashMap<Terminal, Rule> col = row.get(nt);
            for (TerminalRuleWrapper trw : nt.getFirstSet()){
                col.put(trw.getTerminal(),trw.getRule());
            }
        }
    }

    public ParseTable(List<Rule> rules) {

    }
}

