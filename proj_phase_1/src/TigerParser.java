import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by Raghav K on 9/28/15.
 */
public class TigerParser {
    private static final String STATES_FILE_NAME = "./data/states.csv";
    private static final String TRANSITIONS_FILE_NAME = "./data/transitions.csv";
    private final ParseTable parseTable;
    private final TigerScanner infileScanner;
    private Set<Rule> rules;
    private final Terminal EOF;
    private final Nonterminal tigerProg;
    private Stack<Lexeme> stack;

    public TigerParser(File infile) {

        // Initialize parse table
        parseTable = null; // TODO


        // Initialize scanner
        this.infileScanner = new TigerScanner(infile,
                new File(STATES_FILE_NAME), new File(TRANSITIONS_FILE_NAME));

        // Populate rules
        rules = populateRules();

        EOF = null;
        tigerProg = null;

        // Initialize stack to contain end symbol and Tiger-prog nonterminal.
        stack = new Stack<Lexeme>();

        stack.push(EOF);
        stack.push(tigerProg);
    }

    /**
     * Parses file passed in as infile
     * @return true for successful parse, else false
     */
    public boolean parse() {
        boolean hasErrors = false;
        Set<Token> errors = new HashSet<Token>();

        while (infileScanner.peekToken() != null) { // TODO change this to check stack size.
            Token lookahead = infileScanner.nextToken();
            // TODO
        }

        if (stack.size() != 1) {
            System.out.println("Unexpected end of file.");
            return false;
        }

        if (hasErrors) {
            System.out.println("Parse completed with " + errors.size() + " errors.");
        }

        System.out.println("");
        return hasErrors; // TODO fix
    }
    private Set<Rule> populateRules() {
        return null;
    }
}
