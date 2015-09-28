import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Raghav K on 9/25/15.
 */
public class DFATest {
    public DFA dfa;
    private static final String statesFilename = "./data/states.csv",
            transitionsFilename = "./data/transitions.csv";

    @Before
    public void setUp() {
        dfa = new DFA(new File(statesFilename), new File(transitionsFilename));
    }

    @Test
    public void testDumpMapFileIO() {
        Map<String, State> states = dfa.states;

//        System.out.println("Printing out all the states recorded");
//        for (String str : states.keySet()) {
//            System.out.println(states.get(str));
//        }

        ArrayList<Character> al = new ArrayList<Character>(81);
//        System.out.println("Printing out all the transitions");
        for (StateInputWrapper siw : dfa.transitions.keySet()) {
            if (siw.getState() == null)
               al.add(siw.getInput());
//            try{
//                System.out.print(siw + " ");
//                System.out.println(dfa.transitions.get(siw));
//            } catch (NullPointerException e) {
//                System.out.println(siw.getInput());
//            }
        }
        Collections.sort(al);
        System.out.println(al);
    }

//    @Test
    public void testIOEcho() throws FileNotFoundException {
        Scanner scan = new Scanner(new File(transitionsFilename));

        while (scan.hasNextLine()) {
            System.out.println(scan.nextLine());
        }
    }

//    @Test
    public void testStartState() throws Exception {
        System.out.println(dfa.getState());
    }

    @Test
    public void testKeywords() throws Exception {
        String keywords[] = {"array", "break", "do", "else", "end", "for", "function", "if"
                ,"in", "let", "of", "then", "to", "type", "var", "while", "endif"
                , "begin", "end", "enddo", "return"};

        for (String keyword : keywords) {
            dfa.returnToStart();
            for (int i = 0; i < keyword.length(); i++) {
                System.out.println(dfa.getState());
                dfa.getNextState(keyword.charAt(i));
            }
            System.out.println(keyword + " = " + dfa.getState());
        }

    }
}
