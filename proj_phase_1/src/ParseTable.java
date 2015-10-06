import java.util.HashMap;
import java.util.Map;

/**
 * Created by Raghav K on 9/26/15.
 */
public class ParseTable {
    private Map<NontermTokenWrapper, Rule> ruleTable;

    public ParseTable() {
        ruleTable = new HashMap<NontermTokenWrapper, Rule>();
    }

    public void addRule(Nonterminal nonterminal, TokenType token, Rule rule) {
        ruleTable.put(new NontermTokenWrapper(nonterminal, token), rule);
    }

    public Rule matchRule(Nonterminal nonterminal, TokenType token) {
        return ruleTable.get(new NontermTokenWrapper(nonterminal, token));
    }
}
