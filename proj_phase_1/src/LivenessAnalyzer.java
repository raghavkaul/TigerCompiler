import java.util.*;

public class LivenessAnalyzer {
    private IRParser irParser;
    private Map<BasicBlock, Set<String>> inSets, outSets, defSets, useSets;
    private List<Set<String>> inSetsByLine, defSetsByLine, useSetsByLine;
    private Map<Integer, BasicBlock> lineNoToBasicBlock;
    private List<BasicBlock> basicBlockAdjList;
    private List<Instruction> instructionList;
    private boolean defUseSetsGenerated = false, defUseSetsByLineGenerated = false;

    public LivenessAnalyzer(String filename) {
        irParser = new IRParser(filename);

        inSets = new HashMap<>();
        outSets = new HashMap<>();
        defSets = new HashMap<>();
        useSets = new HashMap<>();

        this.basicBlockAdjList = irParser.getBasicBlockAdjList();
        this.lineNoToBasicBlock = irParser.lineNoToBasicBlock();
        this.instructionList = irParser.getInstructions();

        for (BasicBlock bb : basicBlockAdjList) {
            inSets.put(bb, new HashSet<>());
            outSets.put(bb, new HashSet<>());
            defSets.put(bb, new HashSet<>());
            useSets.put(bb, new HashSet<>());
        }

        generateDefUseSets();
        generateInOutSets();
    }

    public IRParser getIrParser() { return irParser; }

    public Map<BasicBlock, Set<String>> getInSets() {
        return inSets;
    }

    public Map<BasicBlock, Set<String>> getOutSets() {
        return outSets;
    }

    public Map<BasicBlock, Set<String>> getDefSets() {
        return defSets;
    }

    public Map<BasicBlock, Set<String>> getUseSets() {
        return useSets;
    }

    /**
     * Updates global def and use sets to their correct value. Prerequisite
     * to getting insets and outsets.
     */
    private void generateDefUseSets() {
        Map<BasicBlock, Set<String>> defSets = new HashMap<>(),
                useSets = new HashMap<>();

        for (int i = 0; i < instructionList.size(); i++) {
            Instruction instruction = instructionList.get(i);
            BasicBlock bb = lineNoToBasicBlock.get(i);
            Set<String> currDefSet = defSets.get(bb);
            Set<String> currUseSet = useSets.get(bb);

            String destinationReg = instruction.getDestinationReg();
            List<String> sourceRegs = instruction.getSourceRegs();

            // def[B] is the set of variables that is assigned a value,
            // i.e. appears to the left of an assignment op in a basic
            // block B before any use of that variable B
            if (destinationReg != null) {
                boolean hasBeenUsed = currUseSet.contains(destinationReg);

                if (!hasBeenUsed) {
                    currDefSet.add(destinationReg);
                    defSets.put(bb, currDefSet);
                }
            }

            // use[B] is the set of variables that are used, i.e. appear
            // to the right of an assignment op in basic block B. These
            // vars may be used prior to any def of the var in B.
            for (String sourceReg : sourceRegs) {
                boolean hasBeenDefined = currDefSet.contains(sourceReg);

                if (!hasBeenDefined) {
                    currDefSet.add(sourceReg);
                    useSets.put(bb, currUseSet);
                }
            }
        }

        this.defSets = defSets;
        this.useSets = useSets;
        defUseSetsGenerated = true;
    }

    private void generateInOutSets() {
        if (!defUseSetsGenerated) generateDefUseSets();

        boolean inSetChanged = false, outSetChanged = false;
        do {
            Set<String> inSet = new HashSet<>(), outSet = new HashSet<>(),
                    inSetOld, outSetOld;

            for (BasicBlock basicBlock : basicBlockAdjList) {
                inSetOld = inSet;
                outSetOld = outSet;

                // in[n] := use[n] ∪ (out[n] - def[n])
                Set<String> newIn = new HashSet<>();
                newIn.addAll(outSet);
                newIn.removeAll(defSets.get(basicBlock));
                newIn.addAll(useSets.get(basicBlock));
                inSet = newIn;

                // Update in[n]
                inSets.put(basicBlock, inSet);

                // out[n] := ∪ {in[s] | s ε succ[n]}
                Set<String> newOut = new HashSet<>();
                for (BasicBlock successor : basicBlock.successors) {
                    newOut.addAll(inSets.get(successor));
                }
                outSet = newOut;

                outSets.put(basicBlock, outSet);

                inSetChanged = !setsAreSame(inSetOld, inSet) || inSetChanged;
                outSetChanged = !setsAreSame(outSetOld, outSet) || outSetChanged;
            }
        } while (inSetChanged || outSetChanged);
    }

