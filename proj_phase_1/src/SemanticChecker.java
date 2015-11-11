import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class SemanticChecker {
    public ParseTree parseTree;
    private VarTable varTable;
    private TypeTable typeTable;
    private FunctionTable functionTable;

    private String recentFuncDeclType;
    private boolean inFuncDec;

    public SemanticChecker(String fileName) {
        TigerParser tp = new TigerParser(new File(fileName));
        tp.parse();
        parseTree = tp.getParseTreeOld();
        varTable = tp.getVarTable();
        typeTable = tp.getTypeTable();
        functionTable = tp.getFunctionTable();
    }

    public boolean returnSemantic() {
        return checkSemantics(parseTree);
    }

    public boolean checkSemantics(ParseTree pt) {
        boolean isCorrect = true;
        List<ParseTree> children = pt.getChildren();

        if (children == null) {
            return true; // Terminals are semantically correct on their own
        } else switch (pt.getSymbolName()) {
            case "<type-declaration>":
                isCorrect = checkTypeDeclaration(pt);
                if (!isCorrect) {
                    System.out.println("fuck9");
                }
                break;
            // Check in case optional-init is being used
            case "<var-declaration>":
                isCorrect = isCorrect && checkVarDeclaration(pt);
                if (!isCorrect) {
                    System.out.println("fuck10");
                }
                break;

            case "<func-declaration>":
                isCorrect = isCorrect && checkFuncDeclaration(pt);
                if (!isCorrect) {
                    System.out.println("fuck11");
                }
                break;
            case "<stat>":
                isCorrect = isCorrect && checkStatsDeclaration(pt);
                if (!isCorrect) {
                    System.out.println("fuck12");
                }
                break;

            default:
                for (ParseTree child : children) {
                    isCorrect = isCorrect && checkSemantics(child);
                }
                break;
        }
        return isCorrect;
    }

    public boolean checkStatsDeclaration(ParseTree pt) {
        boolean isCorrect = true;

        List<ParseTree> children = pt.getChildren();
        ParseTree decision = children.get(0);
        switch (decision.getSymbolName()) {
            case "IF":
                ParseTree ifexpr = children.get(1);
                ParseTree statseq = children.get(3);
                ParseTree statelseseq = children.get(4);
                if (returnTypeExpr(ifexpr).equals(""))
                    isCorrect = false;
                if (!isCorrect) {
                    System.out.println("fuckif1");
                }
                isCorrect = isCorrect && checkStatsDeclaration(statseq);
                if (!isCorrect) {
                    System.out.println("fuckif2");
                }
                isCorrect = isCorrect && checkSemantics(statelseseq);
                if (!isCorrect) {
                    System.out.println("fuckif3");
                }
                break;

            case "WHILE":
                ifexpr = children.get(1);
                statseq = children.get(3);

                if (returnTypeExpr(ifexpr).equals(""))
                    isCorrect = false;
                isCorrect = isCorrect && checkSemantics(statseq);
                break;

            case "FOR":
                ParseTree firstexpr = children.get(3);
                ParseTree secondexpr = children.get(5);
                statseq = children.get(7);

                String firsttype = returnTypeExpr(firstexpr);
                String secondtype = returnTypeExpr(secondexpr);


                isCorrect = firsttype.equals(secondtype);
                isCorrect = isCorrect && checkSemantics(statseq);
                break;

            case "BREAK":
                break;

            case "RETURN":
                if (inFuncDec) {
                    String returntype = returnTypeExpr(children.get(1));
                    isCorrect = recentFuncDeclType.equals(returntype);
                    inFuncDec = false;
                } else {
                    isCorrect = false;
                }
                break;

            // Hard part
            case "ID":
                List<ParseTree> stat_id_tail = children.get(1).getChildren();
                ParseTree first = stat_id_tail.get(0);
                if (first.getSymbolName().equals("LPAREN")) { // Func
                    ParseTree func = decision;
//                    String funcName = func.getTokenLiteral();

                    ParseTree expr_list = stat_id_tail.get(1);
                    // get the list of arguments
                    List<ParseTree> exprs = new ArrayList<>();

                    exprs.add(expr_list.getChildren().get(0));
                    expr_list = expr_list.getChildren().get(1);

                    // Recursively get all the <expr>'s
                    while (expr_list.getChildren().size() != 1) {
                        exprs.add(expr_list.getChildren().get(1));
                        expr_list = expr_list.getChildren().get(2);
                    }

                    List<String> expr_types = exprs.stream().map(this::returnTypeExpr).collect(Collectors.toList());

                    FunctionRecord func_type = functionTable.lookUp(func.getTokenLiteral());
                    isCorrect = expr_types.equals(func_type.getParamTypes());
                    if (!isCorrect) {
                        System.out.println("fuck5");
                    }

                } else { // assign
                    ParseTree id = decision;
                    ParseTree stat_id_expr_tail = stat_id_tail.get(2);

                    List<ParseTree> twoexpr = stat_id_expr_tail.getChildren();

                    String returntype;
                    if (twoexpr.size() == 1) { // simple EXPR
                        returntype = returnTypeExpr(twoexpr.get(0));

                    } else { // possible function call
                        ParseTree assignID = twoexpr.get(0);
                        List<ParseTree> expr_func_tail = twoexpr.get(1).getChildren();
                        if (expr_func_tail.size() == 3) { // Assign with function call
                            ParseTree func = assignID;
                            String funcName = func.getTokenLiteral();

                            ParseTree expr_list = stat_id_tail.get(1);
                            // get the list of arguments
                            List<ParseTree> exprs = new ArrayList<>();

                            exprs.add(expr_func_tail.get(1).getChildren().get(0));
                            expr_list = expr_list.getChildren().get(1);

                            // Recursively get all the <expr>'s
                            while (expr_list.getChildren().size() != 1) {
                                exprs.add(expr_list.getChildren().get(1));
                                expr_list = expr_list.getChildren().get(2);
                            }

                            List<String> expr_types = exprs.stream().map(this::returnTypeExpr).collect(Collectors.toList());

                            FunctionRecord func_type = functionTable.lookUp(func.getTokenLiteral());
                            isCorrect = expr_types.equals(func_type.getParamTypes());
                            if (!isCorrect) {
                                System.out.println("fuck5");
                            }

                            String assigneeType = varTable.lookUp(decision.getTokenLiteral()).getTypeName();
                            isCorrect = isCorrect && functionTable.lookUp(funcName).getReturnType().equals(assigneeType);
                            if (!isCorrect) {
                                System.out.println("fuck4");
                            }
                        } else { // is now an arithmetic expression
                            ParseTree expr_lvalue_only = expr_func_tail.get(0);
                            ParseTree term_or_lvalue_only = expr_lvalue_only.getChildren().get(0);
                            ParseTree expr_tail = expr_lvalue_only.getChildren().get(1);
                            String expr_tail_type = returnTypeExpr(expr_tail); // next parts of expr
                            if (expr_tail.getChildren().size() != 1) {
                                System.out.println("fuck3");
                                isCorrect = varTable.lookUp(id.getTokenLiteral()).getTypeName().equals(expr_tail_type);
                            }

                            ParseTree term_and_lvalue_only = term_or_lvalue_only.getChildren().get(0);
                            ParseTree term_comp_lvalue_only = term_and_lvalue_only.getChildren().get(0);
                            ParseTree term_comp_tail = term_comp_lvalue_only.getChildren().get(1);
                            String term_comp_tail_type = returnTypeExpr(term_comp_tail); // op-add and shit
                            if (term_comp_tail.getChildren().size() != 1) {
                                System.out.println("fuck2");
                                isCorrect = isCorrect && varTable.lookUp(id.getTokenLiteral()).getTypeName().equals(term_comp_tail_type);
                            }

                            ParseTree term_lvalue_only = term_comp_lvalue_only.getChildren().get(0);
                            ParseTree lvalue_tail = term_lvalue_only.getChildren().get(0);
                            ParseTree term_tail = term_lvalue_only.getChildren().get(1);
                            String term_tail_type = returnTypeExpr(term_tail); // op-mul and shit
                            if (term_tail.getChildren().size() != 1) {
                                System.out.println("fuck1");
                                isCorrect = isCorrect && varTable.lookUp(id.getTokenLiteral()).getTypeName().equals(term_tail_type);
                            }

                            if (lvalue_tail.getChildren().get(0).getSymbolName().equals("LBRACK")) {
                                String lvalue_tail_lbrack_expr_type = returnTypeExpr(lvalue_tail.getChildren().get(1));
                                if (!lvalue_tail_lbrack_expr_type.equals("int")) {
                                    System.out.println("fuck");
                                    isCorrect = false;
                                }
                            }
                        }
                    }
                }
                break;
        }

        return isCorrect;
    }

    // TODO rename getTypeExpr

    /**
     *
     * @param pt
     * @return
     *
     */
    public String returnTypeExpr(ParseTree pt) {
        // Base case - return the explicit or implied type
        if (pt.getChildren() == null || pt.getChildren().size() == 0) {
//            assertTrue(ParseTree.isTerminal())
            String symbolName = pt.getSymbolName();

            if (symbolName.equalsIgnoreCase("id") && varTable.contains(pt.getTokenLiteral())) {
                return varTable.lookUp(pt.getTokenLiteral()).getTypeName();
            } else {
                return symbolName.equalsIgnoreCase("intlit") ? "int" :
                        symbolName.equalsIgnoreCase("floatlit") ? "float" :
                                symbolName.equalsIgnoreCase("_array_int") ? "int" :
                                        symbolName.equalsIgnoreCase("_array_float") ? "float" : "";
                // Empty string if there's no type info associated with the tree.
                // May be a nonterminal, delimiter, etc.
                // Equivalent to non-AST parse tree nodes.
            }
        }

        // Recursive case - accumulate entire subtree expansion to a single type
        String currType, nextType;
        currType = returnTypeExpr(pt.getChildren().get(0));
        boolean expectingArrayIndex = false;

        for (int i = 1; i < pt.getChildren().size(); i++) {
            if (pt.getChildren().get(i).getSymbolName().equals("lvalue-tail")) {
                List<ParseTree> potential_lval = pt.getChildren().get(i).getChildren();
                if (potential_lval.size() == 3) {
                    if (returnTypeExpr(potential_lval.get(1)).equals("int")) {
                        currType = "int";
                    } else {
                        currType = "";
                    }
                }
                continue;
            }

            nextType = returnTypeExpr(pt.getChildren().get(i)); // is one of {id , "intlit" "floatlit" or empty}

            if (nextType.isEmpty()) {
                continue;
            }

            if ((currType.equalsIgnoreCase("int") && nextType.equalsIgnoreCase("float"))
            || (currType.equalsIgnoreCase("float") && nextType.equalsIgnoreCase("int"))
                    || currType.equalsIgnoreCase("float") && nextType.equalsIgnoreCase("float")) {
                currType = "float";
            } else if (currType.equalsIgnoreCase("int") && nextType.equalsIgnoreCase("int")) {
                currType = "int";
            } else if (!currType.equalsIgnoreCase(nextType)) {
                currType = "";
            }
        }

        return currType;
    }

    public boolean checkAllTables(ParseTree pt) {
        int count = 0;
        if (varTable.contains(pt.getTokenLiteral()))
            count++;
        if(functionTable.contains(pt.getTokenLiteral()))
            count++;
        if(typeTable.contains(pt.getTokenLiteral()))
            count++;
        return count == 1;
    }

    public boolean checkTypeDeclaration(ParseTree pt) {
        List<ParseTree> children = pt.getChildren();
        ParseTree id = children.get(1);

        boolean isCorrect = checkAllTables(id);
        if (!isCorrect) {
            System.out.println("fuck6");
        }

        return isCorrect;
    }

    public boolean checkVarDeclaration(ParseTree pt) {
        List<ParseTree> children = pt.getChildren();

        // Check for possible duplicate IDs

        ParseTree idList = children.get(1);

        // Expand idList for potential list
        List<ParseTree> id_expansion = idList.getChildren();

        // Check first id
        ParseTree firstID = id_expansion.get(0);
        boolean isCorrect = checkAllTables(firstID);
        if (!isCorrect) {
            System.out.println("fuck8");
        }

        ParseTree id_list_tail = id_expansion.get(1);
        id_expansion = id_list_tail.getChildren();
        // Recursive check on potential list
        while(id_expansion.size() != 1) {
            ParseTree tempID = id_expansion.get(1);

            isCorrect = isCorrect && checkAllTables(tempID);
            if (!isCorrect) {
                System.out.println("fuck7");
            }

            id_expansion = id_expansion.get(2).getChildren();
        }


        //
        //	 Check type for optional initialization
        //

        // Find the type
        ParseTree type = children.get(3);
        List<ParseTree> type_expansion = type.getChildren();
        if (type_expansion.size() != 1) {
            type = type_expansion.get(5);
        } else {
            type = type_expansion.get(0);
        }
        type = type.getChildren().get(0);


        ParseTree opt_init = children.get(4);

        List<ParseTree> opt_init_expansion = opt_init.getChildren();
        if (opt_init_expansion.size() == 2) {
            ParseTree constant = opt_init_expansion.get(1);
            constant = constant.getChildren().get(0);


            String lhsType = convertLits(type.getTokenLiteral());
            String rhsType = convertLits(constant.getSymbolName());

            String lhsSuperType = typeTable.getSuperType(lhsType);
            String rhsSuperType = typeTable.getSuperType(rhsType);
            lhsSuperType = convertArrays(lhsSuperType);

            if (!lhsSuperType.equals(rhsSuperType)) {
                System.out.println("fuck");
                isCorrect = false;
            }
        }

        return isCorrect;
    }

    public String convertArrays(String str) {
        if (str.equals("_array_float"))
            return "float";
        if (str.equals("_array_int"))
            return "int";
        return str;
    }

    public String convertLits(String str) {
        if (str.equals("INTLIT"))
            return "int";
        if (str.equals("FLOATLIT"))
            return "float";
        else
            return str;
    }

    public boolean checkFuncDeclaration(ParseTree pt) {
        List<ParseTree> children = pt.getChildren();
        ParseTree id = children.get(1);
        ParseTree type = children.get(5);

        List<ParseTree> typechildren = type.getChildren();
        if (typechildren.size() != 1) {
            inFuncDec = true;
            ParseTree typent = typechildren.get(1);
            typent = typent.getChildren().get(0);
            typent = typent.getChildren().get(0);
            if (typent.getSymbolName().equals("ID")) {
                String tokenLiteral = typent.getTokenLiteral();
                recentFuncDeclType = varTable.lookUp(tokenLiteral).getTypeName();
            }
        }

        return checkAllTables(type) && checkAllTables(id) && checkSemantics(children.get(7));
    }
}