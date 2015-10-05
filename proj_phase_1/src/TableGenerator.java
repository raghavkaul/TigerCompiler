import java.io.File;
import java.util.*;

/**
 * Created by Raghav K on 10/4/15.
 */
public class TableGenerator {
    private Scanner infileScanner;
    private Set<Nonterminal> nonterminals;
    private Set<Terminal> terminals;

    public TableGenerator(File infile) throws Exception {
        infileScanner = new Scanner(infile);
        nonterminals = new HashSet<>();
        terminals = new HashSet<>();

        String[] ruleLiteralStr;
        Rule currRule = null;
        Nonterminal currNonterm = null;

        while (infileScanner.hasNextLine()) {
            ruleLiteralStr = infileScanner.nextLine().split(" ");
            String nontermName = ruleLiteralStr[0]
                    .substring(1, ruleLiteralStr[1].length() - 1); // Efficiency

            // Populate fields of rule
            for (int i = 0; i < ruleLiteralStr.length; i++) {
                if (ruleLiteralStr[i].charAt(0) == '<') {
                    // i-th term of expansion is a nonterminal. Check prev encountered nonterms
                    Nonterminal temp = new Nonterminal(ruleLiteralStr[i]
                            .substring(1, ruleLiteralStr[i].length() - 1));
                    if (!nonterminals.contains(temp)) {
                        nonterminals.add(temp);
                    }
                    currRule.addLexeme(temp);

                } else {
                    // TODO Terminal temp = new Terminal(ruleLiteralStr[i])
                    // i-th term of expansion is a terminal.
                }
            }
            currRule = new Rule();
            // TODO : lots of stuff.

            // Group rules for like nonterminals
            if (currNonterm != null && nontermName.equals(currNonterm.getName())) {
                // We've seen this nonterminal before, so add the rule to its' possible expansions
                // currNonterm.addExpansion(); // TODO
            } else {
                // Add the old nonterminal to our set
                nonterminals.add(currNonterm);

                // Create a new nonterminal to add rules to
                currNonterm = new Nonterminal(nontermName);

            }

            if (currNonterm != null && !nonterminals.contains(currNonterm)) {
                // We've
                nonterminals.add(currNonterm);
            } else {

            }

            for (String lexeme : ruleLiteralStr) {
                if (lexeme.charAt(0) == '<') {

                    // it's a nonterm
                } else {

                    // it's a terminal
                }
            }
        }

    }

}
