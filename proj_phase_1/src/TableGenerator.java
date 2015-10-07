import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.SynchronousQueue;

/**
 * ParseTable factory and first-pass grammar parser class.
 */
public class TableGenerator {
    private Scanner infileScanner;
    protected Map<String, Nonterminal> nonterminals;

    private List<Rule> rules;

    public TableGenerator(File infile) {
        try {
            infileScanner = new Scanner(infile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        nonterminals = new HashMap<String, Nonterminal>();
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
        Nonterminal currNonterm = new Nonterminal("<tiger-program>");

        while (infileScanner.hasNextLine()) {
            String[] ruleLiteralStr = infileScanner.nextLine().split(" ");

            // Eliminate blank lines
            if (ruleLiteralStr.length < 3) { continue; }

            String nontermName = ruleLiteralStr[0];
            if (nonterminals.get(nontermName) != null) {
                currNonterm = nonterminals.get(nontermName);
            }
            else{
                currNonterm = new Nonterminal(nontermName);
                nonterminals.put(nontermName, currNonterm);
            }
            currRule = new Rule(ruleNo++);
            currRule.setParent(currNonterm);

            // Populate fields of rule
            for (int i = 2; i < ruleLiteralStr.length; i++) {
                Lexeme temp = new Nonterminal("fuck");
                // Checking syntax of grammar.txt
                if (ruleLiteralStr[i].startsWith("<")) {
                    if (nonterminals.containsKey(ruleLiteralStr[i]))
                        temp = nonterminals.get(ruleLiteralStr[i]);
                    else {
                        temp = new Nonterminal(ruleLiteralStr[i]);
                        nonterminals.put(ruleLiteralStr[i], (Nonterminal) temp);
                    }
                } else {
                    temp = new Terminal(ruleLiteralStr[i]);
                }
                currRule.addLexeme(temp);
            }

            // Add to list of rules
            rules.add(currRule);

            currNonterm.addExpansion(currRule);

        }

        this.rules = rules;
        return rules;
    }

    /**
     * Generates the first and follow set for rule r
     * Then adds those sets to globals firstSets and followSets
     * @param rule to generate tables for some nonterminals
     */
    public Rule updateFirstSet(Rule rule, int i, Set<Nonterminal> visitedNT) {
        HashSet<Terminal> firstSet = new HashSet<Terminal>();

        if (i > rule.getExpansion().size() - 1) {
            return rule;
        }
        Lexeme lexeme = rule.getExpansion().get(i);

        if (lexeme instanceof Terminal) {
            if (((Terminal) lexeme).matches(TokenType.NIL)) {
                firstSet.addAll(updateFirstSet(rule, i + 1, visitedNT).getFirstSet());
            } else {
                firstSet.add((Terminal) lexeme);
            }

        } else {
            Nonterminal nt = (Nonterminal) lexeme;
            if (!visitedNT.contains(nt)) {
                visitedNT.add(nt);
                for (Rule r : nt.getDerivations()) {
                    firstSet.addAll(updateFirstSet(r, 0, visitedNT).getFirstSet());
                }
                if (firstSet.contains(new Terminal("NIL"))) {
                    if (i != rule.getExpansion().size() - 1)
                        firstSet.remove(new Terminal("NIL"));
                    firstSet.addAll(updateFirstSet(rule, i + 1, visitedNT).getFirstSet());
                }
            }
        }

        rule.addToFirstSet(firstSet);

//        if (((Terminal) lexeme).matches(TokenType.NIL)) {
//            System.out.println("hello");
//            firstSet.addAll(updateFirstSet(rule, i+1, visitedNT).getFirstSet());
//        }

        return rule;
    }

    public List<Rule> updateRuleFirstFollowSets(List<Rule> rules) {
        List<Rule> updatedRules = new LinkedList<>();
        for (int i = 0; i < rules.size(); i++) {
            Set<Nonterminal> dummy = new HashSet<>();
            Rule temp = updateFirstSet(rules.get(i), 0, dummy);
            updatedRules.add(temp);
        }
        return updatedRules;
    }

    /**
     * Generates a parse table from a list of grammar rules
     * @param rules to populate table with, each knowing its' own set
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
