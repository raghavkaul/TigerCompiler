import java.util.HashMap;
import java.util.Map;

/**
 * Created by Raghav K on 9/17/15.
 */
public class State {
    private String name;
    private Map<Character, State> transitions;
    private TokenType tokenType;


    public State(String name) {
        this.name = name;
        transitions = new HashMap<Character, State>();
    }

    public State(String name, TokenType tokenType) {
        this.name = name;
        this.tokenType = tokenType;
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

    public TokenType isAccepting() {
        return tokenType;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof State) {
            if (((State) o).getName() == this.getName()) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }
}
