import java.util.*;

public class ParseTree {
    private ParseTree parent;
    private String symbolName;

    public String getTokenLiteral() {
        return tokenLiteral;
    }

    private String tokenLiteral;

    private String scopeName;

    public int childNo;

    private List<ParseTree> children;

    private boolean visited = false;

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

    public ParseTree getFunctionDeclarationTree() {
        ParseTree functionDeclarationTree = null;

        if (functionDeclarationTree == null) {

        }

        return functionDeclarationTree;
    }

    public ParseTree getVarDeclarationTree() {
        ParseTree varDeclarationTree = null;

        if (varDeclarationTree == null) {
            // var declaration not found

        }

        return varDeclarationTree;
    }

    public ParseTree generateAST() {
        if (children.size() == 1 && !visited) {
            if (children.get(0).getSymbolName().equalsIgnoreCase("nil")) {
                children = new ArrayList<>();
            } else if (parent != null)  {
                parent.addChildren(childNo, children.get(0));
            }
            visited = true;
        } else {
            for (int i = 0; children != null && i < children.size(); i++)  {
                children.set(i, children.get(i).generateAST());
            }
        }


        return this;
    }
}

