import org.junit.Assert;
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
    public void testCommentTransition() {
        for (StateInputWrapper siw: dfa.transitions.keySet()){
            if (siw.getState().equals(new State("COMMENT_BEGIN_STATE", TokenType.NON_ACCEPTING))) {
                System.out.println(siw + " -> " + dfa.transitions.get(siw));
            }
        }
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
//            if (siw.getState() == null)
//               al.add(siw.getInput());
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

    @Test
    public void testStartState() throws Exception {
        Assert.assertTrue(dfa.getState().tokenType() == TokenType.NON_ACCEPTING
                && dfa.getState().getName().equals("START_STATE"));
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

    @Test
    public void testComment() throws Exception {
        String comments[] = {"/**/", "/*hello world*/","/*137812uikna,vjolk.234*/", "/*hello**/"};
        String errors[] = {"//*fuck*/", "/*/**/*/"};

        for (String comment: comments) {
            dfa.returnToStart();
            for (int i = 0; i < comment.length(); i++) {

                System.out.println(dfa.getState() + " " + comment.charAt(i));
                dfa.getNextState(comment.charAt(i));
            }
            Assert.assertTrue(dfa.getState().tokenType() == TokenType.COMMENT_END);
            System.out.println(comment + " = " + dfa.getState());
        }

        for (String e: errors) {
            dfa.returnToStart();
            System.out.println(dfa.getState());
            for (int i = 0; i < e.length(); i++) {
                dfa.getNextState(e.charAt(i));
                if (dfa.getState().tokenType() == TokenType.INVALID)
                    break;
            }
            if (dfa.getState().tokenType() != TokenType.INVALID){
                Assert.fail("Invalid token validated: " + e);
            }
            System.out.println(e + " = " + dfa.getState());
        }
    }

    @Test
    public void testNumbers() throws Exception {
        String integers[] = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        String floats[] = {"10.53", "0.42", "0.8291"};
        String errors[] = {"00","00.24", "0do"};

        for (String ints : integers) {
            dfa.returnToStart();
            for (int i = 0; i < ints.length(); i++) {
                System.out.println(dfa.getState());
                dfa.getNextState(ints.charAt(i));
            }
            Assert.assertTrue(dfa.getState().tokenType() == TokenType.INTLIT);
            System.out.println(ints + " = " + dfa.getState());
        }

        for (String f : floats) {
            dfa.returnToStart();
            for (int i = 0; i < f.length(); i++) {
                System.out.println(dfa.getState());
                dfa.getNextState(f.charAt(i));
            }
            Assert.assertTrue(dfa.getState().tokenType() == TokenType.FLOATLIT);
            System.out.println(f + " = " + dfa.getState());
        }

        for (String e: errors) {
            dfa.returnToStart();
            System.out.println(dfa.getState());
            for (int i = 0; i < e.length(); i++) {
                dfa.getNextState(e.charAt(i));
                if (dfa.getState().tokenType() == TokenType.INVALID)
                    break;
            }
            if (dfa.getState().tokenType() != TokenType.INVALID)
                Assert.fail("Invalid token validated" + e);
            System.out.println(e + " = " + dfa.getState());
        }
    }
}
