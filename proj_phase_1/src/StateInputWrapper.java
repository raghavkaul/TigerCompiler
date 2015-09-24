/**
 * Created by user1316 on 9/17/15.
 */
public class StateInputWrapper {
    private State state;
    private char inputChar;

    public StateInputWrapper(State state, char inputChar) {
        this.state = state;
        this.inputChar = inputChar;
    }

    public State getState() {
    	return state;
    }

    public char getInput() {
    	return inputChar;
    }

    @Override
    public String toString() {
    	return state.toString() + "\nInput Char: " + inputChar;
    }
}
