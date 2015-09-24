import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Raghav K on 9/17/15
 * Class representing high-level scanner
 */
public class TigerScanner implements  AbstractScanner {
    private File infile;
    private Scanner infileScanner;
    private DFA dfa;
    private int lineNum = 0, columnNum = 0, prevTokLocation = 0;

    public TigerScanner(File stateFile, File transitionFile) {
        dfa = new DFA(stateFile, transitionFile);
    }

    public void initScanner(File infile) {
        this.infile = infile;

        try {
            this.infileScanner = new Scanner(this.infile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public Collection<Token> sillyScan(File infile) {
        List<Token> tokenList = new LinkedList<Token>();

        while(infileScanner != null && infileScanner.hasNext()) {
            String currLine = infileScanner.nextLine();

            for (int i = 0; i < currLine.length(); i++) {
                // Efficiency
                char currChar = currLine.charAt(i);
                State nextState = dfa.getNextState(currChar);

                // Move to next state if a valid one exists
                if(nextState != null) {
                    dfa.setState(nextState);
                } else {
                    // No valid state transition implies we've completed a token
                    State state = dfa.getState();

                    // Add the token represented by the current state to our token list
                    tokenList.add(new Token(state.tokenType(),
                            currLine.substring(prevTokLocation, i), lineNum, columnNum));

                    prevTokLocation = i + 1;
                    dfa.returnToStart();
                    dfa.setState(dfa.getNextState(currChar));
                }
                columnNum++;

            }
            lineNum++;
        }

        return tokenList;
    }

    @Override
    public Token peekToken() {
        // TODO implement
        return null;
    }

    @Override
    public Token nextToken() {
        String currLine = infileScanner.nextLine();
        char currChar = currLine.charAt(columnNum++);
        State currentState = dfa.getNextState(currChar);

        // TODO verify
        while (currentState != null) {
            dfa.setState(currentState);
            currChar = currLine.charAt(columnNum++);
            currentState = dfa.getNextState(currChar);
        }

        Token result = new Token(currentState.tokenType(),
                currLine.substring(prevTokLocation, columnNum - 1),
                lineNum,
                columnNum);

        prevTokLocation = columnNum;


        return result;
    }
}
