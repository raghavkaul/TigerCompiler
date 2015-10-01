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
        Assert.assertTrue(dfa.getCurrState().tokenType() == TokenType.NON_ACCEPTING
                && dfa.getCurrState().getName().equals("START_STATE"));
    }

    @Test
    public void testKeywords() throws Exception {
        String keywords[] = {"array", "break", "do", "else", "end", "for", "function", "if"
                ,"in", "let", "of", "then", "to", "type", "var", "while", "endif"
                , "begin", "end", "enddo", "return"};


        for (String keyword : keywords) {
            dfa.returnToStart();
            for (int i = 0; i < keyword.length(); i++) {
                System.out.println(dfa.getCurrState());
                dfa.getNextState(keyword.charAt(i));
            }
            System.out.println(keyword + " = " + dfa.getCurrState());
        }


    }

    @Test
    public void testID() throws Exception {
        String ids[] = {"arraya", "breakd", "doe", "elsse", "endf", "fov", "functdion", "iaf"
                ,"idn", "leet", "ofs", "thven", "tco", "typae", "vear", "whfile", "endiff"
                , "begind", "endd", "enddoa", "returne", "a19238021i", "d_D3", "D", "A"};

        String errors[] = {"0d", "_D", "sd/"};

        for (String id : ids) {
            dfa.returnToStart();
            for (int i = 0; i < id.length(); i++) {
                System.out.println(dfa.getCurrState() + " " + id.charAt(i));
                dfa.setNextState(id.charAt(i));
            }
            Assert.assertTrue(dfa.getCurrState().tokenType() == TokenType.ID);
        }

        for (String e: errors) {
            dfa.returnToStart();
            System.out.println(dfa.getCurrState());
            for (int i = 0; i < e.length(); i++) {
                dfa.setNextState(e.charAt(i));
                if (dfa.getCurrState().tokenType() == TokenType.INVALID)
                    break;
            }
            if (dfa.getCurrState().tokenType() != TokenType.INVALID){
                Assert.fail("Invalid token validated: " + e);
            }
            System.out.println(e + " = " + dfa.getCurrState());
        }
    }

    @Test
    public void testComment() throws Exception {
        String comments[] = {"/**/", "/*hello world*/","/*137812uikna<vjolk.234*/", "/*hello**/"};
        String errors[] = {"//*fuck*/", "/*/**/*/"};

        for (String comment: comments) {
            dfa.returnToStart();
            for (int i = 0; i < comment.length(); i++) {

                System.out.println(dfa.getCurrState() + " " + comment.charAt(i));
                dfa.setNextState(comment.charAt(i));
            }
            Assert.assertTrue(dfa.getCurrState().tokenType() == TokenType.COMMENT_END);
            System.out.println(comment + " = " + dfa.getCurrState());
        }

        for (String e: errors) {
            dfa.returnToStart();
            System.out.println(dfa.getCurrState());
            for (int i = 0; i < e.length(); i++) {
                dfa.setNextState(e.charAt(i));
                if (dfa.getCurrState().tokenType() == TokenType.INVALID)
                    break;
            }
            if (dfa.getCurrState().tokenType() != TokenType.INVALID){
                Assert.fail("Invalid token validated: " + e);
            }
            System.out.println(e + " = " + dfa.getCurrState());
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
                System.out.println(dfa.getCurrState());
                dfa.setNextState(ints.charAt(i));
            }
            Assert.assertTrue(dfa.getCurrState().tokenType() == TokenType.INTLIT);
            System.out.println(ints + " = " + dfa.getCurrState());
        }

        for (String f : floats) {
            dfa.returnToStart();
            for (int i = 0; i < f.length(); i++) {
                System.out.println(dfa.getCurrState());
                dfa.setNextState(f.charAt(i));
            }
            Assert.assertTrue(dfa.getCurrState().tokenType() == TokenType.FLOATLIT);
            System.out.println(f + " = " + dfa.getCurrState());
        }

        for (String e: errors) {
            dfa.returnToStart();
            System.out.println(dfa.getCurrState());
            for (int i = 0; i < e.length(); i++) {
                dfa.setNextState(e.charAt(i));
                if (dfa.getCurrState().tokenType() == TokenType.INVALID)
                    break;
            }
            if (dfa.getCurrState().tokenType() != TokenType.INVALID)
                Assert.fail("Invalid token validated" + e);
            System.out.println(e + " = " + dfa.getCurrState());
        }
    }
}
