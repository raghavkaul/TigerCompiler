import java.util.*;

public class GraphSolver {
    private RegisterAllocationType rat;
    private SortedMap<String, List<String>> registerInterferenceAdjList;
    private Map<Integer, Map<String, List<String>>> registerInterferenceAdjListByBlock;
    private static final String REG_PREFIX = "$R";

    public GraphSolver(RegisterAllocationType rat, String filename) {
        this.rat = rat;
    }

    public void setRegisterAllocationType(RegisterAllocationType rat) {
        this.rat = rat;
    }

    public String getReg(String virtualReg) {
        if (rat == RegisterAllocationType.GLOBAL_AGGRESSIVE) {
            int regno = getColoringGlobalAggressive(registerInterferenceAdjList).get(virtualReg);
            return REG_PREFIX + regno;
        } else if (rat == RegisterAllocationType.INTRA_BLOCK) {
            return "$R0";// TODO implement
        } else { // Naive allocation always recommends the same register
            return REG_PREFIX + 0;
        }
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
    private Map<String, Integer> getColoringGlobalAggressive(SortedMap<String, List<String>> adjacencyList) {
        Map<String, Integer> coloring = new HashMap<>();

        // Initialize all vertices to unassigned
        adjacencyList.keySet().forEach(vertex -> coloring.put(vertex, -1));

        coloring.put(adjacencyList.lastKey(), 0);

        boolean available[] = new boolean[coloring.size()];

        for (int color = 0; color < available.length; color++) {
            available[color] = true;
        }

        for (int i = 0; i < coloring.size(); i++) {
            String currVertex = adjacencyList.lastKey();

            int j = 0;
            for (String adjacency : adjacencyList.get(currVertex)) {
                if (coloring.get(adjacency) == -1) {
                    available[coloring.get(adjacency)] = false;
                }
            }

            int cr;
            for (cr = 0; cr < coloring.size(); cr++) {
                if (available[cr]) break;
            }

            coloring.put(currVertex, cr);

            for (String adjacency : adjacencyList.get(currVertex)) {
                if (coloring.get(adjacency) == -1) {
                    available[coloring.get(adjacency)] = true;
                }
            }

            adjacencyList.remove(currVertex);
         }

        return coloring;
    }

    /**
     * Part I.b
     * For each node in the control flow graph, run the greedy algorithm
     * on every live range in the node/basic block. We then assign registers
     * to live ranges..
     * @param adjacencyList representation of each register and collisions
     * @return a map of register names -> their colors
     */
    public Map<BasicBlock, Integer> getCFGColoringGreedy(SortedMap<BasicBlock, List<BasicBlock>> adjacencyList) {
//        Map<BasicBlock, Integer> coloring = new HashMap<>();
//
//
//        // Initialize all vertices to color '0'
//        adjacencyList.keySet().forEach(basicBlock -> coloring.put(basicBlock, 0));
//
//        int color = 1;
//
//        // Color of v1 is 1.
//        BasicBlock v1 = adjacencyList.lastKey();
//        coloring.put(v1, color);
//        adjacencyList.remove(v1);
//
//        for (int i = 0; i < adjacencyList.size(); i++) {
//            BasicBlock vi = adjacencyList.lastKey();
//
//
//            adjacencyList.remove(vi);
//        }
//
//        return coloring;
        return null;
    }

    private class BasicBlock implements Comparable<BasicBlock> {
        Set<BasicBlock> successor;
        int startingLine, endingLine;

        @Override
        public int compareTo(BasicBlock bb) {
            return this.successor.size() - bb.successor.size();
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o instanceof BasicBlock
                    && ((BasicBlock) o).successor.equals(this.successor)
                    && ((BasicBlock) o).startingLine == this.startingLine
                    && ((BasicBlock) o).endingLine == this.endingLine;
        }
    }


}
