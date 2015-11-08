import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class TableGen {
	private Scanner infileScanner;
	private Map<String, Set<String>> firstset, followset;
	private List<Rule> rules;
	private Map<String,Rule> parsertable;
	
	
	public TableGen(File infile){
		
		rules = new ArrayList<>();
		
		try {
            infileScanner = new Scanner(infile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
		
		
		int line=1;
		while(infileScanner.hasNextLine()){
			
			String ruleLiteralStr = infileScanner.nextLine();

			if(ruleLiteralStr.split("::=").length == 2) {
				Rule rule = new Rule(ruleLiteralStr,line++);
				rules.add(rule);
			}
		}
		infileScanner.close();
		
	}
	
	public void generateFirstSets(){
		boolean firstSetchanged = true;
		
		firstset = new HashMap<>();
		
		//Add First set for all tokens or terminals
		for(TokenType token: TokenType.values()){
			Set<String> tokenSet = new HashSet<>();
			tokenSet.add(token.toString());
			firstset.put(token.toString(), tokenSet);
		}
		//Remove unwanted Terminals
		firstset.remove(TokenType.COMMENT_BEGIN.toString());
		firstset.remove(TokenType.COMMENT_END.toString());
		firstset.remove(TokenType.NON_ACCEPTING.toString());
		firstset.remove(TokenType.TIGER_PROG.toString());
		firstset.remove(TokenType.INVALID.toString());
		
		//First set code
		while(firstSetchanged){
			//Set change to false
			firstSetchanged = false;
			for(Rule rule: rules){
				
				//Get Rule's first Set
				Set<String> RuleFirstSet = firstset.get(rule.getName());
				//If no Set exists add one and make change = true
				if(RuleFirstSet==null){
					RuleFirstSet = new HashSet<>();
					firstset.put(rule.getName(), RuleFirstSet);
					firstSetchanged = true;
				}
				
				//Get a new rhs for this rule
				Set<String> rhs = new HashSet<>();
				
				//Get first set of B[1](See Algo)
				String firstLexeme = rule.getExpansion().get(0);
				Set<String> firstSetofLexeme = firstset.get(firstLexeme);
				if(firstSetofLexeme == null){
					firstSetofLexeme = new HashSet<>();
					firstset.put(firstLexeme, firstSetofLexeme);
					firstSetchanged = true;
				}
				else{
					//Special case, if B[1] is NIL
					if(firstLexeme.equals("NIL")){
						rhs.add("NIL");
					}
					else {
						//Add all values of firstset of B[1] except NIL
						rhs.addAll(firstSetofLexeme);
						if(firstSetofLexeme.contains(TokenType.NIL.toString())){
							rhs.remove(TokenType.NIL.toString());
						}
					}
				}
				
				//Get the expansion list
				List<String> expansion = rule.getExpansion();
				
				int expansioncount =0;
				
				//Iterate on expansion list till FIRST(B[i]) belongs to NIL
				for(int i=0; i<expansion.size(); i++){
					expansioncount = i;
					
					//Get FIRST set of B[i]
					Set<String> expansionNullFirstSet = firstset.get(expansion.get(i));
					if(expansionNullFirstSet==null){
						expansionNullFirstSet = new HashSet<>();
						firstset.put(expansion.get(i), expansionNullFirstSet);	
						firstSetchanged = true;
						//Break as can't say about its first set as of now
						break;
					}
					//Check if first set contains NIL
					else if(expansionNullFirstSet.contains(TokenType.NIL.toString()) && i<(expansion.size()-1)){
						Set<String> expansionFirstSet = firstset.get(expansion.get(i+1));
						if(expansionFirstSet==null){
							expansionFirstSet = new HashSet<>();
							firstset.put(expansion.get(i+1), expansionFirstSet);
							firstSetchanged = true;
						}
						else{
							//Add all fist set of B[i+1] in rhs if B[i] contains NIL
							rhs.addAll(expansionFirstSet);
							if(expansionFirstSet.contains(TokenType.NIL.toString())){
								rhs.remove(TokenType.NIL.toString());
							}
						}
						
					}
					//else break out
					else{break;}
				}
				
				//If we iterated till the last element
				if(expansioncount == expansion.size()-1){
					Set<String> expansionLast = firstset.get(expansion.get(expansioncount));
					if(expansionLast==null){
						expansionLast = new HashSet<>();
						firstset.put(expansion.get(expansioncount), expansionLast);
						firstSetchanged = true;
					}
					//If expansion of last element contained NIL
					if(expansionLast.contains(TokenType.NIL.toString())){
						//Add NIL to rhs
						rhs.add(TokenType.NIL.toString());
					}
				}
				
				//If This rules first set contains everything in rhs then continue
				if(!RuleFirstSet.containsAll(rhs)) {
					//add everything to RuleFirstSet.
					RuleFirstSet.addAll(rhs);
					firstSetchanged = true;
				}
			}
		}
		
	}
	
	public void generatefollowsets(){
		boolean followsetschanged = true;
		followset = new HashMap<>();
		String firstruleOfgrammar = rules.get(0).getName();
		Set<String> firstrulefollowset = new HashSet<>();
		firstrulefollowset.add(TokenType.EOF_TOKEN.toString());
		followset.put(firstruleOfgrammar, firstrulefollowset);
		
		while(followsetschanged){
			followsetschanged = false;
			for(Rule rule:rules){
				
				//Copy present rules follow set into trailer(TRAILER <- FOLLOW(A))
				Set<String> presetnRuleFollowSet = followset.get(rule.getName());
				if(presetnRuleFollowSet==null){
					presetnRuleFollowSet = new HashSet<>();
					followset.put(rule.getName(), presetnRuleFollowSet);
					followsetschanged = true;
				}
				Set<String> trailer = new HashSet<>(followset.get(rule.getName()));
				
				List<String> expansion = rule.getExpansion();
				for(int i = expansion.size()-1; i>=0; i--){
					String currexpansion = expansion.get(i);
					if(!isTerminal(currexpansion)){
						Set<String> expansionfollowset = followset.get(currexpansion);
						if(expansionfollowset==null){
							expansionfollowset = new HashSet<>();
							followset.put(currexpansion, expansionfollowset);
							followsetschanged = true;
						}
						if(!expansionfollowset.containsAll(trailer)){
							expansionfollowset.addAll(trailer);
							followsetschanged = true;
						}
						
						if(firstset.get(currexpansion).contains(TokenType.NIL.toString())){
							trailer.addAll(firstset.get(currexpansion));
							trailer.remove(TokenType.NIL.toString());
						}
						else{
							trailer.clear();
							trailer.addAll(firstset.get(currexpansion));
						}
					}
					else{
						trailer.clear();
						trailer.addAll(firstset.get(currexpansion));
					}
					
				}
			}
		}
	}
	
	public void generateParsertable(){
		generateFirstSets();
		generatefollowsets();
		parsertable = new HashMap<>();
		for(Rule rule: rules){
			List<String> expansion = rule.getExpansion();
			boolean addfollowset = true;
			Set<String> rulefirstset = new HashSet<>();
			for(String lexeme: expansion){
				if(!isTerminal(lexeme)){
					Set<String> firstsetexpansion = firstset.get(lexeme);
					if(!firstsetexpansion.contains(TokenType.NIL.toString())){
						rulefirstset.addAll(firstsetexpansion);
						addfollowset = false;
						break;
					}
					else{
						rulefirstset.addAll(firstsetexpansion);
						rulefirstset.remove(TokenType.NIL.toString());
					}
				}
				else{
					rulefirstset.add(lexeme);
					if(!lexeme.equals(TokenType.NIL.toString()))
						addfollowset = false;
					break;
				}
			}

			if(addfollowset){
				rulefirstset.add(TokenType.NIL.toString());
			}

			for(String terminal: rulefirstset){
				parsertable.put(rule.getName()+", "+terminal, rule);
			}

			if(addfollowset){
				for(String terminal:followset.get(rule.getName())){
					parsertable.put(rule.getName()+", "+terminal, rule);
				}
			}
		}
	}

	public Map<String, Rule> getParserTable() {
		return parsertable;
	}

	public void printAllRules(){
		int count =1;
		for(Rule rule: rules){
			
			System.out.print(count);
			System.out.print(". ");
			System.out.print(rule.getName());
			System.out.print(" ::= \n");
			for(String lexemes: rule.getExpansion()){
				System.out.println(lexemes);
			}
			count++;
		}
	}
	
	public void printAllFirstsets(){
		System.out.println("----FirstSets----");
		for(Map.Entry<String, Set<String>> entry: firstset.entrySet()){
			System.out.print(entry.getKey());
			System.out.print(": ");
			for(String tok: entry.getValue()){
				System.out.print(tok);
				System.out.print(", ");
			}
			System.out.print("\n");
		}
	}
	
	public void printAllFollowsets(){
		System.out.println("----FollowSets----");
		for(Map.Entry<String, Set<String>> entry: followset.entrySet()){
			System.out.print(entry.getKey());
			System.out.print(": ");
			for(String tok: entry.getValue()){
				System.out.print(tok);
				System.out.print(", ");
			}
			System.out.print("\n");
		}
	}
	
	public void printparsertable(){
		System.out.println("---Parser Table---");
		for(String entry: parsertable.keySet()){
			System.out.print(entry);
			System.out.print(" -> ");
			System.out.println(parsertable.get(entry));
			System.out.print("\n");
		}
		System.out.println(parsertable.keySet().size());
	}
	
	public boolean isTerminal(String lexeme){
        return lexeme.contains("<");
	}

	public static void main(String[] args) {
		TableGen parserTable = new TableGen(new File("/Users/harshitjain/Documents/workspace/TableGenerator/grammar.txt"));
		//parserTable.printAllRules();
		parserTable.generateFirstSets();
		parserTable.printAllFirstsets();
		parserTable.generatefollowsets();
		parserTable.printAllFollowsets();
		parserTable.generateParsertable();
		parserTable.printparsertable();
	}

}
