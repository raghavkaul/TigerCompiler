import java.util.*;

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
        if (rat == RegisterAllocationType.GLOBAL_AGGRESSIVE) {
            int regno = 0;
            return REG_PREFIX + regno;
        } else if (rat == RegisterAllocationType.INTRA_BLOCK) {
            return "$R0";// TODO implement
        } else { // Naive allocation always recommends the same register
            return REG_PREFIX + 0;
        }
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
        return greedyColoring(adjacencyList);
    }

    private <V> Map<V, Integer> greedyColoring(SortedMap<V, List<V>> adjacencyList) {
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

    private <V> Map<V, Integer> chaitinBriggsColoring(SortedMap<V, List<V>> adjacencyList, int R) {
        Map<V, Integer> coloring = new HashMap<>();
        boolean graphIsRColorable = false;
        Stack<V> stack = new Stack<>();

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

            // If the entire Graph has been removed then the graph is R-colorable
            if (adjacencyList.isEmpty()) {
                while (!stack.isEmpty()) {
                    graphIsRColorable = true;
                    V N = stack.pop();
                    // TODO assign it a color from the R colors?
                }
            } else {
                // TODO Spill node with max def and references
            }

        }

        return coloring;
    }
}
