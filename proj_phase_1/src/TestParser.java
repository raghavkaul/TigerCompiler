import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

public class TestParser {
    private static final String GRAMMAR_FILE = "./data/grammar.txt";
    private static List<String> filenames;
    private static final String PREFIX = "./data/test_prog/test";
    private TableGenerator tg;

    @Before
    public void setUp() {
        tg = new TableGenerator(new File(GRAMMAR_FILE));
        filenames = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            filenames.add(PREFIX + i + ".tiger");
        }
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
        for (Nonterminal nt : tg.nonterminals.values()) {
            Set<Terminal> firstOfNt = new LinkedHashSet<>();
            for (Rule rule : nt.getDerivations()) {
                Set<Nonterminal> first = new LinkedHashSet<>();
                //tg.updateFirstSet(rule, 0, first);
                // firstOfNt.addAll(rule.getFirstSet());
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
        tg.parseGrammar();
        ParseTable pt = tg.generateParseTable();

        System.out.println("=== Parse Table === ");
        int i = 0;
        for (Map.Entry<NontermTokenWrapper, Rule> me: pt.ruleTable.entrySet()) {
            System.out.println("Table entry " + i++);
            System.out.println("Nonterm: " + me.getKey().getNonterminal() + "\t"
                    + "and token: " + me.getKey().getToken().getType() + "\t"
                    + "are matched by : " + me.getValue());
        }
    }

    @Test
    public void testTerminalEquality() {
        Terminal t = new Terminal("NIL");
        Terminal t2 = new Terminal("NIL");
        Terminal t3 = new Terminal(TokenType.NIL);
        assertEquals(t, t2);
        assertEquals(t2, t3);
    }

    @Test
    public void testParserInit() {
        for (String s : filenames) {
            TigerParser tp = new TigerParser(new File(s));

            assertNotNull(tp.infileScanner);
            assertNotNull(tp.parseTable);
            assertNotNull(tp.stack);

            assertEquals(tp.stack.size(), 2);
        }
    }
    @Test
    public void testParse() {
        for (String s : filenames) {
            TigerParser tp = new TigerParser(new File(s));
            TigerParser.verbose = true;
            tp.parse();
        }
    }
}
