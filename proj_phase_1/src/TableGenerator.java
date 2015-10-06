import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * ParseTable factory and first-pass grammar parser class.
 */
public class TableGenerator {
    private Scanner infileScanner;
    private Set<Nonterminal> nonterminals;

    public TableGenerator(File infile) {
        try {
            infileScanner = new Scanner(infile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        nonterminals = new HashSet<>();
    }

    /**
     * Parses input file with grammar, returning list of rules with expansions
     * Grammar raw format takes the following format:
     * 1. Nonterminals are specified with opening/closing angle brackets '<>'
     * 2. Terminals are all in uppercase
     * 3. A derivation/rule has one nonterminal on the LHS, followed by '::='
     * This is then followed by a sequence of terminals or nonterminals
     * @return list of rules
     */
    public List<Rule> parseGrammar() {
        List<Rule> rules = new LinkedList<>();
        int ruleNo = 0;
        Rule currRule;
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
        HashSet<Terminal> firstSet = new HashSet<>(),
                followSet = new HashSet<>();

        for (Lexeme lexeme : rule.getExpansion()) {
            if (lexeme instanceof Terminal) {
                firstSet.add((Terminal) lexeme);
                if (((Terminal) lexeme).matches(TokenType.NIL)) {
                    firstSet.add(new Terminal(TokenType.NIL));
                }
            } else if (lexeme instanceof Nonterminal) {

            }
        }

        firstSet.forEach(rule::addToFirstSet);
        followSet.forEach(rule::addToFollowSet);
    }



    /**
     * Generates a parse table from a list of grammar rules
     * @param rules to populate table with
     * @return parse table mapping nonterminals and tokens to rules
     */
    public ParseTable generateParseTable(List<Rule> rules) {
        ParseTable result = new ParseTable();

        for (Rule rule : rules) {
            for (Terminal firstSetTerminal : rule.getFirstSet()) {
                result.addRule(rule.getParent(),
                        new Token(firstSetTerminal.getTokenType()),
                        rule);
                if (firstSetTerminal.getTokenType().equals(TokenType.NIL)) {
                    for (Terminal followSetTerminal : rule.getFollowSet()) {
                        result.addRule(rule.getParent(),
                                new Token(followSetTerminal.getTokenType()),
                                rule);
                    }
                }
            }
        }

        return result;
    }
}
