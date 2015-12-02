import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class IRParser {
    private Scanner irFileScanner;
    private File irFile;
    private Set<String> virtualRegs;
    private List<Instruction> instructions;
    public int numInstructions;

    public IRParser(String infileName) {
        irFile = new File(infileName);
        try {
            irFileScanner = new Scanner(irFile);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }

        virtualRegs = generateVirtualRegs();
        instructions = generateInstructions();
    }

    /**
     * Lists the names of all possible virtual registers
     * @return set of strings of virtual reg names
     */
    private Set<String> generateVirtualRegs() {
        Set<String> virtualRegs = new HashSet<>();

        String[][] regsByProgPt = getRegsByProgPt();

        for (String[] sa : regsByProgPt) {
            Collections.addAll(virtualRegs, sa);
        }

        return virtualRegs;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    private List<Instruction> generateInstructions() {
        List<Instruction> instructions = new ArrayList<>();
        while (irFileScanner.hasNextLine()) {
            Instruction i = new Instruction(irFileScanner.nextLine());
            instructions.add(i);
        }

        resetScanner();

        return instructions;
    }

    private String[] getNextInstr() {
        return parseInstr(irFileScanner.nextLine());
    }

    /**
     * Splits the IR-form instruction into constituent parts
     * @param instr to parse
     * @return string array [OPCODE, SR1, SR2, DR]
     */
    private String[] parseInstr(String instr) { return instr.split(":")[1].split("[\\s,]"); }

    /**
     * Gets array of virtual registers, indexed by IR program points
     * @return String[][] of registers at each line of IR code
     */
    public String[][] getRegsByProgPt() {
        String[][] regs = {};

        for (int i = 0; irFileScanner.hasNextLine(); i++) {
            String[] instruction = getNextInstr();
            String op = instruction[0];

            // Isolate operands from operation name
            String[] virtualRegs = Arrays.copyOfRange(instruction,1,instruction.length);

            // Remove literal ints/floats, which don't need a register
            virtualRegs = Arrays.stream(virtualRegs).filter(this::isNamedReg).toArray(String[]::new);

            // Save result
            regs[i] = virtualRegs;
        }

        resetScanner();

        return regs;
    }

    /**
     * Takes each line of IR code and maps it to an integer Block # based
     * on block "leaders"
     * @return map from line number -> corresponding block number
     */
    public Map<Integer, Integer> getIRBlocks() { // TODO not sure if return type makes sense
        Map<Integer, Integer> irLineToBlock = new HashMap<>();

        // For each leader its basic block consists of itself and all
        // instructions until the next leader or end of IR, exclusive
        for (int lineNo = 0, blockNo = 0; irFileScanner.hasNextLine(); lineNo++) {
            String line = irFileScanner.nextLine();

            if (isBranchInstr(line) || isBranchTarget(line)) {
                // We've found a leader, implying a new block
                blockNo++;
            }

            irLineToBlock.put(lineNo,blockNo);
        }

        resetScanner();

        return irLineToBlock;
    }

    public List<BasicBlock> getBasicBlockAdjList() {
        Map<String, BasicBlock> labels = new HashMap<>();
        List<BasicBlock> basicBlockAdjList = new LinkedList<>();
        BasicBlock currBlock = new BasicBlock();
        currBlock.startingLine = 0;

        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);

            if (instruction.isBranchInstruction()) {
                currBlock.endingLine = i;

                String target = instruction.getBranchLabel();
                labels.put(target, currBlock);

                basicBlockAdjList.add(currBlock);

                currBlock = new BasicBlock();
                currBlock.startingLine = i + 1;
            } else if (instruction.isBranchTarget()) {
                String label = instruction.getLabel();
                BasicBlock predecessor = labels.get(label);
                predecessor.successors.add(currBlock);
            }
        }

        return basicBlockAdjList;
    }

    public Map<Integer, BasicBlock> lineNoToBasicBlock() {
        Map<Integer, BasicBlock> lineToBasicBlock = new HashMap<>();
        List<BasicBlock> basicBlockAdjList = getBasicBlockAdjList();

        for (BasicBlock basicBlock : basicBlockAdjList) {
            for (int i = basicBlock.startingLine; i < basicBlock.endingLine; i++) {
                lineToBasicBlock.put(i, basicBlock);
            }
        }

        return lineToBasicBlock;
    }

    /**
     * A variable "reaches" block i if a definition or use of the variable
     * reaches the basic block along the edges of the CFG
     * A variable is "live" at block i if there is a direct reference to
     * the variable at block i or at some block that succeeds i in the CFG
     * provided the variable is not redefined.
     * @return
     */
    public Map<String, LiveRange> getLiveRanges() {
        List<Map<String, VarUseBlock>> livenessAndNextUse = getLivenessAndNextUse();
        Map<Integer, BasicBlock> lineNoToBasicBlock = lineNoToBasicBlock();
        Map<String, LiveRange> liveRanges = new HashMap<>();

        for (int i = 0; i < instructions.size(); i++) {
            BasicBlock currentBb = lineNoToBasicBlock.get(i);
            Set<String> currentlyLiveVars = livenessAndNextUse.get(i).entrySet()
                    .stream().filter(me -> me.getValue().live)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            for (String currentlyLiveVar : currentlyLiveVars) {
                LiveRange currLiveRange = liveRanges.get(currentlyLiveVar);
                currLiveRange.addBbToRange(currentBb);
                liveRanges.put(currentlyLiveVar, currLiveRange);
            }
        }

        return liveRanges;
    }

    class LiveRange {
        Set<BasicBlock> range;

        LiveRange() {
            range = new HashSet<>();
        }

        boolean addBbToRange(BasicBlock bb) {
            return range.add(bb);
        }
    }

    // Not sure if this is useful.
    public List<Map<String,VarUseBlock>> getLivenessAndNextUse() {
        // Read file backwards from last line
        List<String> linesInReverse = getLinesInReverse();

        // Liveness and next use info for each virtual register, at each line in the IR
        List<Map<String,VarUseBlock>> result = new LinkedList<>();

        // Bookkeeping table to keep running track of each virtual register
        Map<String, VarUseBlock> table = new HashMap<>();

        // Populating bookkeeping table
        virtualRegs.forEach(virtualReg -> table.put(virtualReg, new VarUseBlock(true, linesInReverse.size() - 1)));


        for (int i = 0; i < linesInReverse.size(); i++) {
            String statement = linesInReverse.get(i);

            Map<String, VarUseBlock> varUse = new HashMap<>();

            for (String virtualReg : virtualRegs) {
                // Attach information currently in the bookkeeping table
                varUse.put(virtualReg, table.get(virtualReg));
            }

            String[] parsedInstr = parseInstr(statement);
            String destReg = parsedInstr[parsedInstr.length - 1];
            String[] sourceRegs = Arrays.copyOfRange(parsedInstr, 0, parsedInstr.length - 1);

            // Set the destination to "not live" and "no next use"
            // i.e. use previous next-use info
            varUse.put(destReg, new VarUseBlock(false, table.get(destReg).nextUse));

            // Set source registers to "live" and the next uses to i
            for (String sourceReg : sourceRegs) {
                varUse.put(sourceReg, new VarUseBlock(true, i));
            }

            result.add(i, varUse);
        }

        return result;
    }

    class VarUseBlock {
        boolean live;
        int nextUse;

        public VarUseBlock(boolean live, int nextUse) {
            this.live = live;
            this.nextUse = nextUse;
        }
    }

    // For the liveness/next use info method
    private List<String> getLinesInReverse() {
        List<String> file = new LinkedList<>();

        while (irFileScanner.hasNextLine()) {
            file.add(0, irFileScanner.nextLine());
        }

        return file;
    }

    // Resets global state
    private void resetScanner() {
        try {
            irFileScanner = new Scanner(irFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean isNamedReg(String operand) {
        try {
            Double.parseDouble(operand);
        } catch (NumberFormatException nfe) {
            return true;
        }

        return false;
    }

    private boolean isBranchInstr(String instr) {
        final String[] branchInstrs = {"goto", "breq", "brneq","brlt",
                "brgt", "brgeq", "brleq", "return", "call"};

        for (String branchInstr : branchInstrs) {
            if (instr.contains(branchInstr)) return true;
        }

        return false;
    }

    private boolean isBranchTarget(String instr) {
        // special syntax token implying the instruction contains a label
        return instr.contains(":");
    }
}