    /**
     * Does iterative dataflow calculation for insets and outsets
     * based on the LINE granularity
     */
    private void generateDefUseSetsByLine() {
        // Init def/use sets
        defSetsByLine = new LinkedList<>();
        useSetsByLine = new LinkedList<>();

        // Generate def and use sets by line
        for (int i = 0; i < instructionList.size(); i++) {
            Instruction instruction = instructionList.get(i);
            Set<String> currDefSet = defSetsByLine.get(i);
            Set<String> currUseSet = useSetsByLine.get(i);

            // Fetch all variables actively used or defined in the current line
            String destinationReg = instruction.getDestinationReg();
            List<String> sourceRegs = instruction.getSourceRegs();

            // def[B] is the set of variables that is assigned a value,
            // i.e. appears to the left of an assignment op in a basic
            // block B before any use of that variable B
            if (destinationReg != null) {
                boolean hasBeenUsed = currUseSet.contains(destinationReg);
                if (!hasBeenUsed) {
                    currDefSet.add(destinationReg);
                    defSetsByLine.set(i, currDefSet);
                }
            }

            // use[B] is the set of variables that are used, i.e. appear
            // to the right of an assignment op in basic block B. These
            // vars may be used prior to any def of the var in B.
            for (String sourceReg : sourceRegs) {
                boolean hasBeenDefined = currDefSet.contains(sourceReg);
                if (!hasBeenDefined) {
                    currDefSet.add(sourceReg);
                    useSetsByLine.set(i, currUseSet);
                }
            }
        }

        defUseSetsByLineGenerated = true;
    }

    /**
     * Does iterative dataflow calculation at the LINE granularity
     * @return set of variables live at each given instruction
     */
    public List<Set<String>> generateLiveSetsByLine() {
        if (!defUseSetsByLineGenerated) generateDefUseSetsByLine();

        inSetsByLine = new LinkedList<>();
        List<Set<String>> outSetsByLine = new LinkedList<>();

        boolean inSetChanged = false, outSetChanged = false;
        do {
            Set<String> inSet = new HashSet<>(), outSet = new HashSet<>(),
                    inSetOld, outSetOld;

            for (int i = 0; i < instructionList.size(); i++) {
                inSetOld = inSet;
                outSetOld = outSet;

                // in[n] := use[n] ∪ (out[n] - def[n])
                Set<String> newIn = new HashSet<>();
                newIn.addAll(outSetsByLine.get(i));
                newIn.removeAll(defSetsByLine.get(i));
                newIn.addAll(useSetsByLine.get(i));
                inSet = newIn;

                // Update in[n]
                inSetsByLine.set(i, inSet);

                // out[n] := ∪ {in[s] | s ε succ[n]}
                Set<String> newOut = new HashSet<>();
                lineNoToBasicBlock.get(i).successors.stream()
                        .forEach(successor -> newOut.addAll(inSetsByLine.get(successor.startingLine)));

                // Update out[n]
                outSet = newOut;
                outSetsByLine.set(i, outSet);

                inSetChanged = !setsAreSame(inSetOld, inSet) || inSetChanged;
                outSetChanged = !setsAreSame(outSetOld, outSet) || outSetChanged;


            }
        } while (inSetChanged || outSetChanged);

        return inSetsByLine;
    }

    private <T> boolean setsAreSame(Set<T> a, Set<T> b) {
        a.removeAll(b);

        return a.size() == 0;
    }
}
