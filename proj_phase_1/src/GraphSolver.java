import java.util.*;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphSolver {
    private RegisterAllocationType rat;
    private LivenessAnalyzer la;
    private IRParser irp;
    private static final String REG_PREFIX = "$R";


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
                break;
            default: // Fallthrough.
                throw new IllegalStateException("The register allocation type state machine broke.");
        }
        return null; // TODO fix
    }

    /**
     * Part I.b
     * For each node in the control flow graph, run the greedy algorithm
     * on every live range in the node/basic block. We then assign registers
     * to live ranges..
     * adjacencyList representation of each register and collisions
     * a map of register names -> their colors
     */
    private Map<String, Integer> getGreedyColoring(SortedMap<String, List<String>> adjacencyList) {
//        return greedyColoring(adjacencyList);
        return null; // TODO
    }

    private <V> Map<V, Integer> greedyColoring(SortedMap<V, Collection<V>> adjacencyList) {
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

            int j = 0;
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
     * @return adjacency list representation of interference graph from infile
     */
    private SortedMap<String, Set<String>> getRegInterferenceAdjList() {
        SortedMap<String, Set<String>> regInterferenceAdjList = new TreeMap<>();
        List<Map<String, IRParser.VarUseBlock>> livenessAndNextUse
                = irp.getLivenessAndNextUse();
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
        Set<String> spilledRegisters = new HashSet<>();
        while (!graphIsRColorable) {
            // TODO make sure this matches the semantics of actual algo
            // While the graph has a node N with degree < R
            for (V N : adjacencyList.keySet()) {
                if (adjacencyList.get(N).size() < R) {
                    // Remove the node and its' associated edges, and
                    // push N onto the stack
                    adjacencyList.remove(N);
                    adjacencyList.forEach((vertex, adjacencies) -> {
                        adjacencies.remove(N);
                    });
                    stack.push(N);
                }
            }

            if (!adjacencyList.isEmpty()) {
                V N = adjacencyList.firstKey();
                // Todo Spill live range associated with n
                // Remove n from G, along with all edges incident to it
                // Spill n

            } else { // This implies the graph is R-colorable
                while (!stack.isEmpty()) {
                    graphIsRColorable = true;
                    V N = stack.pop();
                    // TODO get adjacencies of V
                    // color V the lowest color that doesn't clash with
                    // the color of one of the adjacencies
                }
            }
        }

        return coloring;
    }
}
