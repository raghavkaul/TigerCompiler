import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.SystemException;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

public class TestParser {
    private static final String GRAMMAR_FILE = "./data/grammar.txt";
    private static List<String> filenames;
    private static List<Set<String>> expectedSymbolsByFile;
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
        initExpectedSymbols();

    }

    public void initExpectedSymbols() {
        expectedSymbolsByFile = new ArrayList<>();
        String[] expected1 = {"ArrayInt", "X", "Y", "i", "sum"},
                expected2 = {"print"},
                expected3 = {"return_it"},
                expected4 = {"a"},
                expected5 = {"a", "b"},
                expected6 = {"a", "b"},
                expected7 = {"int_arr"};

        expectedSymbolsByFile.add(new HashSet<String>(Arrays.asList(expected1)));
        expectedSymbolsByFile.add(new HashSet<String>(Arrays.asList(expected2)));
        expectedSymbolsByFile.add(new HashSet<String>(Arrays.asList(expected3)));
        expectedSymbolsByFile.add(new HashSet<String>(Arrays.asList(expected4)));
        expectedSymbolsByFile.add(new HashSet<String>(Arrays.asList(expected5)));
        expectedSymbolsByFile.add(new HashSet<String>(Arrays.asList(expected6)));
        expectedSymbolsByFile.add(new HashSet<String>(Arrays.asList(expected7)));

        String[] builtins = {"float", "int", "array"};

        for (Set<String> s : expectedSymbolsByFile) {
            s.addAll(Arrays.asList(builtins));
        }
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
        int i = 0;
        for (String s : filenames) {
            System.out.println("============Filename: " + s + "============");
            TigerParser tp = new TigerParser(new File(s));
            TigerParser.verbose = false;
            TigerParser.debug = true;
            tp.parse();
            st = tp.getSymbolTable();
            int numBuiltInTypes = tp.getSymbolTable().getBuiltins().size();

            // Sanity checks
            assertNotNull(st);
            assertNotNull(st.getTable());
            assertNotEquals(st.getTable().size(), 0);
            assertNotNull(st.getTable().entrySet());


            // Dumps
            System.out.println("Expected symbols: " + expectedSymbolsByFile.get(i));
            System.out.println("Actual symbols: ");
            for (Map.Entry<String, SymbolRecord> me : st.getTable().entrySet()) {
                System.out.println("Symbol: " + me.getKey() + "\tSymbol record:" + me.getValue());
            }

            // Unit tests

            assertEquals(expectedSymbolsByFile.get(i).size(), st.getTable().size());

            i++;
        }


    }
}
