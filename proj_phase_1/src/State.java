import java.util.HashMap;
import java.util.Map;

/**
 * Created by Raghav K on 9/17/15.
 */
public class State {
    private String name;
    private Map<Character, State> transitions;
    private boolean isAccepting;


    public State(String name) {
        this.name = name;
        isAccepting = false;
        transitions = new HashMap<Character, State>();
    }

    public State(String name, boolean isAccepting) {
        this.name = name;
        this.isAccepting = isAccepting;
    }

    public void addTransition(Character inputChar, State nextState) {
        transitions.put(inputChar, nextState);
    }

    /**
     * @param inputChar read from token
     * @return next state given inputChar
     * @throws IllegalStateException if no valid transition exists
     */
    public State getNextState(Character inputChar) {
        State nextState = transitions.get(inputChar);

        if (nextState == null) {
            throw new IllegalStateException("No valid state found.");
        }

        return transitions.get(inputChar);
    }

    public boolean isAccepting() {
        return isAccepting;
    }
}
