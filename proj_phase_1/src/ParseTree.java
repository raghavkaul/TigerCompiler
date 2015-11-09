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
        }
    }

    public void addChildren(String symbolName)  {
        ParseTree child = new ParseTree(symbolName);
        child.setParent(this);
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
        String result = tokenLiteral + "::" + symbolName + " :: Children (" + children.size() + "): {";

        for (ParseTree child : children) {
            result += child.getSymbolName() + ", ";
        }

        return result + "}";


    }

    public void setTokenLiteral(String tokenLiteral) {
        this.tokenLiteral = tokenLiteral;
    }

    public String getScopeName() {
        return scopeName;
    }

    public void setScopeName(String scopeName) {
        this.scopeName = scopeName;
    }

    public void print() {
        print("", true);
    }

    private void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") +
                (tokenLiteral == null ? "<>" : "\"" + tokenLiteral +"\"")
                + " :: " + symbolName);

        for (int i = 0; i < children.size() - 1; i++) {
            children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (children.size() > 0) {
            children.get(children.size() - 1).print(prefix + (isTail ?"    " : "│   "), true);
        }
    }
}
