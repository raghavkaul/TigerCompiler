public class CodeGenerator {
    private GraphSolver graphSolver;
    private static final int NUM_REGS = 8;
    private String[] REGFILE = new String[NUM_REGS];
    private static final String
            LD_INSTR = "LD";

    public CodeGenerator(String filename) {
        graphSolver = new GraphSolver(RegisterAllocationType.NAIVE, filename);
    }

    // TODO make sure this entire method matches MIPS syntax
    public String getInstrThreeAddrType(String op, String srcReg1, String srcReg2, String destReg) {
        // add x <- y,z;
        String instr = "";

        // Get expected registers for two source operands
        String physicalReg1 = graphSolver.getReg(srcReg1);
        String physicalReg2 = graphSolver.getReg(srcReg2);

        // Check regfile if source regs are already loaded
        int physicalReg1No = getIndexOf(physicalReg1, REGFILE);
        int physicalReg2No = getIndexOf(physicalReg2, REGFILE);

        // If source variables not found in expected REGFILE, load them from memory
        if (physicalReg1No == -1) {
            instr += LD_INSTR + " " + physicalReg1 + ", " + getMemoryLocation(srcReg1) + "\n";
        }

        if (physicalReg2No == -1) {
            instr += LD_INSTR + " " + physicalReg2 + ", " + getMemoryLocation(srcReg2) + "\n";
        }

        // Emit the instruction
        instr += op + " " + destReg + ", " + physicalReg1 + ", " + physicalReg2;

        return instr;
    }

    private int getMemoryLocation(String virtualReg) {
        // Decide a memory location for each virtual register on the stack
        // When loading and storing within a stack frame, we will use this offset
        return 0; // TODO implement
    }

    private static <T> int getIndexOf(T elem, T[] myArray) {
        for (int i = 0; i < myArray.length; i++) {
            if (myArray[i].equals(elem)) return i;
        }

        return -1;
    }
}
