import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Instruction {
    private String label, opName, branchLabel, destinationReg, instrLiteral;
    private InstructionClass instructionClass;
    private List<String> sourceRegs;
    private int arrayIndex;

    public Instruction(String instrLiteral) {
        this.instrLiteral = instrLiteral;
        label = instrLiteral.split(":")[0];
        instrLiteral = instrLiteral.split(":")[1];
        String[] splitInstr = instrLiteral.split(",");
        opName = splitInstr[0];
        sourceRegs = new LinkedList<>();
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
                arrayIndex = Integer.parseInt(splitInstr[2]);
                sourceRegs.add(splitInstr[3]);
                instructionClass = InstructionClass.ARR_ST;
                break;
            case "array_load":
                destinationReg = splitInstr[1];
                arrayIndex = Integer.parseInt(splitInstr[2]);
                sourceRegs.add(splitInstr[3]);
                instructionClass = InstructionClass.ARR_LD;
                break;
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
