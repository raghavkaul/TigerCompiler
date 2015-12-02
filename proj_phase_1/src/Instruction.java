import java.util.*;

public class Instruction {
    private String label, opName, branchLabel, destinationReg, instrLiteral;
    private InstructionClass instructionClass;
    private List<String> sourceRegs;
    private int arrayIndex;
    private static final String matchIntTemp = "i\\d+", matchFloatTemp = "f\\d+";

    public Instruction() {

    }

    public Instruction(String instrLiteral) {
        this.instrLiteral = instrLiteral;
        label = instrLiteral.contains(":") ? instrLiteral.split(":")[0] : null;

        if (instrLiteral.split(":").length == 1) {
            instructionClass = InstructionClass.BARE_LABEL;
            return;
        }

        instrLiteral = instrLiteral.contains(":") ? instrLiteral.split(":")[1] : instrLiteral;
        String[] splitInstr = instrLiteral.split(",");

        for (int i = 0; i < splitInstr.length; i++) {
            splitInstr[i] = splitInstr[i].trim();
        }

        opName = splitInstr[0];
        sourceRegs = new ArrayList<>();
        destinationReg = null;
        branchLabel = null;
        arrayIndex = 0;

        switch(splitInstr[0]) {
            case "assign":
                instructionClass = InstructionClass.ASSIGN;
                destinationReg = splitInstr[1];
                sourceRegs.add(splitInstr[2]);
                break;
            case "add":
            case "sub":
            case "mult":
            case "div":
            case "and":
            case "or":
                instructionClass = InstructionClass.BINOP;
                sourceRegs.add(splitInstr[1]);
                sourceRegs.add(splitInstr[2]);
                destinationReg = splitInstr[3];
                break;
            case "goto":
                instructionClass = InstructionClass.GOTO;
                branchLabel = splitInstr[1];
                break;
            case "breq":
            case "brneq":
            case "brlt":
            case "brgt":
            case "brgeq":
            case "brleq":
                instructionClass = InstructionClass.BRANCH;
                sourceRegs.add(splitInstr[1]);
                sourceRegs.add(splitInstr[2]);
                branchLabel = splitInstr[3];
                break;
            case "return":
                instructionClass = InstructionClass.RET;
                sourceRegs.add(splitInstr[1]);
                break;
            case "call":
                instructionClass = InstructionClass.FUNC_CALL;
                branchLabel = splitInstr[1];
                String[] params = Arrays.copyOfRange(splitInstr, 2, splitInstr.length);
                Collections.addAll(sourceRegs, params);
                break;
            case "callr":
                destinationReg = splitInstr[1];
                branchLabel = splitInstr[2];
                instructionClass = InstructionClass.FUNC_CALL;
                params = Arrays.copyOfRange(splitInstr, 3, splitInstr.length);
                Collections.addAll(sourceRegs, params);
                break;
            case "array_store":
                destinationReg = splitInstr[1];
                // check if array is indexed by var or int lit
                if (splitInstr[2].matches("\\d+")) {
                    arrayIndex = Integer.parseInt(splitInstr[2]);
                } else {
                    arrayIndex = -1;
                    sourceRegs.add(splitInstr[2]);
                }
                sourceRegs.add(splitInstr[3]);
                instructionClass = InstructionClass.ARR_ST;
                break;
            case "array_load":
                destinationReg = splitInstr[1];
                sourceRegs.add(splitInstr[2]);
                if (splitInstr[3].matches("\\d+")) {
                    arrayIndex = Integer.parseInt(splitInstr[3]);
                } else {
                    arrayIndex = -1;
                    sourceRegs.add(splitInstr[3]);
                }
                instructionClass = InstructionClass.ARR_LD;
                break;
            default:
                throw new IllegalArgumentException("invalid instruction type: " + splitInstr[0]);
        }
    }

    public String getLabel() {
        return label;
    }

    public String getOpName() {
        return opName;
    }

    public InstructionClass getInstructionClass() {
        return instructionClass;
    }

    public boolean isBranchInstruction() {
        return instructionClass == InstructionClass.BRANCH
                || instructionClass == InstructionClass.FUNC_CALL
                || instructionClass == InstructionClass.GOTO
                || instructionClass == InstructionClass.RET;
    }

    public boolean isBranchTarget() {
        return label != null;
    }

    public int getArrayIndex() {
        return arrayIndex;
    }

    public List<String> getSourceRegs() {
        return sourceRegs;
    }

    public String getDestinationReg() {
        return destinationReg;
    }

    public String getBranchLabel() {
        return branchLabel;
    }

    public String getInstrLiteral() { return instrLiteral; }

    @Override
    public boolean equals(Object o) {
        return o != null
                && o instanceof Instruction
                && ((Instruction) o).getInstrLiteral().equals(this.instrLiteral);
    }
}
