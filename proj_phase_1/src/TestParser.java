import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.SystemException;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

public class TestParser {
    private static final String GRAMMAR_FILE = "./data/grammar.txt";
    private static List<String> filenames;
    private static List<Set<String>> expectedFunctionsByFile, expectedVarsByFile, expectedTypesByFile;
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
        final int numFiles = 7;

        String[][] functions = new String[numFiles][];
        String[][] types = new String[numFiles][];
        String[][] vars = new String[numFiles][];

        for (int i = 0; i < numFiles; i++) {
            functions[i] = new String[]{};
            types[i] = new String[]{};
            vars[i] = new String[]{};
        }

        vars[0] = new String[]{"X", "Y", "i", "sum"};
        vars[3] = new String[]{"a"};
        vars[4] = new String[]{"a", "b"};
        vars[5] = new String[]{"a", "b"};
        vars[6] = new String[]{"a"};
        types[0] = new String[]{"ArrayInt"};
        types[6] = new String[]{"int_arr"};
        functions[1] = new String[]{"print"};
        functions[2] = new String[]{"return_it"};



        expectedFunctionsByFile = new ArrayList<>();
        expectedVarsByFile = new ArrayList<>();
        expectedTypesByFile = new ArrayList<>();

        String[] stdTypes = {"int", "float", "_array_float", "array_int"};
        String[] stdLib = {"print", "printi", "flush", "getchar",
                "ord", "chr", "size", "substring", "concat", "not", "exit"};

        for (int i = 0; i < numFiles; i++) {
            expectedTypesByFile.add(new HashSet<>(Arrays.asList(stdTypes)));
            expectedFunctionsByFile.add(new HashSet<>(Arrays.asList(stdLib)));

            expectedFunctionsByFile.add(new HashSet<>(Arrays.asList(functions[i])));
            expectedVarsByFile.add(new HashSet<>(Arrays.asList(vars[i])));
            expectedTypesByFile.add(new HashSet<>(Arrays.asList(types[i])));
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
            System.out.println("============Filename: " + s + "============");
            TigerParser tp = new TigerParser(new File(s));
            TigerParser.verbose = false;
            TigerParser.debug = true;
            tp.parse();
        }
    }

    @Test
    public void testDumpSymbolTable() {
        int i = 0;
        filenames = Collections.singletonList(PREFIX + "1.tiger");
//        filenames = Arrays.asList(PREFIX+"3.tiger", PREFIX+"5.tiger");

        for (String s : filenames) {
            // Initialization
            System.out.println("============Filename: " + s + "============");
            TigerParser tp = new TigerParser(new File(s));
            TigerParser.verbose = false;
            TigerParser.debug = true;
            tp.parse();
            TypeTable tt = tp.getTypeTable();
            VarTable vt = tp.getVarTable();
            FunctionTable ft = tp.getFunctionTable();

            // Sanity checks
            assertNotNull(tt);
            assertNotNull(vt);
            assertNotNull(ft);
            assertNotNull(tt.getTable());
            assertNotNull(vt.getTable());
            assertNotNull(ft.getTable());

            // Dumps
            System.out.println("-------- Types --------");
            System.out.println("Expected types: " + expectedTypesByFile.get(i));
            for (Map.Entry<String, TypeRecord> me : tt.getTable().entrySet()) {
                System.out.println(me.getKey() + "\t\t" + me.getValue());
            }

            System.out.println("-------- Vars --------");
            System.out.println("Expected vars: " + expectedVarsByFile.get(i));
            for (Map.Entry<String, VarRecord> me : vt.getTable().entrySet()) {
                System.out.println(me.getKey() + "\t\t" + me.getValue());
            }

            System.out.println("-------- Functions --------");
            System.out.println("Expected functions: " + expectedFunctionsByFile.get(i));
            for (Map.Entry<String, FunctionRecord> me : ft.getTable().entrySet()) {
                System.out.println(me.getKey() + "\t\t" + me.getValue());
            }
            i++;
        }
    }

    @Test
    public void dumpVars() {
        for (String s : filenames) {
            System.out.println("============Filename: " + s + "============");
            TigerParser tp = new TigerParser(new File(s));
            TigerParser.verbose = false;
            TigerParser.debug = true;
            tp.parse();
            VarTable vt = tp.getVarTable();

            for (Map.Entry<String, VarRecord> me : vt.getTable().entrySet()) {
                System.out.println(me.getKey() + "\t\t" + me.getValue());
            }
        }

    }

    @Test
    public void dumpTypes() {
        for (String s : filenames) {
            System.out.println("============Filename: " + s + "============");
            TigerParser tp = new TigerParser(new File(s));
            TigerParser.verbose = false;
            TigerParser.debug = true;
            tp.parse();
            TypeTable tt = tp.getTypeTable();
            System.out.println(tt);
        }

    }

    @Test
    public void dumpFuncs() {
        for (String s : filenames) {
            System.out.println("============Filename: " + s + "============");
            TigerParser tp = new TigerParser(new File(s));
            TigerParser.verbose = false;
            TigerParser.debug = true;
            tp.parse();
            FunctionTable ft = tp.getFunctionTable();
            System.out.println(ft);
        }

    }

    @Test
    public void dumpParseTree() {
        filenames = Collections.singletonList(PREFIX + "1.tiger");
        for (String filename : filenames) {
            System.out.println("============Filename: " + filename + "============");
            TigerParser tp = new TigerParser(new File(filename));
            TigerParser.verbose = false;
            TigerParser.debug = true;
            tp.parse();
            ParseTree pt = tp.getParseTreeOld();

            pt.print();
        }
    }
}
