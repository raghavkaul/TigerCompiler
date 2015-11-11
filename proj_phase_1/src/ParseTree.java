import java.util.*;

public class ParseTree {
    private ParseTree parent;
    private String symbolName;
    private static int ID = 0;
    private int myID;

    public String getTokenLiteral() {
        return tokenLiteral;
    }

    private String tokenLiteral;

    public int childNo;

    private List<ParseTree> children;

    public ParseTree(String name) {
        children = new ArrayList<>();
        this.symbolName = name;
        this.myID = ID++;
    }
    public ParseTree(String name, Token tokenLiteral) {
        this.myID = ID++;
        children = new ArrayList<>();
        this.symbolName = name;
        this.tokenLiteral = tokenLiteral.getTokenLiteral();
    }

    // Utility acessors/mutators
    public List<ParseTree> getChildren() {
        return children;
    }

    public void addChildren(String symbolName, Token tokenLiteral) {
        ParseTree child = new ParseTree(symbolName, tokenLiteral);
        child.setParent(this);
        children.add(0, child);

        for (int i = 0; i < children.size(); i++) {
            children.get(i).childNo = i;
            children.get(i).myID = ID++;
        }
    }

    public void addChildren(String symbolName)  {
        ParseTree child = new ParseTree(symbolName);
        child.setParent(this);
        children.add(0, child);

        for (int i = 0; i < children.size(); i++) {
            children.get(i).childNo = i;
            children.get(i).myID = ID++;
        }
    }

    public void addChildren(int location, ParseTree newChild) {
        children.add(location, newChild);
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

    @Override
    public String toString() {
        String result = "Literal: \"" + tokenLiteral + "\"  ID: "
                + myID + "  Type: " + symbolName + "  Children (" + children.size() + "): {";

        for (ParseTree child : children) {
            result += child.getSymbolName() + ", ";
        }

        return result + "}";


    }

    public void setTokenLiteral(String tokenLiteral) {
        this.tokenLiteral = tokenLiteral;
    }

    public void print() {
        print("", true);
    }

    private void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") +
                (tokenLiteral == null ? "\"\"" : "\"" + tokenLiteral +"\"")
                + " :: " + symbolName + " " + myID);

        for (int i = 0; i < children.size() - 1; i++) {
            if (children.get(i) != null) {
                children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
            }
        }
        if (children.size() > 0) {
            if (children.get(children.size() - 1) != null) {
                children.get(children.size() - 1).print(prefix + (isTail ?"    " : "│   "), true);
            }
        }
    }
}

