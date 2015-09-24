/**
 * Created by Raghav K on 9/23/15.
 */

// import org.junit.Before;
// import org.junit.Test;

import java.io.File;
import java.util.List;

public class DFATest {

    public DFA dfa;
    private static final String statesFilename = "../data/states.csv", transitionsFilename = "../data/transitions.csv";

    public DFATest() {
        dfa = new DFA(new File(statesFilename), new File(transitionsFilename));
    }

    // @Test
    public void testDumpMapFileIO() {
        List<State> states = dfa.states;

        for (State s : states) {
            System.out.println(s);
        }
    }

    public static void main(String[] args) {
        DFATest test = new DFATest();
        test.testDumpMapFileIO();

        System.out.println("Testing: array");
        String str = "array";
        for (int i = 0; i < str.length(); i++) {
            System.out.println("Current State: " + test.dfa.getState());
            test.dfa.getNextState(str.charAt(i));
        }

            System.out.println("Current State: " + test.dfa.getState());
    }
}
