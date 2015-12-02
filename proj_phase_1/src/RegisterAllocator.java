import java.util.*;
import java.util.stream.Collectors;

public class RegisterAllocator {
    private IRParser irParser;
    private LivenessAnalyzer livenessAnalyzer;
    private GraphSolver graphSolver;
    private static final String LD_PREFIX = "LD", ST_PREFIX = "ST";

    public RegisterAllocator(String filename) {
        irParser = new IRParser(filename);
        livenessAnalyzer = new LivenessAnalyzer(filename);
        graphSolver = new GraphSolver(RegisterAllocationType.NAIVE, filename);
    }

    public List<String> getModifiedIR() {
        List<String> result = new LinkedList<>();
        BasicBlock[] orderedBasicBlocks = getBasicBlockOrdering(irParser.lineNoToBasicBlock());
        List<BasicBlock> mylist = Arrays.asList(orderedBasicBlocks);
        mylist.forEach(bb -> result.addAll(injectRegNames(bb)));
        return result;
    }

    public BasicBlock[] getBasicBlockOrdering(Map<Integer, BasicBlock> lineNoToBasicBlock) {
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


    public List<String> injectRegNames(BasicBlock basicBlock) {
        List<String> instrsWithRegNames = new LinkedList<>();
        List<Instruction> instructions = basicBlock.instructions;
        Set<String> inSet = livenessAnalyzer.getInSets().get(basicBlock);
        Set<String> outSet = livenessAnalyzer.getOutSets().get(basicBlock);

        // Load all the values in the inset
        instrsWithRegNames.addAll(inSet.stream()
                .map(inVal -> LD_PREFIX + ", " + inVal + ", " + graphSolver.getReg(inVal))
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
        instrsWithRegNames.addAll(outSet.stream()
                .map(outVal -> ST_PREFIX + ", " + graphSolver.getReg(outVal) + ", " + outVal)
                .collect(Collectors.toList()));

        return instrsWithRegNames;
    }
}
