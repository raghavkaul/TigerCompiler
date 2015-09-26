import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Raghav K on 9/25/15.
 */
public class ScannerTest {
    public DFA dfa;
    private static final String statesFilename = "./data/states_dup.csv",
            transitionsFilename = "./data/transitions.csv";

    @Before
    public void setUp() {
        dfa = new DFA(new File(statesFilename), new File(transitionsFilename));
    }

    @Test
    public void testDumpMapFileIO() {
        List<State> states = dfa.states;

        System.out.println("Printing out all the states recorded");
        for (State s : states) {
            System.out.println(s);
        }

        System.out.println("Printing out all the transitions");
        for (StateInputWrapper siw : dfa.transitions.keySet()) {
            System.out.print(siw + " ");
            System.out.println(dfa.transitions.get(siw));
        }
    }

    @Test
    public void testIOEcho() throws FileNotFoundException {
        Scanner scan = new Scanner(new File(transitionsFilename));

        while (scan.hasNextLine()) {
            System.out.println(scan.nextLine());
        }
    }
}
