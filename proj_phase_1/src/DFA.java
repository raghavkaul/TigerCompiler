import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Raghav K on 9/17/15.
 */
public class DFA {
    public Map<StateInputWrapper, State> transitions;
    protected List<State> states;
    private State startState;
    private State currState;
    private Set<Token> punctuationTokens = new HashSet<Token>();

    /**
     * @param statesFile a tsv with relations state -> token type associated with state
     * @param transitionsFile a tsv with relations state -> inputChar -> nextState
     */
    public DFA(File statesFile, File transitionsFile) {
        transitions = new HashMap<StateInputWrapper, State>();
        populateTransitions(statesFile, transitionsFile);
        startState = new State("START_STATE", TokenType.NON_ACCEPTING);
        currState = startState;
    }

    /**
     * Gets next state given the current state and an arbitrary input
     * @param inputChar used for the transition
     * @return next state associated with character or null if no transition exists
     */
    public State getNextState(char inputChar) {
        currState = transitions.get(new StateInputWrapper(currState, inputChar));
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
        states = new ArrayList<State>();

        while (scan.hasNextLine()) {
            String[] row = scan.nextLine().split(infileDelimiter);
            states.add(new State(row[0], TokenType.valueOf(row[1])));
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

            System.out.println(i++);
            // What follows is a fix of the worst <hack>
            State current = new State(row[0]);
            State next = new State(row[2]);
            // </hack>

            // TODO : call regex helper to help populate transitions
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
     * $ : wildcard numeric and symbols
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
                for (char alpha = 'A'; alpha < '[' && alpha >= 'a' && alpha <= 'z'; alpha++) {
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
                for (alphanum = 'A'; alphanum < '[' && alphanum >= 'a' && alphanum <= 'z'; alphanum++) {
                    if (!exceptedChars.contains(alphanum)) {
                        validChars.add(alphanum);
                    }
                }
                for (alphanum = '0'; alphanum <= '9'; alphanum++) {
                    if (!exceptedChars.contains(alphanum)) {
                        validChars.add(alphanum);
                    }
                }
                for (alphanum = 0; alphanum < symbols.length; alphanum++) {
                    if (!exceptedChars.contains(alphanum)) {
                        validChars.add(symbols[alphanum]);
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
