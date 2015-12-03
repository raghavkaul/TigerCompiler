import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BasicBlock implements Comparable<BasicBlock> {
    public Set<BasicBlock> successors;
    public  int startingLine, endingLine;
    public List<Instruction> instructions = new LinkedList<>();

    public void addInstruction(Instruction i) {
        instructions.add(i);
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

    //    \pi(k_1,k_2) := \frac{1}{2}(k_1 + k_2)(k_1 + k_2 + 1)+k_2.
    @Override
    public int hashCode() {
        return (startingLine + endingLine) * (startingLine + endingLine
         + 1) + endingLine;
    }
}