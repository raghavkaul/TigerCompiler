import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raghav K on 10/31/15.
 */
public class ParseTree {
    private ParseTree parent;
    private Token data;
    private List<ParseTree> children;

    public ParseTree() {
        children = new ArrayList<>();
    }

    public ParseTree(Token data) {
        super();
        this.data = data;
    }

    public void addChildren(Token... tokens) {
        for (Token t : tokens) {
            ParseTree child = new ParseTree(t);
            child.setParent(this);
            children.add(child);
        }
    }

    public List<ParseTree> getChildren() {
        return children;
    }

    public Token getData() {
        return data;
    }

    public void setData(Token data) {
        this.data = data;
    }

    public ParseTree getParent() {
        return parent;
    }

    public void setParent(ParseTree parent) {
        this.parent = parent;
    }
}
