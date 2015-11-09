import java.util.*;

public class ParseTree {
    private ParseTree parent;
    private String symbolName;

    private String tokenLiteral;

    private String scopeName;
    public int childNo;
    private List<ParseTree> children;
    public ParseTree(String name) {
        children = new ArrayList<>();
        this.symbolName = name;
    }

    public ParseTree(String name, Token tokenLiteral) {
        children = new ArrayList<>();
        this.symbolName = name;
        this.tokenLiteral = tokenLiteral.getToken();
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

    public List<ParseTree> getChildren() {
        return children;
    }

    public void addChildren(String symbolName, String tokenLiteral)  {
        ParseTree child = new ParseTree(symbolName);
        child.setParent(this);
        child.tokenLiteral = tokenLiteral;
        children.add(0, child);

        for (int i = 0; i < children.size(); i++) {
            children.get(i).childNo = i;
        }
    }


    public ParseTree getParent() {
        return parent;
    }

    public void setParent(ParseTree parent) {
        this.parent = parent;
    }

    public String getSymbolName() {
        return symbolName;
    }

    public String getTokenLiteral() { return tokenLiteral; }


    @Override
    public String toString() {
        String result = "Tree : " + symbolName + " :: Children [" + children.size() + "]: {";

        for (ParseTree child : children) {
            result += child.getSymbolName() + ", ";
        }

        return result + "}";


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
