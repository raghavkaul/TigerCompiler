import java.util.HashMap;

public class CodeGenerator {
    private GraphSolver graphSolver;
    private static final int NUM_REGS = 8;
    private String[] REGFILE = new String[NUM_REGS];
    private static final String
            LD_INSTR = "lw",
            ST_INSTR = "sw",
            LA_INSTR = "la";

    private HashMap<String, String> datamap;

    public CodeGenerator(String filename) {
        graphSolver = new GraphSolver(RegisterAllocationType.NAIVE, filename);
        datamap = new HashMap<String, String>();
    }

    // assign X, 10,
    // assign X, 100, 10
    public String fillData(String op, String variable, String reg1, String reg2) {
        String instr = "";
        instr += "\t" + variable + ":\t.word\t";
        if (reg2.equals("")) { //   Single Variable Assignment
            if (!reg1.matches("\\d+")) { // assign X, Y
                String value = datamap.get(reg1);
                instr += value;
                datamap.put(variable, value);
            } else {
                instr += reg1;
                datamap.put(variable, reg1);
            }
        } else {                    // Array Assignment
            if (!reg2.matches("\\d+")) {
                String value = datamap.get(reg2);
                instr += value;
                datamap.put(variable, value);
            } else {
                instr += reg2;
                datamap.put(variable, reg2);
            }
            instr += ":" + reg1;
        }
        return instr;
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
//            if (srcReg1)
            instr += LA_INSTR + " " + physicalReg1 + ", " + getMemoryLocation(srcReg1) + "\n";
        }

        if (physicalReg2No == -1) {
            instr += LA_INSTR + " " + physicalReg2 + ", " + getMemoryLocation(srcReg2) + "\n";
        }

        // Emit the instruction
        instr += op + " " + destReg + ", " + physicalReg1 + ", " + physicalReg2;

        return instr;
    }

    // TODO I don't think we need this
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