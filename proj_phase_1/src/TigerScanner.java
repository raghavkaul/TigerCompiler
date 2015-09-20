import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Raghav K on 9/17/15
 * Class representing high-level scanner
 */
public class TigerScanner {
    private File infile;
    private Scanner infileScanner;
    private DFA dfa;

    public TigerScanner(File stateFile, File transitionFile) {
        dfa = new DFA(stateFile, transitionFile);
    }

    public void scan(File infile) {
        this.infile = infile;

        try {
            this.infileScanner = new Scanner(this.infile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        List<Token> tokenList = new LinkedList<Token>();

        // FIXME : assumes whitespace delimited file only
        while(infileScanner != null && infileScanner.hasNext()) {
            String[] currLine = infileScanner.nextLine().split(" ");

            for (String s : currLine) {
                for (int i = 0; i < s.length(); i++) {
                    dfa.setState(dfa.getNextState(s.charAt(i)));
                }

                tokenList.add(new Token(dfa.getState().tokenType(), s));
            }
        }
    }
}
