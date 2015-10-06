import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Created by Raghav K on 9/28/15.
 */
public class TigerParser {
    private static final String STATES_FILE_NAME = "./data/states.csv";
    private static final String TRANSITIONS_FILE_NAME = "./data/transitions.csv";
    private static final String GRAMMAR_FILE_NAME = "./data/grammar.txt";
    private final ParseTable parseTable;
    private final TigerScanner infileScanner;
    private List<Rule> rules;
    private final Terminal EOF;
    private final Nonterminal tigerProg;
    private Stack<Lexeme> stack;

    public static void main(String[] args) {
        String helpStr = "Tiger language Parser options:" +
                "\n Usage: java TigerParser [options] [filename]" +
                "\n -D:\t debug output to track errors" +
                "\n -V:\t to display verbose parse result";

        boolean debug, verbose;

        TigerParser tp = null;

        if (args.length > 2 || args.length < 1) {
            System.out.println(helpStr);
            return;
        } else if (args.length == 2) {
            tp = new TigerParser(new File(args[1]));
            for (Character c : args[0].toCharArray()) {
                if (c == 'D' || c == 'd') {
                    debug = true;
                }
                if (c == 'V' || c == 'v') {
                    verbose = true;
                }
            }
        } else {
            tp = new TigerParser(new File(args[0]));
        }

        tp.parse();
    }

    public TigerParser(File infile) {
        // Initialize parse table
        parseTable = null; // TODO

        // Initialize scanner
        this.infileScanner = new TigerScanner(infile,
                new File(STATES_FILE_NAME), new File(TRANSITIONS_FILE_NAME));

        // Populate rules
        TableGenerator tg = new TableGenerator(new File(GRAMMAR_FILE_NAME));
        rules = tg.parseGrammar();

        EOF = null;
        tigerProg = null; // TODO get these from the rules

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
        Set<TokenType> errors = new HashSet<>();
        TokenType lookahead;
        Lexeme topOfStack;

        while (true) {
            topOfStack = stack.peek();
            lookahead = infileScanner.nextToken();

            if (topOfStack.equals(EOF)) {
                break;
            } else if (topOfStack instanceof Terminal) {
                if (((Terminal) topOfStack).matches(lookahead)) {
                    stack.pop();
                } else {
                    hasErrors = true;
                    errors.add(lookahead);
                    System.out.println("Failed to match the following token " +
                            "to a nonterminal: " + infileScanner.peekToken());
                }
            } else {
                Rule matchedRule = parseTable.matchRule((Nonterminal) topOfStack,
                        lookahead);

                if (matchedRule != null) {
                    stack.pop();
                    for (Lexeme l : matchedRule.getExpansion()) {
                        stack.push(l);
                    }
                } else {
                    hasErrors = true;
                    errors.add(lookahead);
                    System.out.println("Failed to match the following token " +
                            "to a rule : " + lookahead);
                }
            }
        }

        System.out.println("Parse completed with " + errors.size() + " errors.");

        return hasErrors;
    }
}
