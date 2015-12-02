import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class IRParserTest {
    private IRParser[] irParsers;
    private static final String[] filenames = {""};


    public void setUp() {
        irParsers = new IRParser[filenames.length];

        for (int i = 0; i < filenames.length; i++) {
            irParsers[i] = new IRParser(filenames[i]);
        }
    }

    @Test
    public void testInstructionGeneration() {
        String[] instructionLiterals = {
                "assign, a, b",
                "helloLabel: assign, a, b",
                "add, a, b, t1",
                "helloLabel: add, a, b, t1",
                "or, y, x, x",
                "goto, after_loop",
                "helloLabel: goto, after_loop",
                "breq, a, 5, after_if_part",
                "helloLabel: breq, a, 5, after_if_part",
                "brlt, 0, 0, b",
                "return, a",
                "call, foo, x, y, z, a, b, c",
                "callr, a, b, c, x, y, z, foo",
                "helloLabel: callr, a, b, c, x, y, z, foo",
                "array_store, b, 0, a",
                "array_load, a, arr, 0",
                "assign, X, 100, 10"
        };

        Instruction[] parsedInstructions = new Instruction[instructionLiterals.length];

        for (int i = 0; i < instructionLiterals.length; i++) {
//            System.out.println(i);
            parsedInstructions[i] = new Instruction(instructionLiterals[i]);
        }

        /*
        Fields to test:
            String label, opName, branchLabel, destinationReg, instrLiteral;
            InstructionClass instructionClass;
            List<String> sourceRegs;
            int arrayIndex;
        */

        Instruction currInstruction = parsedInstructions[0];
        assertNull(currInstruction.getLabel());
        assertEquals("assign", currInstruction.getOpName());
        assertEquals(InstructionClass.ASSIGN, currInstruction.getInstructionClass());
        assertEquals("a", currInstruction.getDestinationReg());
        assertEquals(Arrays.asList(new String[]{"b"}), currInstruction.getSourceRegs());

        currInstruction = parsedInstructions[1];
//        assertNotNull(currInstruction.getLabel());
        assertEquals("helloLabel", currInstruction.getLabel());
        assertEquals("assign", currInstruction.getOpName());
        assertEquals(InstructionClass.ASSIGN, currInstruction.getInstructionClass());
        assertEquals("a", currInstruction.getDestinationReg());
        assertEquals(Arrays.asList(new String[]{"b"}), currInstruction.getSourceRegs());

        currInstruction = parsedInstructions[2]; //"add, a, b, t1",
        assertNull(currInstruction.getLabel());
        assertEquals("add", currInstruction.getOpName());
        assertEquals("t1", currInstruction.getDestinationReg());
        assertEquals(Arrays.asList(new String[]{"a", "b"}), currInstruction.getSourceRegs());
        assertEquals(InstructionClass.BINOP, currInstruction.getInstructionClass());

        currInstruction = parsedInstructions[3]; //"helloLabel: add, a, b, t1",
        assertEquals("helloLabel", currInstruction.getLabel());
        assertEquals("add", currInstruction.getOpName());
        assertEquals("t1", currInstruction.getDestinationReg());
        assertEquals(Arrays.asList(new String[]{"a", "b"}), currInstruction.getSourceRegs());
        assertEquals(InstructionClass.BINOP, currInstruction.getInstructionClass());

        currInstruction = parsedInstructions[4]; // or, y, x, x",
        assertNull(currInstruction.getLabel());
        assertEquals("or", currInstruction.getOpName());
        assertEquals("x", currInstruction.getDestinationReg());
        assertEquals(Arrays.asList(new String[]{"y", "x"}), currInstruction.getSourceRegs());
        assertEquals(InstructionClass.BINOP, currInstruction.getInstructionClass());

        currInstruction = parsedInstructions[5]; // goto, after_loop",
        assertNull(currInstruction.getLabel());
        assertEquals("after_loop", currInstruction.getBranchLabel());
        assertEquals("goto", currInstruction.getOpName());
        assertEquals(InstructionClass.GOTO, currInstruction.getInstructionClass());

        currInstruction = parsedInstructions[6]; // "helloLabel: goto, after_loop"
        assertEquals("helloLabel", currInstruction.getLabel());
        assertEquals("after_loop", currInstruction.getBranchLabel());
        assertEquals("goto", currInstruction.getOpName());
        assertEquals(InstructionClass.GOTO, currInstruction.getInstructionClass());

        currInstruction = parsedInstructions[7]; // "breq, a, 5, after_if_part",
        assertEquals("breq", currInstruction.getOpName());
        assertEquals(InstructionClass.BRANCH, currInstruction.getInstructionClass());
//        assertEquals(Arrays.asList(new String[]{"a, 5"}), currInstruction.getSourceRegs());
        assertEquals("after_if_part", currInstruction.getBranchLabel());

        currInstruction = parsedInstructions[8]; // "helloLabel: breq, a, 5, after_if_part",
        assertEquals("helloLabel", currInstruction.getLabel());
        assertEquals("breq", currInstruction.getOpName());
        assertEquals(InstructionClass.BRANCH, currInstruction.getInstructionClass());
//        assertEquals(Arrays.asList(new String[]{"a, 5"}), currInstruction.getSourceRegs());
        assertEquals("after_if_part", currInstruction.getBranchLabel());

        currInstruction = parsedInstructions[9]; // "brlt, 0, 0, b",
        assertEquals("brlt", currInstruction.getOpName());
        assertEquals(InstructionClass.BRANCH, currInstruction.getInstructionClass());
        assertEquals(Arrays.asList(new String[]{"0", "0"}), currInstruction.getSourceRegs());
        assertEquals("b", currInstruction.getBranchLabel());

        currInstruction = parsedInstructions[10]; // return, a",
        assertEquals("return", currInstruction.getOpName());
        assertEquals(InstructionClass.RET, currInstruction.getInstructionClass());
        assertEquals(Arrays.asList(new String[]{"a"}), currInstruction.getSourceRegs());

        currInstruction = parsedInstructions[11]; // "call, foo, x, y, z, a, b, c",
        assertEquals("call", currInstruction.getOpName());
        assertEquals(InstructionClass.FUNC_CALL, currInstruction.getInstructionClass());
        assertEquals(Arrays.asList(new String[] {"x", "y", "z", "a", "b", "c"}), currInstruction.getSourceRegs());
//        assertEquals("foo", currInstruction.getDestinationReg());
        assertEquals("foo", currInstruction.getBranchLabel());

        currInstruction = parsedInstructions[12]; // "callr, a, b, c, x, y, z, foo",
        assertEquals("callr", currInstruction.getOpName());
        assertEquals(InstructionClass.FUNC_CALL, currInstruction.getInstructionClass());
        assertEquals(Arrays.asList(new String[] {"c", "x", "y", "z", "foo"}), currInstruction.getSourceRegs());
        assertEquals("a", currInstruction.getDestinationReg());
        assertEquals("b", currInstruction.getBranchLabel());

        currInstruction = parsedInstructions[13]; // helloLabel: callr, a, b, c, x, y, z, foo",
        assertEquals("callr", currInstruction.getOpName());
        assertEquals(InstructionClass.FUNC_CALL, currInstruction.getInstructionClass());
        assertEquals(Arrays.asList(new String[] {"c", "x", "y", "z", "foo"}), currInstruction.getSourceRegs());
        assertEquals("a", currInstruction.getDestinationReg());
        assertEquals("b", currInstruction.getBranchLabel());
        assertEquals("helloLabel", currInstruction.getLabel());

//        currInstruction = parsedInstructions[14]; // "array_store, b, 0, a",
//        currInstruction = parsedInstructions[15]; //array_load, a, arr, 0",
//        currInstruction = parsedInstructions[16]; // "assign, X, 100, 10"


    }
}
