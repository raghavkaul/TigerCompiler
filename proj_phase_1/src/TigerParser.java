import org.omg.CORBA.CODESET_INCOMPATIBLE;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Class representing high-level parser
 */
public class TigerParser {
    private static final String STATES_FILE_NAME = "./data/states.csv";
    private static final String TRANSITIONS_FILE_NAME = "./data/transitions.csv";
    private static final String GRAMMAR_FILE_NAME = "./data/grammar.txt";
    private static final String EOF_TERM = TokenType.EOF_TOKEN.toString();
    private final File infile;
    public static boolean debug, verbose;
    public final Map<String, Rule> parseTable;
    public final TigerScanner infileScanner;
    public Stack<String> stack;

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
        TableGen tg = new TableGen(new File(GRAMMAR_FILE_NAME));

        List<Rule> rules = tg.getRules();
        tg.generateParsertable();
        this.parseTable= tg.getParserTable();

        // Initialize scanner
        this.infile = infile;
        this.infileScanner = new TigerScanner(infile,
                new File(STATES_FILE_NAME), new File(TRANSITIONS_FILE_NAME));

        // Initialize stack to contain end symbol and Tiger-program non-terminal.
        stack = new Stack<>();
        stack.push(EOF_TERM);
        stack.push("<tiger-program>");
    }


    private enum SymbolFoundState {
        NONE, FOUND_DECLARATION, FOUND_NAME;
    }

    public SymbolTable generateSymbolTable() {
        Scanner symbolTableScanner = null;
        try {
            symbolTableScanner = new Scanner(infile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SymbolTable symbolTable = new SymbolTable();
        SymbolRecord symbolRecord = null;
        SymbolFoundState sfs = SymbolFoundState.NONE;
        String lookahead, currSymbolName = "UNINITIALIZED";

        while (symbolTableScanner != null && symbolTableScanner.hasNext()) {
            lookahead = symbolTableScanner.next();

            if (lookahead.equalsIgnoreCase("var") || lookahead.equalsIgnoreCase("type")) {
                symbolRecord = lookahead.equalsIgnoreCase("var") ? new VarRecord() : new TypeRecord();
                sfs = SymbolFoundState.FOUND_DECLARATION;
            } else if (sfs == SymbolFoundState.FOUND_DECLARATION) {
                currSymbolName = lookahead;
                sfs = SymbolFoundState.FOUND_NAME;
            } else if (sfs == SymbolFoundState.FOUND_NAME && lookahead.equals(":")) {
                if (symbolTable.lookUp(lookahead) != null) {
                    // Type has been previously defined,
                    symbolTable.insert(currSymbolName, symbolRecord);
                } else {
                    // Parse error
                    System.out.println("Semantic error at token " + lookahead + ": no such type exists");
                }
                sfs = SymbolFoundState.NONE;
            }
        }
        return symbolTable;
    }

    /**
     * Parses file passed in as infile
     * @return true for successful parse, else false
     */
    public boolean parse() {
        ParseTree parseTree = new ParseTree();
        boolean hasErrors = false;
        Set<String> errors = new HashSet<>();
        String lookahead;
        String topOfStack;

        int i = 0;
        while (true) {
            topOfStack = stack.peek();
            lookahead = infileScanner.peekToken().toString();
            if (lookahead.equals(new Token(TokenType.COMMENT_END).toString())) {
                infileScanner.nextToken();
                continue;
            }
            if (lookahead.equals(TokenType.INVALID.toString())) {
                System.out.println("Scanner Error (line )");
                infileScanner.nextToken();
            }

            if (verbose) {
                System.out.println("Iteration : " + i++
                        + "\tTop of Stack: " + topOfStack
                        + "\tToken: " + lookahead);
            }

            if (lookahead.equals(EOF_TERM)) {
                if (!topOfStack.equals(EOF_TERM)) {
                    hasErrors = true;
                    System.out.println("Unexpectedly reached end of file while parsing.");
                }
                break;
            }
            if (topOfStack.equals(EOF_TERM)) {
                if (!lookahead.equals(new Token(TokenType.EOF_TOKEN))) {
                    hasErrors = true;
                    System.out.println("Unexpected input past end of file.");
                }
                // Can't match more tokens -> implies parse completed.
                break;
            } else if (isTerminal(topOfStack)) {
                if (topOfStack.equals(TokenType.NIL.toString())) {
                    stack.pop();
                    continue;
                }

                if ((topOfStack).equals(lookahead)) {
                    // We've read a token matching our expected terminal
                    // Move the stack and stream forward
                    if (verbose) {
                        System.out.println(topOfStack);
                    }
                    stack.pop();
                    infileScanner.nextToken();
                } else {
                    hasErrors = true;
                    errors.add(lookahead);
                    if (lookahead.equals("SEMI")) {
                        stack.pop();
                    } else {
                        infileScanner.nextToken();
                        if (debug) {
                            System.out.println("Failed to match the following token " +
                                    "to a nonterminal: " + infileScanner.peekToken());
                        }
                    }
                }
            } else { // The lexeme is a non-terminal that needs to be expanded
                Rule matchedRule = parseTable.get(topOfStack + ", " + lookahead);

                if (matchedRule != null) {
                    if (verbose) {
                        System.out.println(matchedRule.getName());
                    }
                    // We've found a matching rule, need to push its' entire expansion to the stack
                    stack.pop();

                    // Yay FP
                    for (int x = matchedRule.getExpansion().size() - 1; x >= 0; x--) {
                        stack.push(matchedRule.getExpansion().get(x));
                    }
                } else {
                    if (!lookahead.equals("SEMI"))
                        infileScanner.nextToken();
                    else
                        stack.pop();
                    hasErrors = true;
                    errors.add(lookahead);
                    if (debug) {
                        System.out.println("Failed to match the following token " +
                                "to a rule : " + lookahead);
                    }
                }
            }
        }

        hasErrors = hasErrors || infileScanner.hasErrors();
        System.out.println("Parse completed with " + (errors.size() + infileScanner.getNumErrors()) + " errors.");


        return hasErrors;
    }

    private boolean isTerminal(String str) {
        return !str.contains("<");
    }
}
