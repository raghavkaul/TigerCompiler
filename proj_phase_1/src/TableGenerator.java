import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Raghav K on 10/4/15.
 */
public class TableGenerator {
    private Scanner infileScanner;
    private List<Rule> rules;
    private Set<Nonterminal> nonterminals;
    private Map<Rule, HashSet<Terminal>> firstSets, followSets;

    public TableGenerator(File infile) {
        firstSets = new HashMap<Rule, HashSet<Terminal>>();
        followSets = new HashMap<Rule, HashSet<Terminal>>();

        try {
            infileScanner = new Scanner(infile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        rules = new LinkedList<Rule>();
        nonterminals = new HashSet<Nonterminal>();
    }

    /**
     * Parses input file with grammar, returning list of rules with expansions
     * Grammar takes the following format:
     * 1. Nonterminals are specified with opening/closing angle brackets '<>'
     * 2. Terminals are all in uppercase
     * 3. A derivation/rule has one nonterminal on the LHS, followed by '::='
     * This is then followed by a sequence of terminals or nonterminals
     * @return global list of rules
     */
    public List<Rule> parseGrammar() {
        int ruleNo = 0;
        Rule currRule = null;
        Nonterminal currNonterm = null;

        while (infileScanner.hasNextLine()) {
            String[] ruleLiteralStr = infileScanner.nextLine().split(" ");

            // Eliminate blank lines
            if (ruleLiteralStr.length == 0) { continue; }

            String nontermName = ruleLiteralStr[0]
                    .substring(1, ruleLiteralStr[1].length() - 1); // Efficiency
            currRule = new Rule(ruleNo++);

            // Populate fields of rule
            for (int i = 2; i < ruleLiteralStr.length; i++) {
                Lexeme temp;
                // Checking syntax of grammar.txt
                temp = ruleLiteralStr[i].charAt(0) == '<' ?
                        new Nonterminal(ruleLiteralStr[i]
                                .substring(1, ruleLiteralStr[i].length() - 1)) :
                        new Terminal(ruleLiteralStr[i]);
                currRule.addLexeme(temp);
            }

            // Add to list of rules
            rules.add(currRule);

            // Group rules by the nonterminal they belong to
            if (currNonterm != null && nontermName.equals(currNonterm.getName())) {
                // We've seen this nonterminal before, so add the rule to its' possible expansions
                currNonterm.addExpansion(currRule);
            } else {
                // Create a new nonterminal to add rules to
                currNonterm = new Nonterminal(nontermName);
                nonterminals.add(currNonterm);
                ruleNo = 0;
            }
        }

        return rules;
    }

    /**
     * Generates the first and follow set for rule r
     * Then adds those sets to globals firstSets and followSets
     * @param rule to generate sets for
     */
    private void generateFirstFollowSet(Rule rule) {

    }


    public ParseTable generateParseTable() {
        ParseTable result = new ParseTable();
        for (Rule rule : rules) {
            for (Terminal terminal : firstSets.get(rule)) {
                result.addRule(rule.getParent(), terminal.getTokenType(), rule);
                if (terminal.getTokenType().equals(TokenType.NIL)) {
                    for (Terminal followTerminal : rule.getFollowSet()) {
                        result.addRule(rule.getParent(), followTerminal.getTokenType(), rule);
                    }
                }
            }
        }
        return result;
    }
}
