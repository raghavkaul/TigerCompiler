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
        datamap = new HashMap<>();
    }

    // assign X, 10,
    // assign X, 100, 10
    public String fillData(String op, String variable, String reg1, String reg2) {
        String instr = "";
        instr += "\t" + variable + ":\t.word\t";
        if (reg2.equals("")) { //   Single Variable Assignment
            if (!reg1.matches("\\d+") && !reg1.matches("\\d+.\\d+")) { // assign X, Y
                String value = datamap.get(reg1);
                instr += value;
                datamap.put(variable, value);
            } else {
                instr += reg1;
                datamap.put(variable, reg1);
            }
        } else {                    // Array Assignment
            if (!reg2.matches("\\d+") && !reg2.matches("\\d+.\\d+")) {
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
            instr += LD_INSTR + " " + physicalReg1 + ", " + getMemoryLocation(srcReg1) + "\n";
        }

        if (physicalReg2No == -1) {
            instr += LD_INSTR + " " + physicalReg2 + ", " + getMemoryLocation(srcReg2) + "\n";
        }

        String ops;
        switch (op) {
            case "add":
                if (srcReg1.matches("f\\d+") || srcReg2.matches("f\\d+"))
                    ops = "add.s";
                else
                    ops = "addu";
                break;
            case "sub":
                if (srcReg1.matches("f\\d+") || srcReg2.matches("f\\d+"))
                    ops = "sub.s";
                else
                    ops = "subu";
                break;
            case "mult":
                if (srcReg1.matches("f\\d+") || srcReg2.matches("f\\d+"))
                    ops = "mult.s";
                else
                    ops = "multu";
                break;
            case "div":
                if (srcReg1.matches("f\\d+") || srcReg2.matches("f\\d+"))
                    ops = "div.s";
                else
                    ops = "divu";
                break;
            case "and":
                ops = "and";
                break;
            case "or":
                ops = "or";
                break;
            default: // should never happen
                ops = op;
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