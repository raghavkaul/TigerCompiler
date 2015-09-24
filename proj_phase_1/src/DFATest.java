/**
 * Created by Raghav K on 9/23/15.
 *
 */

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class DFATest {

    private DFA dfa;
    private static final String statesFilename = "states.csv", transitionsFilename = "transitions.csv";

    @Before
    public void setUp() {
        dfa = new DFA(new File(statesFilename), new File(transitionsFilename));
    }

    @Test
    public void testDumpMapFileIO() {
        List<State> states = dfa.states;

        for (State s : states) {
            System.out.println(s);
        }
    }
}
