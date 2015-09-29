import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Raghav K on 9/17/15.
 */
public class DFA {
    public Map<StateInputWrapper, State> transitions;
    protected Map<String, State> states;
    private State startState;
    private State currState;
    private State errorState;
    private Set<Token> punctuationTokens = new HashSet<Token>();

    /**
     * @param statesFile a tsv with relations state -> token type associated with state
     * @param transitionsFile a tsv with relations state -> inputChar -> nextState
     */
    public DFA(File statesFile, File transitionsFile) {
        transitions = new HashMap<StateInputWrapper, State>();
        populateTransitions(statesFile, transitionsFile);
        startState = states.get("START_STATE");
        currState = startState;
        errorState = new State("ERROR", TokenType.INVALID);
    }

    /**
     * Gets next state given the current state and an arbitrary input
     * @param inputChar used for the transition
     * @return next state associated with character or null if no transition exists
     */
    public State getNextState(char inputChar) {
        currState = transitions.get(new StateInputWrapper(currState, inputChar));
        if (currState == null) {
            currState = errorState;
        }
        return currState;
    }

    public State getState() {
        return currState;
    }

    public void setState(State state) {
        currState = state;
    }

    public void returnToStart() {
        currState = startState;
    }

    private void populateTransitions(File statesFile, File transitionsFile) {
        final String infileDelimiter = "\t";
        Scanner scan = null;

        try {
            scan = new Scanner(statesFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Tables are denormalized
        // We have to read on table of State -> TokenType
        // And another of State -> Input character -> Next State
        states = new HashMap<String, State>();

        while (scan.hasNextLine()) {
            String[] row = scan.nextLine().split(infileDelimiter);
            states.put(row[0], new State(row[0], TokenType.valueOf(row[1])));
        }

        try {
            scan = new Scanner(transitionsFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Each line in transitions.csv is a tuple (state, inChar, outState)
        // Sequentially read lines, populating transitions table
        int i = 0;
        while(scan.hasNextLine()) {
            String[] row = scan.nextLine().split(infileDelimiter);

            // What follows is a fix of the worst <hack>
            State current = states.get(row[0]);
            State next = states.get(row[2]);
            // </hack>

            Set<Character> characters = regexHelper(row[1]);

            for (Character c : characters) {
                transitions.put(new StateInputWrapper(current, c), next);
            }
        }
    }

    /**
     * @param regexString string from transition table to parse, using primitive regex rules
     * Rules;
     * ^ : negation
     * ~ : wildcard alphanumeric and symbols
     * # : wildcard numeric
     * @ : wildcard alpha
     * @return a set of characters matching that primitive regex
     */
    protected Set<Character> regexHelper(String regexString) {
        Set<Character> validChars = new HashSet<Character>();
        Set<Character> exceptedChars = new HashSet<Character>();
        char[] symbols = new char[]
                {',',':',';','(',')','[',']','{','}','+','-','*','/','=','<','>','&','|', '.'};

        // Verify : refactor loops, can't increment a -> Z.
        switch(regexString.charAt(0)) {
            case '@':
                for (char alpha = 'A'; alpha <= 'Z'; alpha++) {
                    validChars.add(alpha);
                }
                for (char alpha = 'a'; alpha <= 'z'; alpha++){
                    validChars.add(alpha);
                }
                break;
            case '!':
                for (int i = 1; i < regexString.length(); i++) {
                    exceptedChars.add(regexString.charAt(i));
                }
            case '#':
                for (char numeric = '0'; numeric <= '9'; numeric++) {
                    validChars.add(numeric);
                }
                break;
            case '^':
                for (int i = 1; i < regexString.length(); i++) {
                    exceptedChars.add(regexString.charAt(i));
                }
            case '~':
                char alphanum;
                for (alphanum = 'A'; alphanum <= 'Z'; alphanum++) {
                    if (!exceptedChars.contains(alphanum)) {
                        validChars.add(alphanum);
                    }
                }
                for (alphanum = 'a';  alphanum <= 'z'; alphanum++) {
                    if (!exceptedChars.contains(alphanum)) {
                        validChars.add(alphanum);
                    }
                }
                for (alphanum = '0'; alphanum <= '9'; alphanum++) {
                    if (!exceptedChars.contains(alphanum)) {
                        validChars.add(alphanum);
                    }
                }
                for (int i = 0; i < symbols.length; i++) {
                    if (!exceptedChars.contains(symbols[i])) {
                        validChars.add(symbols[i]);
                    }
                }
                break;
            default:
                for (char c : regexString.toCharArray()) {
                    validChars.add(c);
                }

        }

        return validChars;

    }

}
