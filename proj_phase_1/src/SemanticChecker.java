import java.io.File;
import java.util.*;

public class SemanticChecker {
    private ParseTree parseTree;
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
                break;
            // Check in case optional-init is being used
            case "<var-declaration>":
                isCorrect = checkVarDeclaration(pt);
                break;

            case "<func-declaration>":
                isCorrect = checkFuncDeclaration(pt);
                break;
            case "<stats>":
                isCorrect = checkStatsDeclaration(pt);
                break;

            default:
                for (ParseTree child : children) {
                    isCorrect = checkSemantics(child) && isCorrect;
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

                //TODO
                break;

            case "WHILE":
                ifexpr = children.get(1);
                statseq = children.get(3);

                isCorrect = checkIFExpr(ifexpr);
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
                    String funcName = func.getTokenLiteral();

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

                    List<String> expr_types = new ArrayList<>();

                    for (ParseTree expr : exprs) {
                        expr_types.add(returnTypeExpr(expr));
                    }

                    FunctionRecord func_type = functionTable.lookUp(func.getTokenLiteral());
                    isCorrect = expr_types.equals(func_type.getParamTypes());

                } else { // assign
                    ParseTree id = decision;

                }
                break;
        }

        return isCorrect;
    }

    public boolean checkIFExpr(ParseTree pt) {
        boolean isCorrect = true;

        return isCorrect;
    }

    public String returnTypeExpr(ParseTree pt) {


        return null;
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
        boolean isCorrect = true;

        List<ParseTree> children = pt.getChildren();
        ParseTree id = children.get(1);

        isCorrect = isCorrect && checkAllTables(id);

        return isCorrect;
    }

    public boolean checkVarDeclaration(ParseTree pt) {
        boolean isCorrect = true;
        List<ParseTree> children = pt.getChildren();


        //
        //	Check for possible duplicate ids
        //

        ParseTree idList = children.get(1);

        // Expand idList for potential list
        List<ParseTree> id_expansion = idList.getChildren();

        // Check first id
        ParseTree firstID = id_expansion.get(0);
        isCorrect = isCorrect && checkAllTables(firstID);

        ParseTree id_list_tail = id_expansion.get(1);
        id_expansion = id_list_tail.getChildren();
        // Recursive check on potential list
        while(id_expansion.size() != 1) {
            ParseTree tempID = id_expansion.get(1);

            isCorrect = isCorrect && checkAllTables(tempID);

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
            String rhsType = convertLits(constant.getTokenLiteral());

            String lhsSuperType = typeTable.getSuperType(lhsType);
            String rhsSuperType = typeTable.getSuperType(rhsType);
            lhsSuperType = convertArrays(lhsSuperType);

            if (!lhsSuperType.equals(rhsSuperType))
                isCorrect = false;
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

        inFuncDec = true;

        recentFuncDeclType = type.getTokenLiteral();
        return checkAllTables(type) && checkAllTables(id) && checkSemantics(children.get(7));
    }

}