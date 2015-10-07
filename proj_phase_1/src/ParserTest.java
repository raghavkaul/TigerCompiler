import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ParserTest {
    private static final String GRAMMAR_FILE = "./data/grammar.txt";
    private static final String FILE_TO_PARSE = "./data/";
    private TableGenerator tg;

    @Before
    public void setUp() {
        tg = new TableGenerator(new File(GRAMMAR_FILE));
    }

    @Test
    public void dumpParseGrammarOut() {
        List<Rule> rules = tg.parseGrammar();

        for (Rule rule : rules) {
            System.out.println(rule.getParent().getName());
        }
    }

    @Test
    public void dumpRules() {
        List<Rule> rules = tg.parseGrammar();

        for(Rule rule : rules) {
            System.out.println(rule.getParent().getName() + " ::= " + rule.getExpansion());
        }
    }
    @Test
    public void dumpFirstFollowSets() {
        List<Rule> rules = tg.parseGrammar();

        for (Nonterminal nt : tg.nonterminals.values()) {
            Set<Terminal> firstOfNt = new LinkedHashSet<>();
            for (Rule rule : nt.getDerivations()) {
                Set<Nonterminal> first = new LinkedHashSet<>();
                tg.updateFirstSet(rule, 0, first);
                firstOfNt.addAll(rule.getFirstSet());
            }
            System.out.print("First set of " + nt.getName() + "\n{");
            assertFalse(firstOfNt.size() == 0);
            for (Terminal t : firstOfNt) {
                System.out.print(t.getTokenType().toString() + " ");
            }
            System.out.println("}");
        }
    }

    @Test
    public void dumpParseTable() {
        List<Rule> rules = tg.parseGrammar();

        for (Nonterminal nt : tg.nonterminals.values()) {
            Set<Terminal> firstOfNt = new LinkedHashSet<>();
            for (Rule rule : nt.getDerivations()) {
                Set<Nonterminal> first = new LinkedHashSet<>();
                tg.updateFirstSet(rule, 0, first);
                firstOfNt.addAll(rule.getFirstSet());
            }
        }
        ParseTable pt = new ParseTable();

    }

}
