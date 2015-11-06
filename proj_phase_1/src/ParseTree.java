import java.util.*;

/**
 * Created by Raghav K on 10/31/15.
 */
public class ParseTree {
    private ParseTree parent;
    private String name;
    private List<ParseTree> children;
    private int childNo;

    public int getChildNo() {
        return childNo;
    }

    public ParseTree() {
        children = new ArrayList<>();
    }

    public ParseTree(String name) {
        children = new ArrayList<>();
        this.name = name;
    }

    public void addChildren(Collection<String> names) {
        int i = 0;
        for (String t : names) {
            ParseTree child = new ParseTree(t);
            child.setParent(this);
            child.childNo = i++;
            children.add(child);
        }
    }

    public boolean updateChild(int childNo, ParseTree newChild) {
        if (childNo < children.size()) {
            children.add(childNo, newChild);
            children.remove(childNo + 1);
        }
        return false;
    }
    public void addChildren(String name)  {
        ParseTree child = new ParseTree(name);
        child.setParent(this);
//        System.out.println(children);
        child.childNo = children.size();
        children.add(0, child);
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

    public void levelOrderPrint() {
        Queue<ParseTree> q = new LinkedList<>();
        q.add(this);
        while(!q.isEmpty()) {
            ParseTree temp = q.poll();
            System.out.println(temp.name);
            for (ParseTree child : children) {
                if (child != null) {
                    q.add(child);
                }
            }
        }
    }

    public static ParseTree populateIntermediates(ParseTree pt) {
        List<ParseTree> children = pt.getChildren();

        for (int i = 0; i < children.size(); i++) {
            ParseTree child = children.get(i);



            pt.updateChild(i, populateIntermediates(child));
        }

        return pt;
    }

    public static boolean checkSemantics(ParseTree pt) {
        boolean isCorrect = true;

return false; //TODO
    }

    public boolean isBinaryOp(String token) {
        String[] binaryOpsArr = {"PLUS", "MINUS", "AND", "OR",
                "EQ", "NEQ", "LESSER", "GREATER", "LESSEREQ", "GREATEREQ"};
        Set<String> binaryOps = new HashSet<>(Arrays.asList(binaryOpsArr));

        return binaryOps.contains(token);
    }

}
