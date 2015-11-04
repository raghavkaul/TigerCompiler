import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

public class TestParser {
    private static final String GRAMMAR_FILE = "./data/grammar.txt";
    private static List<String> filenames;
    private static final String PREFIX = "./data/test_prog/test";
    private TableGen tg;

    @Before
    public void setUp() {
        tg = new TableGen(new File(GRAMMAR_FILE));
        filenames = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            filenames.add(PREFIX + i + ".tiger");
        }
        tg.generateParsertable();
    }

    @Test
    public void dumpParseGrammarOut() {
    }

    @Test
    public void dumpRules() {
        tg.printAllRules();
    }

    @Test
    public void dumpFirstSet() {
        tg.printAllFirstsets();
    }

    @Test
    public void dumpFollowSets() {
        tg.printAllFollowsets();
    }



    @Test
    public void dumpParseTable() {
        tg.printparsertable();
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
            System.out.println("=================" + s + "=============");
            TigerParser tp = new TigerParser(new File(s));
            TigerParser.verbose = false;
            TigerParser.debug = true;
            tp.parse();
        }
    }

    @Test
    public void testSymbolTableGeneration() {
        SymbolTable st;
        for (String s : filenames) {
            TigerParser tp = new TigerParser(new File(s));
            TigerParser.verbose = false;
            TigerParser.debug = true;
            st = tp.generateSymbolTable();
            System.out.println("=================" + s + "=============");
            for (Map.Entry<String, SymbolRecord> me : st.getTable().entrySet()) {
                System.out.println("Symbol: " + me.getKey() + "Symbol record:" + me.getValue());
            }
        }


    }
}
