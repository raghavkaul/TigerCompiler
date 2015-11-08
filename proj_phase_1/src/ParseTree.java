import java.util.*;

public class ParseTree {
    private ParseTree parent;
    private String symbolName;
    private SymbolRecord symbolRecord;
    private List<ParseTree> children;
    private int childNo;

    public ParseTree(String name) {
        children = new ArrayList<>();
        this.symbolName = name;
    }

    // Utility acessors/mutators
    public boolean updateChild(int childNo, ParseTree newChild) {
        if (childNo < children.size()) {
            children.add(childNo, newChild);
            children.remove(childNo + 1);
            return true;
        }
        return false;
    }

    public void addChildren(String name)  {
        ParseTree child = new ParseTree(name);
        child.setParent(this);
        child.childNo = children.size();
        children.add(0, child);
    }

    public String getSymbolName() {
        return symbolName;
    }

    public int getChildNo() {
        return childNo;
    }

    public List<ParseTree> getChildren() {
        return children;
    }

    public ParseTree getParent() {
        return parent;
    }

    public void setParent(ParseTree parent) {
        this.parent = parent;
    }

    // Type checking
    public static ParseTree populateIntermediates(ParseTree pt) {
        List<ParseTree> children = pt.getChildren();

        for (int i = 0; i < children.size(); i++) {
            ParseTree child = children.get(i);

            pt.updateChild(i, populateIntermediates(child));
        }

        return pt;
    }

//    public static boolean checkSemantics(ParseTree pt) {
//        boolean isCorrect = true;
//
//        if (pt.getChildren() == null) {
//            // Terminals are semantically correct on their own
//            return true;
//        } else {
//            int binopLocation = -1;
//            for (int i = 0; i < pt.getChildren().size(); i++) {
//                if (isBinaryOp(pt.getChildren().get(i).tokenType)) {
//                    binopLocation = i;
//                }
//            }
//
//            if (!pt.getChildren().get(binopLocation - 1).reducibleType.equalsIgnoreCase(
//                    pt.getChildren().get(binopLocation + 1).reducibleType
//            )) {
//                isCorrect = false;
//            }
//            for (ParseTree child : pt.getChildren()) {
//                isCorrect = checkSemantics(child) && isCorrect;
//            }
//            return isCorrect;
//        }
//
//
//    }

    // Utility methods
    private static boolean isBinaryOp(String token) {
        String[] binaryOpsArr = {"PLUS", "MINUS", "AND", "OR",
                "EQ", "NEQ", "LESSER", "GREATER", "LESSEREQ", "GREATEREQ"};

        Set<String> binaryOps = new HashSet<>(Arrays.asList(binaryOpsArr));

        return binaryOps.contains(token);
    }

    public void print() {
        print("", true);
    }

    private void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + symbolName);
        for (int i = 0; i < children.size() - 1; i++) {
            children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (children.size() > 0) {
            children.get(children.size() - 1).print(prefix + (isTail ?"    " : "│   "), true);
        }
    }
}
