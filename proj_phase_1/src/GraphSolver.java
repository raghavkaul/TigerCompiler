import java.util.*;

public class GraphSolver {
    private RegisterAllocationType rat;
    private LivenessAnalyzer la;
    private IRParser irp;

    public GraphSolver(RegisterAllocationType rat, String filename) {
        this.rat = rat;
        la = new LivenessAnalyzer(filename);
        irp = la.getIrParser();
    }

    public void setRegisterAllocationType(RegisterAllocationType rat) {
        this.rat = rat;
    }

    public String getReg(String virtualReg) {
        switch (rat) {
            case INTRA_BLOCK:
                break;
            case GLOBAL_AGGRESSIVE:
                break;
            case NAIVE:
                return "$R0";
            default: // Fallthrough.
                throw new IllegalStateException("The register allocation type state machine broke.");
        }
        return null; // TODO fix
    }

    private Map<BasicBlock, Map<String, Integer>> getLoadCounts() {
        // Gets the greedy coloring for each block
        Map<BasicBlock, Set<String>> liveSets = la.getLiveSets();
        Map<BasicBlock, Map<String, Integer>> loadCounts = new HashMap<>();
        Map<Integer, BasicBlock> lineNoToBasicBlock = irp.lineNoToBasicBlock();

        List<Instruction> instructions = irp.getInstructions();

        BasicBlock currBasicBlock = lineNoToBasicBlock.get(0);
        Set<String> currLiveSet = liveSets.get(currBasicBlock);
        Map<String, Integer> currLoadCounts = loadCounts.get(currBasicBlock);

        for (int i = 0; i < instructions.size(); i++) {
            if (!lineNoToBasicBlock.get(i).equals(currBasicBlock)) {
                currBasicBlock = lineNoToBasicBlock.get(i);
                currLiveSet = liveSets.get(currBasicBlock);
                currLoadCounts = loadCounts.get(currBasicBlock);

            }

            Instruction currInstruction = instructions.get(i);

            for (String sourceReg : currInstruction.getSourceRegs()) {
                if (!currLiveSet.contains(sourceReg)) continue; // TODO Not sure what this is for.

                // Update load counts
                Integer count = currLoadCounts.get(sourceReg);

                if (count == null) currLoadCounts.put(sourceReg, 1);
                else currLoadCounts.put(sourceReg, count + 1);

                loadCounts.put(currBasicBlock, currLoadCounts);
            }
        }

        return loadCounts;
    }

    /**
     * Part I.b
     * For each node in the control flow graph, run the greedy algorithm
     * on every live range in the node/basic block. We then assign registers
     * to live ranges..
     * adjacencyList representation of each register and collisions
     * a map of register names -> their colors
     */
    private Map<BasicBlock, Map<String, Integer>> getGreedyColoring() {
        Map<BasicBlock, Set<String>> liveSets = la.getLiveSets();
        Map<BasicBlock, Map<String, Integer>> globalGreedyColoring = new HashMap<>();
        Map<Integer, BasicBlock> lineNoToBasicBlock = irp.lineNoToBasicBlock();
        List<Instruction> instructions = irp.getInstructions();

        BasicBlock currBasicBlock = lineNoToBasicBlock.get(0);
        Set<String> currLiveSet = liveSets.get(currBasicBlock);
        SortedMap<String, Set<String>> currAdjacencyList = new TreeMap<>();

        for (int i = 0; i < instructions.size(); i++) {
            if (!lineNoToBasicBlock.get(i).equals(currBasicBlock)) {
                Map<String, Integer> greedyColoring = greedyColoring(currAdjacencyList);
                globalGreedyColoring.put(currBasicBlock, greedyColoring);
                currBasicBlock = lineNoToBasicBlock.get(i);
                currLiveSet = liveSets.get(currBasicBlock);
            }

            Instruction currInstruction = instructions.get(i);

            for (String sourceReg : currInstruction.getSourceRegs()) {
                if (!currLiveSet.contains(sourceReg)) continue; // TODO Not sure what this is for.

                // Populate adjacency list
                for (String sourceReg1 : currInstruction.getSourceRegs()) {
                    if (sourceReg1.equals(sourceReg)) continue; // No self loops

                    // Get current edgeset
                    Set<String> srSet = currAdjacencyList.get(sourceReg),
                            sr1Set = currAdjacencyList.get(sourceReg1);

                    // Add edges
                    if (srSet == null) srSet = new HashSet<>();
                    srSet.add(sourceReg1);

                    if (sr1Set == null) sr1Set = new HashSet<>();
                    sr1Set.add(sourceReg);

                    // Update adjacencylist
                    currAdjacencyList.put(sourceReg, srSet);
                    currAdjacencyList.put(sourceReg1, sr1Set);

                }
            }
        }

        return globalGreedyColoring;
    }

