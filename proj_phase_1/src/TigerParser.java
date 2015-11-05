import java.io.File;
import java.util.*;

/**
 * Class representing high-level parser
 */
public class TigerParser {
    private static final String STATES_FILE_NAME = "./data/states.csv";
    private static final String TRANSITIONS_FILE_NAME = "./data/transitions.csv";
    private static final String GRAMMAR_FILE_NAME = "./data/grammar.txt";
    private static final String EOF_TERM = TokenType.EOF_TOKEN.toString();
    public static boolean debug, verbose;
    public SymbolTable symbolTable;
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

        tg.generateParsertable();
        this.parseTable= tg.getParserTable();

        // Initialize scanner
        this.infileScanner = new TigerScanner(infile,
                new File(STATES_FILE_NAME), new File(TRANSITIONS_FILE_NAME));

        // Initialize stack to contain end symbol and Tiger-program non-terminal.
        stack = new Stack<>();
        stack.push(EOF_TERM);
        stack.push("<tiger-program>");
    }


    private enum SymbolFoundState {
        NONE, EXPECTING_NAME, FOUND_NAME, EXPECTING_VARLIST, EXPECTING_TYPE_DECL,
        EXPECTING_PARAMLIST, EXPECTING_PARAMTYPE, FOUND_PARAMTYPE, EXPECTING_RETURNTYPE
    }

    public SymbolTable getSymbolTable() {
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
        String lookahead, topOfStack, tokenLiteral, currParamName = "";
        symbolTable = new SymbolTable();
        SymbolRecord symbolRecord = null;
        SymbolFoundState sfs = SymbolFoundState.NONE;
        List<String> symbolNames = new ArrayList<>();

        int i = 0;
        while (true) {
            topOfStack = stack.peek();
            lookahead = infileScanner.peekToken().toString();
            tokenLiteral = infileScanner.peekToken().tokenLiteral;

            if (lookahead.equals(new Token(TokenType.COMMENT_END).toString())) {
                infileScanner.nextToken();
                continue;
            }
            if (lookahead.equals(TokenType.INVALID.toString())) {
                System.out.println("Scanner Error");
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

                    // Symbol table population DFA
                    switch(sfs) {
                        case NONE:
                            boolean foundDeclaration = false;
                            if (lookahead.equalsIgnoreCase("var")) {
                                symbolRecord = new VarRecord();
                                foundDeclaration = true;
                            } else if (lookahead.equalsIgnoreCase("type")) {
                                symbolRecord = new TypeRecord();
                                foundDeclaration = true;
                            } else if (lookahead.equalsIgnoreCase("function")) {
                                symbolRecord = new FunctionRecord();
                                foundDeclaration = true;
                            }

                            sfs = foundDeclaration ? SymbolFoundState.EXPECTING_NAME : sfs;
                            break;
                        case EXPECTING_NAME:
                            symbolNames.add(infileScanner.peekToken().getToken());
                            sfs = SymbolFoundState.FOUND_NAME;
                            break;
                        case FOUND_NAME:
                            if (lookahead.equalsIgnoreCase("eq") || lookahead.equalsIgnoreCase("assign")) {
                                for (String symbolName : symbolNames) {
                                    symbolTable.insert(symbolName, symbolRecord);
                                }

                                symbolRecord = null;
                                symbolNames.clear();
                                sfs = SymbolFoundState.NONE;
                            } else if (lookahead.equalsIgnoreCase("colon")) {
                                sfs = SymbolFoundState.EXPECTING_TYPE_DECL;
                            } else if (lookahead.equalsIgnoreCase("comma")) {
                                sfs = SymbolFoundState.EXPECTING_VARLIST;
                            } else if (lookahead.equalsIgnoreCase("lparen")) {
                                sfs = SymbolFoundState.EXPECTING_PARAMLIST;
                            }
                            break;
                        case EXPECTING_VARLIST: // e.g. var a, b : int;
                            symbolNames.add(tokenLiteral);
                            sfs = SymbolFoundState.FOUND_NAME;
                            break;
                        case EXPECTING_TYPE_DECL:
                            if (symbolTable.contains(tokenLiteral)) {
                                ((VarRecord) symbolRecord).setTypeName(tokenLiteral);
                            } else {
                                System.out.println("Type " + tokenLiteral + "is undefined.");
                            }
                            sfs = SymbolFoundState.FOUND_NAME;
                            break;
                        case EXPECTING_PARAMLIST:
                            if (lookahead.equalsIgnoreCase("id")) {
                                currParamName = tokenLiteral;
                                sfs = SymbolFoundState.EXPECTING_PARAMTYPE;
                            }
                            break;
                        case EXPECTING_PARAMTYPE:
                            if (lookahead.equalsIgnoreCase("id")) {
                                if (symbolTable.contains(tokenLiteral)) {
                                    ((FunctionRecord) symbolRecord).addParam(currParamName, tokenLiteral);
                                } else {
                                    System.out.println("Type " + tokenLiteral + " is undefined.");
                                }
                                sfs = SymbolFoundState.FOUND_PARAMTYPE;
                            } else if (lookahead.equalsIgnoreCase("int_type")) {
                                ((FunctionRecord) symbolRecord).addParam(currParamName, "int");
                                sfs = SymbolFoundState.FOUND_PARAMTYPE;
                            } else if (lookahead.equalsIgnoreCase("float_type")) {
                                ((FunctionRecord) symbolRecord).addParam(currParamName, "float");
                                sfs = SymbolFoundState.FOUND_PARAMTYPE;
                            } else if (lookahead.equalsIgnoreCase("array")) {
                                ((FunctionRecord) symbolRecord).addParam(currParamName, "array");
                                sfs = SymbolFoundState.FOUND_PARAMTYPE;
                            }
                            break;
                        case FOUND_PARAMTYPE:
                            if (lookahead.equalsIgnoreCase("rparen")) {
                                sfs = SymbolFoundState.EXPECTING_RETURNTYPE;
                            } else if (lookahead.equalsIgnoreCase("comma")) {
                                sfs = SymbolFoundState.EXPECTING_PARAMLIST;
                            }
                            break;
                        case EXPECTING_RETURNTYPE:
                            if (lookahead.equalsIgnoreCase("begin")) {
                                for (String symbolName : symbolNames) {
                                    symbolTable.insert(symbolName, symbolRecord);
                                }

                                symbolRecord = null;
                                symbolNames.clear();
                                sfs = SymbolFoundState.NONE;
                            } else if (!lookahead.equalsIgnoreCase("colon")) {
                                if (symbolTable.contains(tokenLiteral)) {
                                    ((FunctionRecord) symbolRecord).setReturnType(tokenLiteral);
                                }
                            }
                            break;
                        default:
                            System.out.println("You broke the symbol table state machine :(");
                            break;
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
