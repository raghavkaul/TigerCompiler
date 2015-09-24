import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Raghav K on 9/17/15
 * Class representing high-level scanner
 */
public class TigerScanner implements  AbstractScanner {
    private Scanner infileScanner;
    private String currLine;
    private DFA dfa;
    private int lineNum, columnNum, prevTokLocation;

    public TigerScanner(File infile, File stateFile, File transitionFile) {
        try {
            this.infileScanner = new Scanner(infile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (infileScanner.hasNextLine()) {
            currLine = infileScanner.nextLine();
        } else {
            System.out.println("No code in file to be compiled.");
        }

        lineNum = 0;
        columnNum = 0;
        prevTokLocation = 0;

        dfa = new DFA(stateFile, transitionFile);
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
        // Clone state data
        DFA dfaCopy = dfa;
        Scanner infileScannerCopy = infileScanner;
        int columnNumCopy = columnNum;

        if (!infileScannerCopy.hasNext()) {
            currLine = infileScannerCopy.nextLine();
        }

        // Get first character and reinitialize DFA
        char currChar = currLine.charAt(columnNumCopy++);

        // Restore DFA to start state
        dfaCopy.returnToStart();
        State currentState = dfaCopy.getNextState(currChar);

        // Advance DFA until error no transition exists
        while (currentState != null) {
            dfaCopy.setState(currentState);
            currChar = currLine.charAt(columnNumCopy++);
            currentState = dfaCopy.getNextState(currChar);
        }

        Token result = new Token (currentState.tokenType(),
                currLine.substring(prevTokLocation, columnNumCopy - 1),
                lineNum,
                columnNumCopy);

        return result;
    }

    @Override
    public Token nextToken() {
        if (!infileScanner.hasNext()) {
            currLine = infileScanner.nextLine();
        }

        char currChar = 'i';
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