    private <V> Map<V, Integer> greedyColoring(SortedMap<V, ? extends Collection<V>> adjacencyList) {
        Map<V, Integer> coloring = new HashMap<>();

        // Initialize all vertices to unassigned
        adjacencyList.keySet().forEach(vertex -> coloring.put(vertex, -1));

        coloring.put(adjacencyList.lastKey(), 0);

        boolean available[] = new boolean[coloring.size()];

        for (int color = 0; color < available.length; color++) {
            available[color] = true;
        }

        for (int i = 0; i < coloring.size(); i++) {
            V currVertex = adjacencyList.lastKey();

            for (V adjacency : adjacencyList.get(currVertex)) {
                if (coloring.get(adjacency) == -1) {
                    available[coloring.get(adjacency)] = false;
                }
            }

            int cr;
            for (cr = 0; cr < coloring.size(); cr++) {
                if (available[cr]) break;
            }

            coloring.put(currVertex, cr);

            for (V adjacency : adjacencyList.get(currVertex)) {
                if (coloring.get(adjacency) == -1) {
                    available[coloring.get(adjacency)] = true;
                }
            }

            adjacencyList.remove(currVertex);
        }

        return coloring;
    }

    /**
     * Part I.c
     * Runs optimized algorithm on register interference graph,
     * producing a mapping of register names -> colors, corresponding to a
     * mapping of virtual registers to physical registers. Discrete colors/PRs
     * for two different VRs implies they are used in the same instruction,
     * and therefore may not exist in the same physical register at the same time
     * @return Mapping from register name to physical register number
     */
    private Object getGlobalAggressiveColoring() {

        return null; // TODO implement
    }

    /**
     * Builds interference graph across all program points.
     * This essentially computes interference within a live range -
     * if a register is live in the same basic block as another
     * register, then there is an overlap in live ranges and they
     * are therefore adjacent
     * @return adjacency list representation of interference graph from infile
     */
    private SortedMap<String, Set<String>> getRegInterferenceAdjList() {
        SortedMap<String, Set<String>> regInterferenceAdjList = new TreeMap<>();
        Map<Integer, BasicBlock> lineNoToBasicBlock = irp.lineNoToBasicBlock();
        List<Instruction> instructions = irp.getInstructions();

        for (int i = 0; i < instructions.size(); i++) {

            Map<BasicBlock, Set<String>> insets = la.getInSets();
            Set<String> liveTemps = insets.get(lineNoToBasicBlock.get(i));

            for (String s1 : liveTemps) {
                for (String s2 : liveTemps) {
                    if (!s1.equals(s2)) {
                        Set<String> currAdjS1 = regInterferenceAdjList.get(s1),
                                currAdjS2 = regInterferenceAdjList.get(s2);

                        if (currAdjS1 == null) {
                            currAdjS1 = new HashSet<>();
                        }

                        if (currAdjS2 == null) {
                            currAdjS2 = new HashSet<>();
                        }

                        currAdjS1.add(s2);
                        regInterferenceAdjList.put(s1, currAdjS1);
                        currAdjS2.add(s1);
                        regInterferenceAdjList.put(s2, currAdjS2);

                    }
                }
            }
        }

        return regInterferenceAdjList;
    }

    private <V> Map<V, Integer> chaitinBriggsColoring(SortedMap<V, Collection<V>> adjacencyList,
                                                      int R) {
        Map<V, Integer> coloring = new HashMap<>();
        boolean graphIsRColorable = false;
        Stack<V> stack = new Stack<>();
        Set<V> spilled = new HashSet<>();
        while (!graphIsRColorable) {
            // TODO make sure this matches the semantics of actual algo
            // While the graph has a node N with degree < R
            for (V N : adjacencyList.keySet()) {
                if (adjacencyList.get(N).size() < R) {
                    // Remove the node and its' associated edges, and
                    // push N onto the stack
                    adjacencyList.remove(N);
                    adjacencyList.forEach((vertex, adjacencies) -> adjacencies.remove(N));
                    stack.push(N);
                }
            }

            if (!adjacencyList.isEmpty()) {
                V N = adjacencyList.firstKey();
                // Spill live range associated with n
                // Remove n from G, along with all edges incident to it
                spilled.add(N);
                adjacencyList.remove(N);
                adjacencyList.forEach((vertex, adjacencies) -> adjacencies.remove(N));

            } else { // This implies the graph is R-colorable
                while (!stack.isEmpty()) {
                    graphIsRColorable = true;
                    V N = stack.pop();
                    Collection<V> adjacencies = adjacencyList.get(N);
                    boolean[] availableColors = new boolean[R];
                    for (V adjacency : adjacencies) {
                        Integer colorOfAdj = coloring.get(adjacency);
                        if (colorOfAdj != null) {
                            availableColors[colorOfAdj] = false;
                        }
                    }

                    boolean foundColor = false;
                    for (int i = 0; i < availableColors.length; i++) {
                        if (availableColors[i]) {
                            coloring.put(N, i);
                            foundColor = true;
                            break;
                        }
                    }

                    if (!foundColor) {
                        spilled.add(N);
                    }
                }
            }
        }

        return coloring;
    }
}
