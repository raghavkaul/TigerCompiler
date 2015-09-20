import java.util.*;

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

    private DFA() {
        states = new HashSet<State>();
        states.add(new State("Start", false));




    }

    public State getNextState(State currState, char inputChar) {
        return transitions.get(currState).getNextState(inputChar);
    }
}
