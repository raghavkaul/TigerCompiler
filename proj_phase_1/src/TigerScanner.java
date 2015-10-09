import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Raghav K on 9/17/15
 * Class representing high-level scanner
 */
public class TigerScanner {
    private FileReader infileReader;
    private DFA dfa;
    private int lineNum, columnNum;

    private boolean done;

    private boolean peeked;
    private Token peekedToken;

    private char invalidChar;
    private boolean invalidated;
    private int numErrors;

    public TigerScanner(File infile, File stateFile, File transitionFile) {
        try {
            this.infileReader = new FileReader(infile);
        } catch (IOException i) {
            i.printStackTrace();
        }

        lineNum = 1;
        columnNum = 0;

        dfa = new DFA(stateFile, transitionFile);
        done = false;
    }

    public Token peekToken() {
        if (!peeked) {
            peekedToken = nextToken();
            peeked = true;
        }
        return peekedToken;
    }

    public Token nextToken() {
        if (peeked) {
            peeked = false;
            return peekedToken;
        }
        // Reinitialize DFA
        dfa.returnToStart();

        // Initialize token data
        Character currChar;
        StringBuilder tokenLiteral = new StringBuilder();

        // Pre-process whitespace
        do {
            currChar = safeRead();
        } while (currChar == '\n' || currChar == ' ' || currChar == '\t');

        if (currChar == '\0')
            return new Token(TokenType.EOF_TOKEN,
                    "EOF",
                    lineNum,
                    columnNum);

        State currentState = dfa.getNextState(currChar);

        // Checks for invalid characters, unicode, UTF-16, etc.
        if (currentState.tokenType() == TokenType.INVALID) {
            System.out.println("No token matching at line " + getLineNum() + ":" + getColumnNum() + ": "  + tokenLiteral
                    + "\'" + currChar + "\'");
            numErrors++;
            return nextToken();
        }

        while (!done) {
            tokenLiteral.append(currChar);
            dfa.setNextState(currChar);

            try {
                currChar = Character.toChars(infileReader.read())[0];
                if (currChar == '\n') {
                    lineNum++;
                    columnNum = 1;
                } else {
                    columnNum++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalArgumentException e) {
                if (currChar == 'd')
                    return new Token(currentState.tokenType(),
                        tokenLiteral.toString(),
                        lineNum,
                        columnNum);
                else
                    return new Token(TokenType.EOF_TOKEN,
                            "EOF",
                            lineNum,
                            columnNum);
            }

            // If no transition is found, push back character and finish tokenizing
            if (dfa.getNextState(currChar).tokenType() == TokenType.INVALID) {
                invalidated = true;
                invalidChar = currChar;
                return new Token(currentState.tokenType(),
                        tokenLiteral.toString(),
                        lineNum,
                        columnNum - 1);
            } else { // If transition is found, keep going
                currentState = dfa.getNextState(currChar);
                columnNum++;
            }
        }
        // Returns null when scanner has no more tokens to return
        return null;
    }

    /**
     * Modularize-s calls to the file reader
     * Updates line and column numbers
     * @return the next character in the file buffer
     */
    private Character safeRead() {
        Character currChar = null;

        if (invalidated) {
            currChar = invalidChar;
            invalidated = false;
            return currChar;
        }

        try {
            currChar = Character.toChars(infileReader.read())[0];
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            return '\0';
        }

        columnNum++;

        if (currChar == '\n') {
            lineNum++;
            columnNum = 1;
        }
        return currChar;
    }

    public boolean hasErrors() {
        return numErrors != 0;
    }

    public int getNumErrors() {
        return numErrors;
    }

    public int getLineNum() {
        return lineNum;
    }

    public int getColumnNum() {
        return columnNum;
    }

    public String getCurrentIndex() {
        return "line: " + lineNum + ":" + columnNum;
    }
}