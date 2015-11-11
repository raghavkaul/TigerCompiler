import java.io.File;
import java.util.*;

/**
 * Class representing high-level parser
 */
public class TigerParser {
    private static final String STATES_FILE_NAME = "./data/states.csv",
            TRANSITIONS_FILE_NAME = "./data/transitions.csv",
            GRAMMAR_FILE_NAME = "./data/grammar.txt",
            EOF_TERM = TokenType.EOF_TOKEN.toString();

    public static boolean debug, verbose;
    private boolean parseCompleted = false, hasErrors = false;

    private VarTable varTable = new VarTable();
    private TypeTable typeTable = new TypeTable();
    private FunctionTable functionTable = new FunctionTable();

    private ParseTree parseTree;
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
        NONE, EXPECTING_FUNCNAME, EXPECTING_VARNAME, EXPECTING_TYPENAME,
        EXPECTING_PARAMLIST, EXPECTING_PARAMTYPE, FOUND_PARAMTYPE,
        EXPECTING_RETURNTYPE, FOUND_VARNAME, EXPECTING_VARTYPE,
        FOUND_TYPENAME, EXPECTING_ARRAYSIZE, EXPECTING_ARRAYTYPE, POPULATE
    }

    public VarTable getVarTable() {
        if (!parseCompleted) {
            parse();
        }
        return varTable;

    }

    public TypeTable getTypeTable() {
        if (!parseCompleted) {
            parse();
        }
        return typeTable;

    }

    public FunctionTable getFunctionTable() {
        if (!parseCompleted) {
            parse();
        }
        return functionTable;
    }

    public ParseTree getParseTreeOld() {
        while (parseTree.getParent() != null) {
            parseTree = parseTree.getParent();
        }
        return parseTree;
    }

    /**
     * Parses file passed in as infile
     * @return true for successful parse, else false
     */
    public boolean parse() {
        if (parseCompleted) {
            return hasErrors;
        }

        parseTree = new ParseTree("<tiger-program>");
        hasErrors = false;
        Set<String> errors = new HashSet<>();
        String lookahead, topOfStack, tokenLiteral, currParamName = "";
        SymbolRecord symbolRecord = null;
        SymbolFoundState sfs = SymbolFoundState.NONE;
        List<String> symbolNames = new ArrayList<>();

        int i = 0;
        while (true) {
            topOfStack = stack.peek();
            lookahead = infileScanner.peekToken().toString();
            tokenLiteral = infileScanner.peekToken().getTokenLiteral();

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
                if (!lookahead.equals(new Token(TokenType.EOF_TOKEN).getTokenLiteral())) {
                    hasErrors = true;
                    System.out.println("Unexpected input past end of file.");
                }
                // Can't match more tokens -> implies parse completed.
                break;
            } else if (isTerminal(topOfStack)) {
                if (topOfStack.equals(TokenType.NIL.toString())) {
                    int mostRecentChildNo = parseTree.childNo;
                    while(parseTree.childNo >= parseTree.getParent().getChildren().size() - 1) {

                        parseTree = parseTree.getParent();
                        mostRecentChildNo = parseTree.childNo;
                        if (parseTree.getParent() == null) {
                            break;
                        }
                    }
                    parseTree = parseTree.getParent().getChildren().get(mostRecentChildNo + 1);

                    stack.pop();
                    // find most recent ancestor with a expansion remaining
                    continue;
                }

                if ((topOfStack).equals(lookahead)) {
                    // We've read a token matching our expected terminal
                    // Move the stack and stream forward
                    if (verbose) {
                        System.out.println(topOfStack);
                    }

                    // Symbol table population DFA
                    // TODO encapsulate variables and modularize this
                    switch(sfs) {
                        case NONE:
                            if (lookahead.equalsIgnoreCase("function")) {
                                symbolRecord = new FunctionRecord();
                                sfs = SymbolFoundState.EXPECTING_FUNCNAME;
                            } else if (lookahead.equalsIgnoreCase("var")) {
                                symbolRecord = new VarRecord();
                                sfs = SymbolFoundState.EXPECTING_VARNAME;
                            } else if (lookahead.equalsIgnoreCase("type")) {
                                symbolRecord = new TypeRecord();
                                sfs = SymbolFoundState.EXPECTING_TYPENAME;
                            }
                            break;
                        case EXPECTING_FUNCNAME:
                            if (lookahead.equalsIgnoreCase("id")) {
                                symbolNames.add(symbolNames.size(), tokenLiteral);
                                sfs = SymbolFoundState.EXPECTING_PARAMLIST;
                            }
                            break;
                        case  EXPECTING_PARAMLIST:
                            if (lookahead.equalsIgnoreCase("id")) {
                                currParamName = tokenLiteral;
                                sfs = SymbolFoundState.EXPECTING_PARAMTYPE;
                            }
                            break;
                        case EXPECTING_PARAMTYPE:
                            if (lookahead.equalsIgnoreCase("id") && typeTable.contains(tokenLiteral)) {
                                System.out.println("Semantic error: type " + tokenLiteral + " not defined.");
                            } else if (lookahead.equalsIgnoreCase("id") || lookahead.contains("array")
                                    || lookahead.equalsIgnoreCase("float_type") || lookahead.equalsIgnoreCase("int_type")){
                                ((FunctionRecord) symbolRecord).addParam(currParamName, tokenLiteral);
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
                            if (lookahead.equalsIgnoreCase("id") || lookahead.equalsIgnoreCase("float_type") || lookahead.equalsIgnoreCase("int_type") || lookahead.contains("array")) {
                                ((FunctionRecord) symbolRecord).setReturnType(tokenLiteral);
                                sfs = SymbolFoundState.POPULATE;
                            }
                            break;
                        case POPULATE:
                            for (String symbolName : symbolNames) {
                                if (symbolRecord instanceof FunctionRecord) {
                                    functionTable.insert(symbolName, ((FunctionRecord) symbolRecord));
                                } else if (symbolRecord instanceof VarRecord) {
                                    varTable.insert(symbolName, ((VarRecord) symbolRecord));
                                } else if (symbolRecord instanceof TypeRecord) {
                                    typeTable.insert(symbolName, ((TypeRecord) symbolRecord));
                                }
                            }

                            symbolNames.clear();
                            sfs = SymbolFoundState.NONE;

                            break;
                        case EXPECTING_VARNAME:
                            if (lookahead.equalsIgnoreCase("id")) {
                                symbolNames.add(symbolNames.size(), tokenLiteral);
                                sfs = SymbolFoundState.FOUND_VARNAME;
                            }
                            break;
                        case FOUND_VARNAME:
                            if (lookahead.equalsIgnoreCase("comma")) {
                                sfs = SymbolFoundState.EXPECTING_VARNAME;
                            } else if (lookahead.equalsIgnoreCase("colon")) {
                                sfs = SymbolFoundState.EXPECTING_VARTYPE;
                            }
                            break;
                        case EXPECTING_VARTYPE:
                            if (lookahead.equalsIgnoreCase("id") || lookahead.equalsIgnoreCase("int_type") || lookahead.equalsIgnoreCase("float_type")) {
                                ((VarRecord) symbolRecord).setTypeName(tokenLiteral);

                                for (String symbolName : symbolNames) {
                                    varTable.insert(symbolName, ((VarRecord) symbolRecord));
                                }
                                symbolNames.clear();
                                sfs = SymbolFoundState.NONE;
                            }

                            break;
                        case EXPECTING_TYPENAME:
                            if (lookahead.equalsIgnoreCase("id")) {
                                symbolNames.add(symbolNames.size(), tokenLiteral);
                                sfs = SymbolFoundState.FOUND_TYPENAME;
                            }
                            break;
                        case FOUND_TYPENAME:
                            if (lookahead.equalsIgnoreCase("array")) {
                                sfs = SymbolFoundState.EXPECTING_ARRAYSIZE;
                            } else if (lookahead.equalsIgnoreCase("int_type")
                                    || lookahead.equalsIgnoreCase("float_type")
                                    || lookahead.equalsIgnoreCase("id")) {
                                TypeRecord superTypeRecord = typeTable.lookUp(tokenLiteral);
                                if (superTypeRecord != null) {
                                    ((TypeRecord) symbolRecord).setSuperType(superTypeRecord.getSuperType());
                                    ((TypeRecord) symbolRecord).setNumElements(superTypeRecord.getNumElements());
                                } else {
                                    System.out.println("No type found named " + tokenLiteral);
                                }
                                sfs = SymbolFoundState.POPULATE;
                            }
                            break;
                        case EXPECTING_ARRAYSIZE:
                            if (lookahead.equalsIgnoreCase("intlit")) {
                                ((TypeRecord) symbolRecord).setNumElements(Integer.parseInt(tokenLiteral));
                                sfs = SymbolFoundState.EXPECTING_ARRAYTYPE;
                            }
                            break;
                        case EXPECTING_ARRAYTYPE:
                            if (!lookahead.equalsIgnoreCase("rbrack") && !lookahead.equalsIgnoreCase("of")) { // We've advanced the input stream to the array type
                                
                                // Find expected type of array contents
                                TypeRecord arrayTypeRecord = typeTable.lookUp(tokenLiteral);

                                // Arrays can't contain arrays
                                if (tokenLiteral.equalsIgnoreCase("array")
                                || arrayTypeRecord.getSuperType().contains("array")) {
                                    System.out.println("Semantic error: cannot declare array of arrays");
                                } else if (arrayTypeRecord != null) {
                                    ((TypeRecord) symbolRecord).setSuperType("_array_" + arrayTypeRecord.getSuperType());
                                    ((TypeRecord) symbolRecord).setParentType("_array_" + arrayTypeRecord.getSuperType());
                                } else {
                                    System.out.println("No type found named " + tokenLiteral);
                                }
                            }
                            if (lookahead.equalsIgnoreCase("id") || lookahead.equalsIgnoreCase("int_type")
                                    || lookahead.equalsIgnoreCase("float_type") || lookahead.equalsIgnoreCase("array")) {

                                for (String symbolName : symbolNames) {
                                    typeTable.insert(symbolName, (TypeRecord) symbolRecord);
                                }
                                symbolNames.clear();
                                sfs = SymbolFoundState.NONE;
                            }
                            break;
                        default:
                            System.out.println("You broke the symbol table state machine :(");
                            break;

                    }

                    List<ParseTree> siblings = parseTree.getParent().getChildren();
                    int currLoc = siblings.indexOf(parseTree);

                    if (currLoc < siblings.size() - 1) {
                        if (isTerminal(parseTree.getSymbolName())) {
                            parseTree.setTokenLiteral(infileScanner.peekToken().getTokenLiteral());
                        }
                        parseTree = siblings.get(currLoc + 1);
                    } else {
                        int mostRecentChildNo = parseTree.childNo;
                        while(parseTree.childNo >= parseTree.getParent().getChildren().size() - 1) {
                            if (isTerminal(parseTree.getSymbolName())) {
                                parseTree.setTokenLiteral(infileScanner.peekToken().getTokenLiteral());
                            }
                            parseTree = parseTree.getParent();
                            mostRecentChildNo = parseTree.childNo;
                            if (parseTree.getParent() == null) {
                                break;
                            }
                        }
                        if (parseTree.getParent() != null) {
                            parseTree = parseTree.getParent().getChildren().get(mostRecentChildNo + 1);
                        }
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

                    for (int x = matchedRule.getExpansion().size() - 1; x >= 0; x--) {
                        String currLex = matchedRule.getExpansion().get(x);

                        if (isTerminal(currLex) && !currLex.equalsIgnoreCase("NIL")) {
                            parseTree.addChildren(currLex);
                        } else {
                            parseTree.addChildren(currLex);
                        }

                        stack.push(currLex);
                    }

                    parseTree = parseTree.getChildren().get(0);
                } else {
                    if (!lookahead.equals("SEMI")) {
                        parseTree.setTokenLiteral(tokenLiteral);
                        infileScanner.nextToken();
                    } else
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

        parseCompleted = true;
        return hasErrors;
    }


    private boolean isTerminal(String str) {
        return !str.contains("<");
    }

    private boolean isValidType(String symbolName, String tokenLiteral) {
        return symbolName.equalsIgnoreCase("int") || symbolName.equalsIgnoreCase("float")
                || symbolName.equalsIgnoreCase("array")
                || symbolName.equalsIgnoreCase("id") && typeTable.contains(tokenLiteral);
    }
}
