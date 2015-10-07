import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Class representing high-level parser
 */
public class TigerParser {
    private static final String STATES_FILE_NAME = "./data/states.csv";
    private static final String TRANSITIONS_FILE_NAME = "./data/transitions.csv";
    private static final String GRAMMAR_FILE_NAME = "./data/grammar.txt";
    private static final Terminal EOF_TERM = new Terminal(TokenType.EOF_TOKEN);
    private static boolean debug, verbose;
    protected final ParseTable parseTable;
    protected final TigerScanner infileScanner;
    protected Stack<Lexeme> stack;

    public static void main(String[] args) {
        String helpStr = "Tiger language Parser options:" +
                "\n Usage: java TigerParser [options] [filename]" +
                "\n -D:\t debug output to track errors" +
                "\n -V:\t to display verbose parse result";

        TigerParser tp;

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
        TableGenerator tg = new TableGenerator(new File(GRAMMAR_FILE_NAME));

        List<Rule> rules = tg.parseGrammar();
        rules = tg.updateRuleFirstFollowSets(rules);

        this.parseTable = tg.generateParseTable(rules);

        // Initialize scanner
        this.infileScanner = new TigerScanner(infile,
                new File(STATES_FILE_NAME), new File(TRANSITIONS_FILE_NAME));

        // Initialize stack to contain end symbol and Tiger-program non-terminal.
        stack = new Stack<>();
    }

    /**
     * Parses file passed in as infile
     * @return true for successful parse, else false
     */
    public boolean parse() {
        boolean hasErrors = false;
        Set<Token> errors = new HashSet<>();
        Token lookahead;
        Lexeme topOfStack;

        while (true) {
            topOfStack = stack.peek();
            lookahead = infileScanner.nextToken();

            if (topOfStack.equals(EOF_TERM)) {
                // Can't match more tokens -> implies parse completed.
                break;
            } else if (topOfStack instanceof Terminal) {
                if (((Terminal) topOfStack).matches(lookahead)) {
                    // We've read a token matching our expected terminal
                    // Move the stack and stream forward
                    if (verbose) {
                        System.out.println(topOfStack.toString());
                    }
                    stack.pop();
                } else {
                    hasErrors = true;
                    errors.add(lookahead);
                    if (debug) {
                        System.out.println("Failed to match the following token " +
                                "to a nonterminal: " + infileScanner.peekToken());
                    }
                }
            } else { // The lexeme is a non-terminal that needs to be expanded
                Rule matchedRule = parseTable.matchRule((Nonterminal) topOfStack,
                        lookahead);

                if (matchedRule != null) {
                    if (verbose) {
                        System.out.println(matchedRule.getParent().getName());
                    }
                    // We've found a matching rule, need to push its' entire expansion to the stack
                    stack.pop();

                    // Yay FP
                    stack.addAll(matchedRule.getExpansion());
                } else {
                    hasErrors = true;
                    errors.add(lookahead);
                    if (debug) {
                        System.out.println("Failed to match the following token " +
                                "to a rule : " + lookahead);
                    }
                }
            }
        }

        System.out.println("Parse completed with " + errors.size() + " errors.");

        return hasErrors;
    }
}
