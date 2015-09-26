import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Raghav K on 9/17/15
 * Class representing high-level scanner
 */
public class TigerScanner implements  AbstractScanner {
    private FileReader infileReader;
    private DFA dfa;
    private int lineNum, columnNum;

    public TigerScanner(File infile, File stateFile, File transitionFile) {
        try {
            this.infileReader = new FileReader(infile);
        } catch (IOException i) {
            i.printStackTrace();
        }

        lineNum = 0;
        columnNum = 0;

        dfa = new DFA(stateFile, transitionFile);
    }

    @Override
    public Token peekToken() {
        return null;
    }

    @Override
    public Token nextToken() {
        // Reinitialize DFA
        dfa.returnToStart();

        // Initialize token data
        Character currChar;
        StringBuilder tokenLiteral = new StringBuilder();

        // Pre-process whitespace
        do {
            currChar = safeRead();
        } while (currChar == '\n' || currChar == ' ');

        State currentState = dfa.getNextState(currChar);

        if (currentState == null) {
            return new Token(TokenType.INVALID,
                    currChar.toString(),
                    lineNum,
                    columnNum);
        }

        while (true) {
            tokenLiteral.append(currChar);
            dfa.setState(currentState);

            try {
                currChar = Character.toChars(infileReader.read())[0];
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (dfa.getNextState(currChar) == null) {
                try {
                    infileReader.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            } else {
                currentState = dfa.getNextState(currChar);
                try {
                    infileReader.mark(Integer.MAX_VALUE);
                    columnNum++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new Token(currentState.tokenType(),
                tokenLiteral.toString(),
                lineNum,
                columnNum);
    }

    /**
     * Modular-izes calls to the file reader
     * Updates line and column numbers
     * @return the next character in the file buffer
     */
    private Character safeRead() {
        Character currChar = null;

        try {
            currChar = Character.toChars(infileReader.read())[0];
        } catch (IOException e) {
            e.printStackTrace();
        }

        columnNum++;

        if (currChar != null && currChar == '\n') {
            lineNum++;
        }

        try {
            infileReader.mark(Integer.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return currChar;
    }
}