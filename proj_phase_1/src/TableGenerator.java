import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.SynchronousQueue;

/**
 * ParseTable factory and first-pass grammar parser class.
 */
public class TableGenerator {
    private Scanner infileScanner;
    public Map<String, Nonterminal> nonterminals;

    public List<Rule> rules;

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
            currRule = new Rule(++ruleNo);
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

    public Set<Terminal> computeMatches(List<Lexeme> lexemes, int current) {
        Lexeme lexeme = lexemes.get(current);
        Set<Terminal> result = new HashSet<>();

        if (lexeme instanceof Terminal) {
            result.add((Terminal) lexeme);
            return result;
        } else {
            for (Rule rule : ((Nonterminal) lexeme).getDerivations()) {
                Set<Terminal> toAdd = computeMatches(rule.getExpansion(), 0);
                boolean isNullable = false;
                if (toAdd.contains(new Terminal(TokenType.NIL))) {
                    isNullable = true;
                }
                if (isNullable) {
                    if (current == rule.getExpansion().size() - 1) {
                        return result;
                    } else {
                        result.addAll(computeMatches(rule.getExpansion(), ++current));
                    }
                }
                result.addAll(toAdd);
            }
            return result;
        }
    }

    /**
     * Generates a parse table from a list of grammar rules
     * @return parse table mapping nonterminals and tokens to rules
     */
    public ParseTable generateParseTable() {
        ParseTable pt = new ParseTable();
        for (Nonterminal nt : nonterminals.values()) {
            for (Rule rule : nt.getDerivations()) {
                for (Terminal t: computeMatches(rule.getExpansion(), 0)) {
                    pt.addRule(nt, new Token(t.getTokenType()), rule);
                }
            }
        }
        return pt;
    }

    public Map<String, Nonterminal> getNonterminals() {
        return nonterminals;
    }

    public Set getFirstSet(Nonterminal nt, Set<Nonterminal> visitedNT) {
        HashSet<TerminalRuleWrapper> set = new HashSet<TerminalRuleWrapper>();

        for (Rule rule : nt.getDerivations()) {
            List<Lexeme> lexemes = rule.getExpansion();
            if (lexemes.get(0) instanceof Terminal) {
                set.add(new TerminalRuleWrapper((Terminal) lexemes.get(0),rule));
            } else {
                if (!visitedNT.contains(nt)) {
                    for (int i = 0; i < lexemes.size(); i++) {
                        HashSet<TerminalRuleWrapper> nullCheck = new HashSet<>();
                        if (lexemes.get(i) instanceof Nonterminal) {
                            visitedNT.add((Nonterminal) lexemes.get(i));
                            nullCheck.addAll(getFirstSet((Nonterminal) lexemes.get(i), visitedNT));
                            if (nullCheck.contains(new TerminalRuleWrapper(new Terminal("NIL"), rule))) {
                                if (i != lexemes.size() - 1) {
                                    nullCheck.remove(new TerminalRuleWrapper(new Terminal("NIL"), rule));
                                    continue;
                                }
                                set.addAll(nullCheck);
                            } else {
                                set.addAll(nullCheck);
                                break;
                            }
                        } else {
                            set.add(new TerminalRuleWrapper((Terminal) lexemes.get(i), rule));
                            break;
                        }
                    }
                }

//                Nonterminal nt = (Nonterminal) lexeme;
//                if (!visitedNT.contains(nt)) {
//                    visitedNT.add(nt);
//                    for (Rule r : nt.getDerivations()) {
//                        firstSet.addAll(updateFirstSet(r, 0, visitedNT).getFirstSet());
//                    }
//                    if (firstSet.contains(new TerminalRuleWrapper(new Terminal("NIL"), rule))) {
//                        if (i != rule.getExpansion().size() - 1)
//                            firstSet.remove(new TerminalRuleWrapper(new Terminal("NIL"), rule));
//                        firstSet.addAll(updateFirstSet(rule, i + 1, visitedNT).getFirstSet());
//                    }
//                }
            }
        }

        return set;
    }

