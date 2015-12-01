import java.util.Set;

public class BasicBlock implements Comparable<BasicBlock> {
    public Set<BasicBlock> successors, predecessors;
    public  int startingLine, endingLine;

    public int degree() {
        return successors.size() + predecessors.size();
    }

    @Override
    public int compareTo(BasicBlock bb) {
        return this.successors.size() - bb.successors.size();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof BasicBlock
                && ((BasicBlock) o).successors.equals(this.successors)
                && ((BasicBlock) o).startingLine == this.startingLine
                && ((BasicBlock) o).endingLine == this.endingLine;
    }
}