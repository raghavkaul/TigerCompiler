import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SemanticChecker {
    private ParseTree parseTree;
    private VarTable varTable;
    private TypeTable typeTable;
    private FunctionTable functionTable;

    public SemanticChecker(String fileName) {
        TigerParser tp = new TigerParser(new File(fileName));
        parseTree = tp.getParseTree();
        varTable = tp.getVarTable();
        typeTable = tp.getTypeTable();
        functionTable = tp.getFunctionTable();
    }

    public static ParseTree populateIntermediates(ParseTree pt) {
        List<ParseTree> children = pt.getChildren();

        for (int i = 0; i < children.size(); i++) {
            ParseTree child = children.get(i);

            pt.updateChild(i, populateIntermediates(child));
        }

        return pt;
    }

    public boolean checkSemantics(ParseTree pt) {
        boolean isCorrect = true;

        List<ParseTree> children = pt.getChildren();

        if (children == null) {
            return isCorrect; // Terminals are semantically correct on their own
        } else {
            switch (pt.getSymbolName()) {
                case "<type-declaration>":
                    isCorrect = checkTypeDeclaration(pt) && isCorrect;
                    break;
                // Check in case optional-init is being used
                case "<var-declaration>":
                    isCorrect = checkVarDeclaration(pt) && isCorrect;
                    break;

                case "<func-declaration>":
                    isCorrect = checkFuncDeclaration(pt) && isCorrect;
                    break;
                case "<stats>":
                    isCorrect = checkStatsDeclaration(pt) && isCorrect;
                    break;

                default:
                    for (ParseTree child : children) {
                        isCorrect = checkSemantics(child) && isCorrect;
                    }
            }
        }
        return isCorrect;
    }

    public boolean checkStatsDeclaration(ParseTree pt) {
        boolean isCorrect = true;

        List<ParseTree> children = pt.getChildren();
        ParseTree decision = children.get(0);
        switch (decision.getSymbolName()) {
            case "IF":

                break;

            case "WHILE":

                break;

            case "FOR":

                break;

            case "BREAK":

                break;

            case "RETURN":

                break;

            // Hard part
            case "ID":

                break;
        }

        return isCorrect;
    }

    public boolean checkIFExpr(ParseTree pt) {
        boolean isCorrect = true;

        return isCorrect;
    }

    public boolean checkExpr(ParseTree pt) {
        boolean isCorrect = true;

        return isCorrect;
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

        return checkAllTables(id);
    }

    private boolean isAssignOp(String token) {
        return token.equals("ASSIGN");
    }

    /**
     * Checks the agreement of binary operands in an expression
     * @param expression
     * @return
     */
    private boolean checkBinopAgreement(List<ParseTree> expression) {
        int binopLocation = -1;
        for (int i = 0; i < expression.size(); i++) {
            if (isBinaryOp(expression.get(i).getSymbolName())) {
                binopLocation = i;
            }
        }

        if (binopLocation != -1) {
            String lhs, rhs, lhsType, rhsType, lhsSuperType, rhsSuperType;

            lhs = expression.get(binopLocation - 1).getSymbolName();
            rhs = expression.get(binopLocation + 1).getSymbolName();

            lhsType = varTable.lookUp(lhs).getTypeName();
            rhsType = varTable.lookUp(rhs).getTypeName();

            lhsSuperType = typeTable.lookUp(lhsType).getSuperType();
            rhsSuperType = typeTable.lookUp(rhsType).getSuperType();

            return lhsSuperType.equalsIgnoreCase(rhsSuperType);
        }

        return true;
    }

    private static boolean isBinaryOp(String token) {
        String[] binaryOpsArr = {"PLUS", "MINUS", "AND", "OR",
                "EQ", "NEQ", "LESSER", "GREATER", "LESSEREQ", "GREATEREQ"};

        Set<String> binaryOps = new HashSet<>(Arrays.asList(binaryOpsArr));

        return binaryOps.contains(token);
    }
}