    /**
     * Generates the first and follow set for rule r
     * Then adds those sets to globals firstSets and followSets
     * @param rule to generate tables for some nonterminals
     */
    public Rule updateFirstSet(Rule rule, int i, Set<Nonterminal> visitedNT) {
        HashSet<TerminalRuleWrapper> firstSet = new HashSet<>();

        if (i > rule.getExpansion().size() - 1) {
            return rule;
        }
        Lexeme lexeme = rule.getExpansion().get(i);

        if (lexeme instanceof Terminal) {
            if (((Terminal) lexeme).matches(TokenType.NIL)) {
                firstSet.addAll(updateFirstSet(rule, i + 1, visitedNT).getFirstSet());
            } else {
                firstSet.add(new TerminalRuleWrapper((Terminal) lexeme, rule));
            }

        } else {
            Nonterminal nt = (Nonterminal) lexeme;
            if (!visitedNT.contains(nt)) {
                visitedNT.add(nt);
                for (Rule r : nt.getDerivations()) {
                    firstSet.addAll(updateFirstSet(r, 0, visitedNT).getFirstSet());
                }
                if (firstSet.contains(new TerminalRuleWrapper(new Terminal("NIL"), rule))) {
                    if (i != rule.getExpansion().size() - 1)
                        firstSet.remove(new TerminalRuleWrapper(new Terminal("NIL"), rule));
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

    public Rule updateFollowSet(Rule rule, int i, Set<Nonterminal> visitedNT) {
        if (i < 0)
            return rule;
        HashSet<TerminalRuleWrapper> followSet = new HashSet<>();
        List<Lexeme> list = rule.getExpansion();
        for (int x = list.size() - 1; x >= 0; x--) {
            Lexeme lexeme = list.get(x);
            for (Rule r : rule.getParent().getDerivations())
                followSet.addAll(r.getFollowSet());
            if (lexeme instanceof Nonterminal) {
                Nonterminal nt = (Nonterminal) lexeme;
                followSet.addAll(rule.getFollowSet());
                HashSet<TerminalRuleWrapper> nilCheck = new HashSet<>();
                for(Rule r : nt.getDerivations()) {
                    nilCheck.addAll(r.getFirstSet());
                }
                if (nilCheck.contains(new TerminalRuleWrapper(new Terminal("NIL"), rule))){
                    followSet.addAll(nilCheck);
                    followSet.remove(new TerminalRuleWrapper(new Terminal("NIL"), rule));
                } else {
                    followSet = nilCheck;
                }
            } else {
                followSet.add(new TerminalRuleWrapper((Terminal) lexeme, rule));
            }
        }

        rule.addToFollowSet(followSet);

        return rule;
    }

    public List<Rule> updateRuleFirstFollowSets(List<Rule> rules) {
        List<Rule> updatedRules = new LinkedList<>();
        for (Rule rule : rules) {
            Set<Nonterminal> dummy = new HashSet<>();
            Rule temp = updateFirstSet(rule, 0, dummy);
            temp = updateFollowSet(temp, 0, dummy);
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
            if (rule.getExpansion().get(0) instanceof Terminal
                    && ((Terminal) rule.getExpansion().get(0))
                    .getTokenType().equals(TokenType.LET)) {
                // Tiger-prog rule base case -- add eof token to follow set
                Set<TerminalRuleWrapper> dummy = new HashSet<>();
                dummy.add(new TerminalRuleWrapper(new Terminal(TokenType.EOF_TOKEN), rule));
                rule.addToFollowSet(dummy);
            }
            for (TerminalRuleWrapper firstSetTerminal : rule.getFirstSet()) {
                result.addRule(rule.getParent(),
                        new Token(firstSetTerminal.t.getTokenType()),
                        firstSetTerminal.r);
                if (firstSetTerminal.t.getTokenType().equals(TokenType.NIL)) {
                    for (TerminalRuleWrapper followSetTerminal : rule.getFollowSet()) {
                        result.addRule(rule.getParent(),
                                new Token(followSetTerminal.t.getTokenType()),
                                followSetTerminal.r);
                    }
                }
            }
        }

        return result;
    }
}
