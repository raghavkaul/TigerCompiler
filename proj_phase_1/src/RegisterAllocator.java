import java.util.*;
import java.util.stream.Collectors;

public class RegisterAllocator {
    private IRParser irParser;
    private LivenessAnalyzer livenessAnalyzer;
    private GraphSolver graphSolver;
    private static final String LD_PREFIX = "LD", ST_PREFIX = "ST";

    public RegisterAllocator(String filename, RegisterAllocationType rat) {
        irParser = new IRParser(filename);
        livenessAnalyzer = new LivenessAnalyzer(filename);
        graphSolver = new GraphSolver(rat, filename);
    }

    public void setRegisterAllocationType(RegisterAllocationType rat) {
        graphSolver.setRegisterAllocationType(rat);
    }

    public List<String> getModifiedIR() {
        List<String> result = new LinkedList<>();
        BasicBlock[] orderedBasicBlocks = getBasicBlockOrdering(irParser.lineNoToBasicBlock());
        List<BasicBlock> mylist = Arrays.asList(orderedBasicBlocks);
        mylist.forEach(bb -> result.addAll(allocateRegsCFG(bb)));
        return result;
    }

    private BasicBlock[] getBasicBlockOrdering(Map<Integer, BasicBlock> lineNoToBasicBlock) {
        Set<BasicBlock> tempSet = new HashSet<>();
        tempSet.addAll(lineNoToBasicBlock.values());
        BasicBlock[] result = new BasicBlock[tempSet.size()];
        BasicBlock currBlock = lineNoToBasicBlock.get(0);

        for (int i = 0, j = 0; i < lineNoToBasicBlock.entrySet().size(); i++) {
            BasicBlock nextBlock = lineNoToBasicBlock.get(i);

            if (!currBlock.equals(nextBlock)) {
                result[j++] = currBlock;
                currBlock = nextBlock;
            }
        }

        return result;
    }

    private List<String> allocateRegsCFG(BasicBlock basicBlock) {
        graphSolver.setRegisterAllocationType(RegisterAllocationType.INTRA_BLOCK);

        List<String> instrsWithRegNames = new LinkedList<>();
        List<Instruction> instructions = basicBlock.instructions;
        Set<String> defSet = livenessAnalyzer.getDefSets().get(basicBlock),
                useSet = livenessAnalyzer.getUseSets().get(basicBlock);

        instrsWithRegNames.addAll(useSet.stream()
                .map(useVal -> LD_PREFIX + ", " + useVal + ", " + graphSolver.getReg(useVal))
                .collect(Collectors.toList()));

        for (Instruction instruction : instructions) {
            List<String> srcRegs = instruction.getSourceRegs();
            String destReg = instruction.getDestinationReg();

            String currInstrWithRegs = instruction.getOpName() + ", ";

            for (String srcReg : srcRegs) {
                currInstrWithRegs += graphSolver.getReg(srcReg) + ", ";
            }

            currInstrWithRegs += graphSolver.getReg(destReg);
            instrsWithRegNames.add(currInstrWithRegs);
        }

        // Store all values in the outset
        instrsWithRegNames.addAll(defSet.stream()
                .map(defVal -> ST_PREFIX + ", " + graphSolver.getReg(defVal) + ", " + defVal)
                .collect(Collectors.toList()));

        return instrsWithRegNames;

    }
}
