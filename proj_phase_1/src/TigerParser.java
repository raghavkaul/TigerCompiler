import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

/**
 * Created by Raghav K on 9/28/15.
 */
public class TigerParser {
    private static final String statesFilename = "./data/states.csv";
    private static final String transitionsFilename = "./data/transitions.csv";
    private Set<Rule> rules;
    private Stack<Lexeme> stack;

    public TigerParser(File infile) {

        // Initialize scanner
        TigerScanner infileScanner = null;
        infileScanner = new TigerScanner(infile,
                new File(statesFilename), new File(transitionsFilename));

        rules = populateRules();

        // Initialize stack to contain end symbol and Tiger-prog nonterminal.
        stack = new Stack<Lexeme>();

        stack.push(new Terminal()); // end symbol
        stack.push(new Nonterminal()); // <Tiger-prog>

        while (infileScanner.peekToken() != null) {
            Token nextToken = infileScanner.nextToken();

        }
    }

    private Set<Rule> populateRules() {
        return null;
    }
}
