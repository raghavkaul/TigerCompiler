import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Raghav K on 9/17/15.
 */
public class DFA {
    private Map<StateInputWrapper, State> transitions;
    private State startState;
    private State currState;

    public DFA(File statesFile, File transitionsFile) {
        populateTransitions(statesFile, transitionsFile);
        startState = new State("Start", TokenType.NON_ACCEPTING);
        currState = startState;
    }

    public State getNextState(char inputChar) {
        return currState.getNextState(inputChar);
    }

    private void populateTransitions(File statesFile, File transitionsFile) {
        Scanner scan = null;

        try {
            scan = new Scanner(statesFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Tables are denormalized
        List<State> states = new ArrayList<State>();

        while (scan.hasNextLine()) {
            //
        }

        try {
            scan = new Scanner(transitionsFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while(scan.hasNextLine()) {
            String[] row = scan.nextLine().split(",");
            
        }
    }

}
