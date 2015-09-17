import java.util.Map;
import java.util.Set;

/**
 * Created by Raghav K on 9/17/15.
 */
public class DFA {
    private Set<State> states;
    private Set<Character> alphabet;
    private Map<StateInputWrapper, State> transitions;
    private State startState;
    private Set<State> acceptingStates;

    private static DFA DFAInstance;

    public State getNextState(State currState, char inputChar) {
        return transitions.get(new StateInputWrapper(currState, inputChar));
    }
}
