//package proj_phase_1.src;
import java.io.File;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;


public class IRCodeGenerator {
    public Map<String, String> tokenToIR;
    private int intCount;
    private int floatCount;
    private int loopCount;
    
    private String intTemp;
    private String floatTemp;
    private String loopTemp;
    
    
    private ParseTree irParseTree;
    private VarTable irVarTable;
    private TypeTable irTypeTable;
    private FunctionTable irFuncTable;
    
    public Set<ParseTree> visitedSet;
    public Set<String>  unusableStrings;
    public Set<String> dontExpandStrings;
    public List<String> IRCodeList;
    public List<Map.Entry<String, String>> exprList;
    public Stack<String> loopStack;
    

  //  public void recurseParseTree(ParseTree root){

    public IRCodeGenerator(ParseTree _irParseTree, VarTable _irVarTable, TypeTable _irTypeTable, FunctionTable _irFuncTable){
    	
    	irParseTree = _irParseTree;
    	irVarTable  = _irVarTable;
    	irTypeTable = _irTypeTable;
    	irFuncTable = _irFuncTable;
    	
        intCount =0;
        floatCount =0;
        loopCount = 0;
      
        
        intTemp = "i";
        floatTemp = "f";
        loopTemp = "afterbranch_part";
        
        exprList = new ArrayList<>();
        loopStack = new Stack<>();
        
        

        tokenToIR = new HashMap<String, String>();
        tokenToIR.put(":=","assign");
        tokenToIR.put("+", "add");
        tokenToIR.put("-", "sub");
        tokenToIR.put("*","mult");
        tokenToIR.put("/","div");
        tokenToIR.put("&","and");
        tokenToIR.put("|","or");
        tokenToIR.put("", "break");
        tokenToIR.put("=", "brneq");
        tokenToIR.put("<>", "breq");
        tokenToIR.put("<=", "brgt");
        tokenToIR.put(">=", "brlt");
        tokenToIR.put("<", "brgeq");
        tokenToIR.put(">" , "brleq");
        

        IRCodeList = new ArrayList<String>();


    }

    
    //Parse the fucking Treeeee
    //<tiger-program> ::= LET <declaration-segment> IN <stat-seq> END
    public List<String> generateIrCode(){
    	
    	ParseTree root  = irParseTree;
    			
    	
    	//Check if root is ok
    	if(root!=null && root.getSymbolName().equals("<tiger-program>")){
    		
    		ParseTree declarationSegmentNode = root.getChildren().get(1);  		
    		generateDeclarationSegment(declarationSegmentNode);
    		
    		String mainLabel = "main:";
    		IRCodeList.add(mainLabel);
    		
    		ParseTree statSeqNode = root.getChildren().get(3);
    		generateStatSeq(statSeqNode);

			//Return
			IRCodeList.add("return, , ,");
    	}
    			
    	return IRCodeList;
    	
    }
   

    //<declaration-segment> ::= <type-declaration-list> <var-declaration-list> <funct-declaration-list>
    private void generateDeclarationSegment(ParseTree declarationSegmentNode){
    	
    	ParseTree varDeclarationListNode = declarationSegmentNode.getChildren().get(1);
    	ParseTree functDeclarationListNode = declarationSegmentNode.getChildren().get(2);
    	
    	if(!varDeclarationListNode.getChildren().get(0).getSymbolName().equals("NIL")){
    		generateVarDeclarationList(varDeclarationListNode);
    	}
    	
    	if(!functDeclarationListNode.getChildren().get(0).getSymbolName().equals("NIL")){
    		generateFunctDeclarationList(functDeclarationListNode);
    	}
    	
    }
    
    //<var-declaration-list> ::= NIL
    //<var-declaration-list> ::= <var-declaration> <var-declaration-list>
    private void generateVarDeclarationList(ParseTree varDeclarationListNode){
    	//Check if node is ok
    	if(varDeclarationListNode!=null && varDeclarationListNode.getSymbolName().equals("<var-declaration-list>")){
    		
    		//check if not nil
    		if(!varDeclarationListNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			
    			ParseTree varDeclarationNode = varDeclarationListNode.getChildren().get(0);
    			ParseTree varDeclarationListChildNode = varDeclarationListNode.getChildren().get(1);
    			generateVarDecleration(varDeclarationNode);
    			
    			if(!varDeclarationListChildNode.getChildren().get(0).getSymbolName().equals("NIL")){
    				generateVarDeclarationList(varDeclarationListChildNode);
    			}
    			
    		}
    		
    	}
    }
    
    
    //<var-declaration> ::= VAR <id-list> COLON <type> <optional-init> SEMI
    //<id-list> ::= ID <id-list-tail>
    //<id-list-tail> ::= NIL
    //<id-list-tail> ::= COMMA ID <id-list-tail>
    //<optional-init> ::= NIL
    //<optional-init> ::= ASSIGN <const>
    
    private void generateVarDecleration(ParseTree varDeclarationNode){
    	
    	//check if node is ok
    	if(varDeclarationNode!=null && varDeclarationNode.getSymbolName().equals("<var-declaration>")){
    		
    		ParseTree optionalInitNode = varDeclarationNode.getChildren().get(4);
    		if(!optionalInitNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			String constValLiteral = optionalInitNode.getChildren().get(1).getChildren().get(0).getTokenLiteral();
    			ParseTree idListNode = varDeclarationNode.getChildren().get(1);
    			List<String> idList = generateIdList(idListNode);
    			
    			ParseTree typeIdNode = varDeclarationNode.getChildren().get(3).getChildren().get(0);
    			
    			String idType = getType(idList.get(0));
    			
    			if(idType.toLowerCase().contains("array")){
    				for(String id: idList){
    					String arraySize = Integer.toString(irTypeTable.lookUp(irVarTable.lookUp(id).getTypeName()).getNumElements());
        				String irCode = "assign, " + id + ", " + arraySize + ", " + constValLiteral;
        				IRCodeList.add(irCode);
        			}
    			}
    			
    			else {
					for (String id : idList) {
						String irCode = "assign, " + id + ", " + constValLiteral + ",";
						IRCodeList.add(irCode);
					}
				}
    			
    		}
    		
    	}
    	
    }
    
    private List<String> generateIdListTail(ParseTree idListTailNode){
    	List<String> idList = new ArrayList<String>();
    	if(idListTailNode!=null && idListTailNode.getSymbolName().equals("<id-list-tail>")){
    		if(!idListTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			ParseTree idListTailChildNode = idListTailNode.getChildren().get(2);
    			
    			if(!idListTailChildNode.getChildren().get(0).getSymbolName().equals("NIL")){
    				idList = generateIdListTail(idListTailChildNode);
    			}
    			
    			idList.add(idListTailNode.getChildren().get(1).getTokenLiteral());
    		}
    		
    	}
    	return idList;
    }
    
    
    private List<String> generateIdList(ParseTree idListNode){
    	List<String> idList = new ArrayList<String>();
    	if(idListNode!=null && idListNode.getSymbolName().equals("<id-list>")){
    		ParseTree idListTailNode = idListNode.getChildren().get(1);
    		if(!idListTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			idList = generateIdListTail(idListTailNode);
    		}
    		idList.add(idListNode.getChildren().get(0).getTokenLiteral());
    		
    	}
    	return idList;
    }
    
    //<funct-declaration-list> ::= NIL
    //<funct-declaration-list> ::= <funct-declaration> <funct-declaration-list>
    private void generateFunctDeclarationList(ParseTree functDeclarationListNode){
    	
    	//check if node is ok
    	if(functDeclarationListNode!=null && functDeclarationListNode.getSymbolName().equals("<funct-declaration-list>")){
    		ParseTree functDeclarationNode = functDeclarationListNode.getChildren().get(0);
    		//Check if child is not nil
    		if(!functDeclarationNode.getSymbolName().equals("NIL")){
    			generateFunctDeclaration(functDeclarationNode);
    			ParseTree functDeclarationListChildNode = functDeclarationListNode.getChildren().get(1);
    			if(!functDeclarationListChildNode.getChildren().get(0).equals("NIL")){
    				generateFunctDeclarationList(functDeclarationListChildNode);
    			}
    		}
    	}
    }
    
    
    
    //<funct-declaration> ::= FUNCTION ID LPAREN <param-list> RPAREN <ret-type> BEGIN <stat-seq> END SEMI
    private void generateFunctDeclaration(ParseTree functDeclarationNode){
    	
    	//check if node is ok
    	if(functDeclarationNode!=null && functDeclarationNode.getSymbolName().equals("<funct-declaration>")){
    		ParseTree funcIdNode = functDeclarationNode.getChildren().get(1);
    		//Put function label in the IR code list
    		IRCodeList.add(funcIdNode.getTokenLiteral()+":");
    		
    		//Generate stat sequence
    		ParseTree statSeqNode = functDeclarationNode.getChildren().get(7);
    		generateStatSeq(statSeqNode);
    		
    		//Check return type
    		ParseTree returnNode = functDeclarationNode.getChildren().get(5);
    		String funcRetType = irFuncTable.lookUp(funcIdNode.getTokenLiteral()).getReturnType();

			//System.out.println(funcRetType);
			if(funcRetType.equals("void") && returnNode.getChildren().get(0).getSymbolName().equals("NIL")){
				IRCodeList.add("return, , ,");
    		}
    	}
    }
    
    
    //<stat-seq> ::= <stat> <stat-seq-tail>
    //<stat-seq-tail> ::= NIL
    //<stat-seq-tail> ::= <stat> <stat-seq-tail>
    //<stat> ::= BREAK SEMI
    //<stat> ::= RETURN <expr> SEMI
    //<stat> ::= ID <stat-ID-tail>
    //This procedire is same for stat-seq and stat-seq-tail
    private void generateStatSeq(ParseTree statSeqNode){
    	
    	//Check if we have a valid node
    	if(statSeqNode!=null && (statSeqNode.getSymbolName().equals("<stat-seq>") || statSeqNode.getSymbolName().equals("<stat-seq-tail>")))
    	{
    		//Get first child
    		ParseTree statChild = statSeqNode.getChildren().get(0);
    		//As it can be tail too in case of tail
        	if(!statChild.getSymbolName().equals("NIL")){
        		
        		ParseTree statFirstChild  = statChild.getChildren().get(0);
        		
        		//Now do switch
        		String childToken = statFirstChild.getSymbolName();
        		
        		switch(childToken){
        			
	        		//If stat sequece
		    		case("IF"):{
		    			generateIf(statChild);
		    			break;
		    		}
		    		
		    		//While stat sequence
		    		case("WHILE"):{
		    			generateWhile(statChild);
		    			break;
		    		}
		    		
		    		//For stat sequence
		    		case("FOR"):{
		    			generateFor(statChild);
		    			break;
		    		}
		    		
		    		case("BREAK"):{
		    			String breakIrCode = "goto, " + loopStack.peek() + ", ,";
		    			IRCodeList.add(breakIrCode);
		    			break;
		    		}
		    		
		    		case("RETURN"):{
		    			
		    			//Generate expr
		    			ParseTree exprNode = statChild.getChildren().get(1);
		    			generateExpr(exprNode);
		    			
		    			//Get return value
		    			Map.Entry<String, String> retVar = exprList.get(exprList.size()-1);
		    			exprList.remove(exprList.size()-1);
		    			
		    			//Return IrCode
		    			String returnIrCode = "return, " + retVar.getKey() + ", ,";
		    			IRCodeList.add(returnIrCode);
		    			break;
		    		}
    	    		
    	    		case("ID"):{
    	    			
    	    			ParseTree idNode = statChild.getChildren().get(0);
    	    			exprList.add(new AbstractMap.SimpleEntry<>(idNode.getTokenLiteral(),getType(idNode.getTokenLiteral())));
    	    			
    	    			//Call stat ID tail
    	    			ParseTree statIdTailNode = statChild.getChildren().get(1);
    	    			generateStatIdTail(statIdTailNode);
    	    			break;
    	    		}
    	    		
    	    		default:
    	    			break;
        		}
        		
        		//Call the same method on second node if not NIL
        		ParseTree statTailNode = statSeqNode.getChildren().get(1);
        		if(!statTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
        			generateStatSeq(statTailNode);
        		}
        	}
    	}
    } 
    
    //<stat> ::= IF <expr> THEN <stat-seq> <stat-ELSE-tail> ENDIF SEMI
    private void generateIf(ParseTree ifNode){
    	
    	//Check if node is not null and size is not 0
    	if(ifNode!=null && ifNode.getChildren().get(0).getSymbolName().equals("IF")){
    		
    		//Get first Node:
    		ParseTree firstChild = ifNode.getChildren().get(0);
    		if(firstChild.getSymbolName().equals("IF")){
    			ParseTree ifExpr = ifNode.getChildren().get(1);
    			generateExpr(ifExpr);
    			
    			Map.Entry<String, String> tempVar = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			String tempLabel = getLoopLabel();
    			
    			String branchCode = "breq, " + tempVar.getKey() + ", 0, " + tempLabel;
    			
    			IRCodeList.add(branchCode);
    			
    			generateStatSeq(ifNode.getChildren().get(3));
    			
    			if(!ifNode.getChildren().get(4).getChildren().get(0).getSymbolName().equals("NIL")){
    				String tempLabel2 = getLoopLabel();
    				String elseBranch = "breq, " + "0, " + "0, " + tempLabel2;
    				IRCodeList.add(elseBranch);
    				IRCodeList.add(tempLabel+":");
    				generateStatSeq(ifNode.getChildren().get(4).getChildren().get(1));
    				IRCodeList.add(tempLabel2+":");
    			}
    			else{
    				IRCodeList.add(tempLabel+":");
    			}
    		}	
    	}
    }
    
    //<stat> ::= WHILE <expr> DO <stat-seq> ENDDO SEMI
    private void generateWhile(ParseTree whileNode){
    	
    	//Check if node is not null and size is not 0
    	if(whileNode!=null && whileNode.getChildren().get(0).getSymbolName().equals("WHILE")){
    		
    		//Get first Node:
    		ParseTree firstChild = whileNode.getChildren().get(0);
    		if(firstChild.getSymbolName().equals("WHILE")){
    			
    			//Start of while loop
    			String tempLabel1 = getLoopLabel();
    			IRCodeList.add(tempLabel1+":");
    			
    			
    			//Eval exp
    			ParseTree whileExpr = whileNode.getChildren().get(1);
    			generateExpr(whileExpr);
    			
    			//Generate branch based on eval
    			Map.Entry<String, String> tempVar = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			String tempLabel2 = getLoopLabel();
    			//push for return
    			loopStack.push(tempLabel2);
    			String branchCode = "breq, " + tempVar.getKey() + ", 0, " + tempLabel2;
    			IRCodeList.add(branchCode);
    			
    			//Generate stat sequences
    			generateStatSeq(whileNode.getChildren().get(3));
    			loopStack.pop();
    			
    			//go back to top
    			String backToTop =  "breq, " + "0, " + "0, " + tempLabel1;
    			IRCodeList.add(backToTop);
    			IRCodeList.add(tempLabel2+":");
    		}	
    	}
    }
    
    //<stat> ::= FOR ID ASSIGN <expr> TO <expr> DO <stat-seq> ENDDO SEMI
    private void generateFor(ParseTree forNode){
    	//Check if node is not null and size is not 0
    	if(forNode!=null && forNode.getChildren().get(0).getSymbolName().equals("FOR")){
    		
    		
    		//Generate To expr
    		ParseTree toExprNode = forNode.getChildren().get(5);
    		generateExpr(toExprNode);
    		
    		//Get to expr on stack
			Map.Entry<String, String> toVar = exprList.get(exprList.size()-1);
			exprList.remove(exprList.size()-1);
			
			//Generate assign expr
			ParseTree idExprNode= forNode.getChildren().get(3);
    		generateExpr(idExprNode);
    		
    		//Get assign expr on stack
			Map.Entry<String, String> idExprVar = exprList.get(exprList.size()-1);
			exprList.remove(exprList.size()-1);
			
			//Get id node
    		ParseTree idNode = forNode.getChildren().get(1);
    		String idLiteral = idNode.getTokenLiteral();
    		
    		//Generate assign
    		String irAssignCode = "assign, " + idLiteral + ", " + idExprVar.getKey() + ",";
    		IRCodeList.add(irAssignCode);
    		
    		//Get begin label
    		String forBeginLabel = getLoopLabel();
    		//Push label on Ircode
    		IRCodeList.add(forBeginLabel+":");
    		
    		//Get end label
    		String forEndLabel = getLoopLabel();
    		//Push label on labelStack
    		loopStack.push(forEndLabel);
    		
    		//Get for branch
    		String forBranch =  "brgt, " + idLiteral + ", " + toVar.getKey() + ", " + forEndLabel;
    		IRCodeList.add(forBranch);
    		
    		//Generate stat sequence
    		ParseTree statSeqNode = forNode.getChildren().get(7);
    		generateStatSeq(statSeqNode);
    		
    		String incIrCode;
    		//Insert increment code
    		if(getType(idLiteral).contains("int")){
    			incIrCode = "add, " + idLiteral + ", 1, " + idLiteral;
    		}
    		else{
    			incIrCode = "add, " + idLiteral + ", 1.0, " + idLiteral;
    		}
    		
    		//Add it to IR code list
    		IRCodeList.add(incIrCode);
    		
    		//Return to top
    		String gotoTopCode = "goto, " + forBeginLabel +  ", ,";
    		IRCodeList.add(gotoTopCode);
    		
    		//Add end label
    		IRCodeList.add(loopStack.pop()+":");
    	}
    }
    
    
    
    //<stat-ID-tail> ::= LPAREN <expr-list> RPAREN SEMI
    //<stat-ID-tail> ::= <lvalue-tail> ASSIGN <stat-ID-expr-tail> SEMI
    private void generateStatIdTail(ParseTree statIdTailNode){
    	//Check if node is ok
    	if(statIdTailNode!=null && statIdTailNode.getSymbolName().equals("<stat-ID-tail>")){
    		ParseTree statIdTailChildNode = statIdTailNode.getChildren().get(0);
    		if(statIdTailChildNode.getSymbolName().equals("LPAREN")){
    			List<String> tempExprList = generateExprList(statIdTailNode.getChildren().get(1));
    			Map.Entry<String, String> funcId = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			FunctionRecord funcRec = irFuncTable.lookUp(funcId.getKey());
    			String irCode;
    			if(funcRec.getReturnType().equals("void") || funcRec.getReturnType().equals("null")){
    				irCode = "call, " + funcId.getKey();
    				for(String exp:tempExprList){
    					irCode += ", " + exp;
    				}
    				IRCodeList.add(irCode);
    			}
    			else{
    				String tempVar = getTempVar(funcRec.getReturnType());
    				irCode = "callr, " + tempVar + ", " + funcId.getKey();
    				exprList.add(new AbstractMap.SimpleEntry<>(tempVar,funcRec.getReturnType()));
    				for(String exp:tempExprList){
    					irCode += ", " + exp;
    				}
    				IRCodeList.add(irCode);
    			}
    		}
    		
    		else{
    			
    			//ParseTree lavalueTailNode = statIdTailNode.getChildren().get(0);
    			//generateLvalueTail(statIdTailNode.getChildren().get(0));
    			
    			ParseTree lvalueTailNode = statIdTailNode.getChildren().get(0);
    			
    			if(!lvalueTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    				ParseTree exprNode = lvalueTailNode.getChildren().get(1);
        			
        			Map.Entry<String, String> idTuple = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			String id = idTuple.getKey();
        			String idType = "float";
        			if(idTuple.getValue().toLowerCase().contains("int")){
        				idType = "int";
        			}
        			
        			generateExpr(exprNode);
        			
        			Map.Entry<String, String> indexTuple = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			String index = indexTuple.getKey();
        			
        			generateStatIdExprTail(statIdTailNode.getChildren().get(2));
        			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			
        			String irCode = "array_store, " + id + ", " + index + ", " +  id2.getKey();
        			IRCodeList.add(irCode);
        			
    			}
    			
    			else{
    				Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			generateStatIdExprTail(statIdTailNode.getChildren().get(2));
        			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			
        			String irCode = "assign, " + id1.getKey() + ", " + id2.getKey() + ",";
        			IRCodeList.add(irCode);
    			}
    			
    			
    			
    		}
    	}
    	
    }
    
    
    //<stat-ID-expr-tail> ::= <expr-no-ID>
    //<stat-ID-expr-tail> ::= ID <expr-func-tail>
    private void generateStatIdExprTail(ParseTree statIdExprTailNode){
    	//Check if node is ok
    	if(statIdExprTailNode!=null && statIdExprTailNode.getSymbolName().equals("<stat-ID-expr-tail>")){
    		
    		ParseTree statIdExprTailNodeChild = statIdExprTailNode.getChildren().get(0);
    		
    		if(statIdExprTailNodeChild.getSymbolName().equals("<expr-no-ID>")){
    			generateExprNoID(statIdExprTailNodeChild);
    		}
    		else{
    			String id = statIdExprTailNodeChild.getTokenLiteral();
				//Check if function or not
				String idType = "";
				if(irVarTable.lookUp(id)!=null){
					idType = irVarTable.lookUp(id).getTypeName();
				}
    			//Add it to exprList
    			exprList.add(new AbstractMap.SimpleEntry<>(id,idType));
    			
    			//Call generate func tail
    			generateExprFuncTail(statIdExprTailNode.getChildren().get(1));
    			
    		}
    		
    	}
    }
    
    
    //<expr-func-tail> ::= LPAREN <expr-list> RPAREN
    //<expr-func-tail> ::= <expr-lvalue-only>
    private void generateExprFuncTail(ParseTree exprFuncTailNode){
    	//Check if node is ok
    	if(exprFuncTailNode!=null && exprFuncTailNode.getSymbolName().equals("<expr-func-tail>")){
    		ParseTree exprFuncTailChild = exprFuncTailNode.getChildren().get(0);
    		
    		//if LPAREN
    		if(exprFuncTailChild.getSymbolName().equals("LPAREN")){
    			List<String> tempExprList = generateExprList(exprFuncTailNode.getChildren().get(1));
    			Map.Entry<String, String> funcId = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			FunctionRecord funcRec = irFuncTable.lookUp(funcId.getKey());
    			String irCode;
    			if(funcRec.getReturnType().equals("void") || funcRec.getReturnType().equals("null")){
    				irCode = "call, " + funcId.getKey();
    				for(String exp:tempExprList){
    					irCode += ", " + exp;
    				}
    				IRCodeList.add(irCode);
    			}
    			else{
    				String tempVar = getTempVar(funcRec.getReturnType());
    				irCode = "callr, " + tempVar + ", " + funcId.getKey();
    				exprList.add(new AbstractMap.SimpleEntry<>(tempVar,funcRec.getReturnType()));
    				for(String exp:tempExprList){
    					irCode += ", " + exp;
    				}
    				IRCodeList.add(irCode);
    			}
    			
    		}
    		else{
    			generateExprLvalueOnly(exprFuncTailNode.getChildren().get(0));
    		}
    		
    	}
    	
    }
    
    
    //<expr-lvalue-only> ::= <term-OR-lvalue-only> <expr-tail>
    private void generateExprLvalueOnly(ParseTree exprLvalueOnlyNode){
    	//Check if node is ok
    	if(exprLvalueOnlyNode!=null && exprLvalueOnlyNode.getSymbolName().equals("<expr-lvalue-only>")){
    		
    		//Generate Term node
    		ParseTree termOrLvalueOnlyNode = exprLvalueOnlyNode.getChildren().get(0);
    		generateTermOrLvalueOnly(termOrLvalueOnlyNode);
    		
    		//Generate tail
    		ParseTree exprTailNode = termOrLvalueOnlyNode.getChildren().get(1);
    		if(!exprTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateExprTail(exprTailNode);
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: or
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempVar = getTempVar(getType(id2.getValue()));
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
    		}
    	}
    }
    
    //<term-OR-lvalue-only> ::= <term-AND-lvalue-only> <term-OR-tail>
    private void generateTermOrLvalueOnly(ParseTree termOrLvalueOnlyNode){
    	//Check if node is ok
    	if(termOrLvalueOnlyNode!=null && termOrLvalueOnlyNode.getSymbolName().equals("<term-OR-lvalue-only>")){
    		
    		//Generate Term node
    		ParseTree termAndLvalueOnlyNode = termOrLvalueOnlyNode.getChildren().get(0);
    		generateTermAndLvalueOnly(termAndLvalueOnlyNode);
    		
    		//Generate tail
    		ParseTree termOrTailNode = termOrLvalueOnlyNode.getChildren().get(1);
    		if(!termOrTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateTermCompTail(termOrTailNode);
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: or
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempVar = getTempVar(getType(id2.getValue()));
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
    		}
    	}
    }
    
    
    //<term-AND-lvalue-only> ::= <term-comp-lvalue-only> <term-AND-tail>
    private void generateTermAndLvalueOnly(ParseTree termAndLvalueOnlyNode){
    	//Check if node is ok
    	if(termAndLvalueOnlyNode!=null && termAndLvalueOnlyNode.getSymbolName().equals("<term-AND-lvalue-only>")){
    		
    		//Generate Term node
    		ParseTree termCompLvalueOnlyNode = termAndLvalueOnlyNode.getChildren().get(0);
    		generateTermCompLvalueOnly(termCompLvalueOnlyNode);
    		
    		//Generate tail
    		ParseTree termAndTailNode = termAndLvalueOnlyNode.getChildren().get(1);
    		if(!termAndTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateTermAndTail(termAndTailNode);
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    	
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			
    			//Get actual opcode: or
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a int tempvar, this also adds it to symbol table, this stroes the result of the branch
    			String tempVar = getTempVar("int");
    			
    			//Assign tempVar to 0, this is 0 as branch is evaluated yet
    			String irAssign0 = "assign, " + tempVar + ", 0,"; 
    			
    			//Add it to IR List
    			IRCodeList.add(irAssign0);
    			
    			//Push the label on the label stack
    			String tempLabel = getLoopLabel();
    			
    			//Make an IR of branch, if branch is taken the result is 0
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempLabel;	
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			loopStack.push(tempLabel+":");
    			
    			//Assign tempVar to its idtype
    			String irAssign1 = "assign, " + tempVar + ", 1,";
    			
    			//Add it to IR List
    			IRCodeList.add(irAssign1);
    			IRCodeList.add(loopStack.pop());
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,"int"));
    		}
    	}
    }
    
    
    
    //<term-comp-lvalue-only> ::= <term-lvalue-only> <term-comp-tail>
    private void generateTermCompLvalueOnly(ParseTree termCompLvalueOnlyNode){
    	//Check if node is ok
    	if(termCompLvalueOnlyNode!=null && termCompLvalueOnlyNode.getSymbolName().equals("<term-comp-lvalue-only>")){
    		
    		//Generate Term node
    		ParseTree termLvalueOnlyNode = termCompLvalueOnlyNode.getChildren().get(0);
    		generateTermLvalueOnly(termLvalueOnlyNode);
    		
    		//Generate tail
    		ParseTree termCompTailNode = termCompLvalueOnlyNode.getChildren().get(1);
    		if(!termCompTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateTermCompTail(termCompTailNode);
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: or
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempVar = getTempVar(id2.getValue());
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
    		}
    	}
    }
    
    
    //<term-lvalue-only> ::= <lvalue-tail> <term-tail>
    private void generateTermLvalueOnly(ParseTree termLvalueOnlyNode){
    	//Check if node is ok
    	if(termLvalueOnlyNode!=null && termLvalueOnlyNode.getSymbolName().equals("<term-lvalue-only>")){
    		
    		//Generate Term node
    		ParseTree lvalueTailNode = termLvalueOnlyNode.getChildren().get(0);
    		generateLvalueTail(lvalueTailNode);
    		
    		//Generate tail
    		ParseTree termTailNode = termLvalueOnlyNode.getChildren().get(1);
    		if(!termTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateTermTail(termTailNode);
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: or
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempVar = getTempVar(getType(id2.getValue()));
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
    		}
    	}
    }
    
    //<lvalue-tail>
    private void generateLvalueTail(ParseTree lvalueTailNode){
    	//Check node
    	if(lvalueTailNode!=null && lvalueTailNode.getSymbolName().equals("<lvalue-tail>")){
    		
    		//Check if not nil
    		if(!lvalueTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			ParseTree exprNode = lvalueTailNode.getChildren().get(1);
    			
    			Map.Entry<String, String> idTuple = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			String id = idTuple.getKey();
    			String idType = "float";
    			if(idTuple.getValue().toLowerCase().contains("int")){
    				idType = "int";
    			}
    			
    			generateExpr(exprNode);
    			
    			Map.Entry<String, String> indexTuple = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			String index = indexTuple.getKey();
    			
    			//Get a temproray var on the basis of type
    			String tempVar = getTempVar(getType(id));
    			
    			String irCode = "array_load, " + tempVar + ", "	+ id + ", " + index;
    			IRCodeList.add(irCode);
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar, idType));
    		}
    	}
    	
    }
    
    //<expr> ::= <term-OR> <expr-tail>
    private void generateExpr(ParseTree exprNode){
    	//Check if node is ok
    	if(exprNode!=null && exprNode.getSymbolName().equals("<expr>")){
    		
    		//Generate Term node
    		ParseTree termOrNode = exprNode.getChildren().get(0);
    		generateTermOr(termOrNode);
    		
    		//Generate tail
    		ParseTree exprTailNode = exprNode.getChildren().get(1);
    		if(!exprTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateExprTail(exprTailNode);
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: or
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempVar = getTempVar(id2.getValue());
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
    		}
    		
    	}
    }
    
    //<expr-tail> ::= NIL
    //<expr-tail> ::= OR <term-OR> <expr-tail>
    private void generateExprTail(ParseTree termExprTailNode){
    	
    	//Check if node is ok
    	if(termExprTailNode!=null && termExprTailNode.getSymbolName().equals("<expr-tail>")){
    		
    		//check if node is not nil
    		if(!termExprTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			//int op index
    			int opIndex;
    			ParseTree opNode =  termExprTailNode.getChildren().get(0);
    			ParseTree termOrNode = termExprTailNode.getChildren().get(1);
    			exprList.add(new AbstractMap.SimpleEntry<>(opNode.getTokenLiteral(),""));
    			opIndex = exprList.size()-1;
    			
    			generateTermOr(termOrNode);
    			
    			//Run till you get NIL
    			ParseTree termExprTailChild = termExprTailNode.getChildren().get(2);
    			String termExprTailChildSymbolName = termExprTailChild.getChildren().get(0).getSymbolName();
    			while(!termExprTailChildSymbolName.equals("NIL")){
    				opNode =  termExprTailChild.getChildren().get(0);
    				termOrNode = termExprTailChild.getChildren().get(1);
    				exprList.add(new AbstractMap.SimpleEntry<>(opNode.getTokenLiteral(),""));
    				generateTermOr(termOrNode);
    				termExprTailChild = termExprTailChild.getChildren().get(2);
    				termExprTailChildSymbolName = termExprTailChild.getChildren().get(0).getSymbolName();
    			}
    			
    			for(int i=exprList.size()-1; i>opIndex+1;){
    				//get the three symbols
        			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			
        			//Get actual opcode: or
        			String opcode = tokenToIR.get(op.getKey());
        			
        			//get a tempvar, this also adds it to symbol table
        			String tempVar = getTempVar(getType(id2.getValue()));
        			
        			//Make an IR
        			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
        			
        			//Add it to IR List
        			IRCodeList.add(irCode);
        			
        			//Push temp val
        			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
        			i++;
    			
    			}
    		}
    	}  	
    }
    
    //<term-OR> ::= <term-AND> <term-OR-tail>
    private void generateTermOr(ParseTree termOrNode){
    	
    	//Check if node is ok
    	if(termOrNode!=null && termOrNode.getSymbolName().equals("<term-OR>")){
    		
    		//Generate Term node
    		ParseTree termAndNode = termOrNode.getChildren().get(0);
    		generateTermAnd(termAndNode);
    		
    		//Generate tail
    		ParseTree termOrTailNode = termOrNode.getChildren().get(1);
    		if(!termOrTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateTermOrTail(termOrTailNode);
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: or
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempVar = getTempVar(id2.getValue());
    			
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
    		}
    		
    	}
	}
    
    
    //<term-OR-tail> ::= NIL
    //<term-OR-tail> ::= AND <term-AND> <term-OR-tail>
    private void generateTermOrTail(ParseTree termOrTailNode){
    	
    	//Check if node is ok
    	if(termOrTailNode!=null && termOrTailNode.getSymbolName().equals("<term-OR-tail>")){
    		
    		//check if node is not nil
    		if(!termOrTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			//int op index
    			int opIndex;
    			ParseTree opNode =  termOrTailNode.getChildren().get(0);
    			ParseTree termAndNode = termOrTailNode.getChildren().get(1);
    			exprList.add(new AbstractMap.SimpleEntry<>(opNode.getTokenLiteral(),""));
    			opIndex = exprList.size()-1;
    			
    			generateTermAnd(termAndNode);
    			
    			//Run till you get NIL
    			ParseTree termOrTailChild = termOrTailNode.getChildren().get(2);
    			String termOrTailChildSymbolName = termOrTailChild.getChildren().get(0).getSymbolName();
    			while(!termOrTailChildSymbolName.equals("NIL")){
    				opNode =  termOrTailChild.getChildren().get(0);
    				termAndNode = termOrTailChild.getChildren().get(1);
    				exprList.add(new AbstractMap.SimpleEntry<>(opNode.getTokenLiteral(),""));
    				generateTermAnd(termAndNode);
    				termOrTailChild = termOrTailChild.getChildren().get(2);
    				termOrTailChildSymbolName = termOrTailChild.getChildren().get(0).getSymbolName();
    			}
    			
    			for(int i=exprList.size()-1; i>opIndex+1;){
    				//get the three symbols
        			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			
        			//Get actual opcode: or
        			String opcode = tokenToIR.get(op.getKey());
        			
        			//get a tempvar, this also adds it to symbol table
        			String tempVar = getTempVar(id2.getValue());
        			
        			//Make an IR
        			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
        			
        			//Add it to IR List
        			IRCodeList.add(irCode);
        			
        			//Push temp val
        			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
        			i++;
    			
    			}
    		}
    	}  	
    }
    
    
    //<term-AND> ::= <term-comp> <term-AND-tail>
    private void generateTermAnd(ParseTree termAndNode){
    	
    	//Check if node is ok
    	if(termAndNode!=null && termAndNode.getSymbolName().equals("<term-AND>")){
    		
    		//Generate Term ndoe
    		ParseTree termCompNode = termAndNode.getChildren().get(0);
    		generateTermComp(termCompNode);
    		
    		//Generate tail
    		ParseTree termAndTailNode = termAndNode.getChildren().get(1);
    		if(!termAndTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateTermAndTail(termAndTailNode);
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: or
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a int tempvar, this also adds it to symbol table, this stroes the result of the branch
    			String tempVar = getTempVar("int");
    			
    			//Assign tempVar to 0, this is 0 as branch is evaluated yet
    			String irAssign0 = "assign, " + tempVar + ", 0,"; 
    			
    			//Add it to IR List
    			IRCodeList.add(irAssign0);
    			
    			//Push the label on the label stack
    			String tempLabel = getLoopLabel();
    			
    			//Make an IR of branch, if branch is taken the result is 0
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempLabel;	
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			loopStack.push(tempLabel+":");
    			
    			//Assign tempVar to its idtype
    			String irAssign1 = "assign, " + tempVar + ", 1,";
    			
    			//Add it to IR List
    			IRCodeList.add(irAssign1);
    			IRCodeList.add(loopStack.pop());
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,"int"));
    		}
    		
    	}
    	
    }
    
    //<term-AND-tail> ::= <op-comp> <term-comp> <term-AND-tail>
    //<term-AND-tail> ::= NIL
    private void generateTermAndTail(ParseTree termAndTailNode){
    	
    	//Check if node is ok
    	if(termAndTailNode!=null && termAndTailNode.getSymbolName().equals("<term-AND-tail>")){
    		
    		//check if node is not nil
    		if(!termAndTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			//int op index
    			int opIndex;
    			ParseTree opNode =  termAndTailNode.getChildren().get(0);
    			ParseTree termCompNode = termAndTailNode.getChildren().get(1);
    			exprList.add(new AbstractMap.SimpleEntry<>(opNode.getChildren().get(0).getTokenLiteral(),""));
    			opIndex = exprList.size()-1;
    			
    			generateTermComp(termCompNode);
    			
    			//Run till you get NIL
    			ParseTree termAndTailChild = termAndTailNode.getChildren().get(2);
    			String termAndTailChildSymbolName = termAndTailChild.getChildren().get(0).getSymbolName();
    			while(!termAndTailChildSymbolName.equals("NIL")){
    				opNode =  termAndTailChild.getChildren().get(0);
    				termCompNode = termAndTailChild.getChildren().get(1);
    				exprList.add(new AbstractMap.SimpleEntry<>(opNode.getChildren().get(0).getTokenLiteral(),""));
    				generateTermComp(termCompNode);
    				termAndTailChild = termAndTailChild.getChildren().get(2);
    				termAndTailChildSymbolName = termAndTailChild.getChildren().get(0).getSymbolName();
    			}
    			
    			for(int i=exprList.size()-1; i>opIndex+1;){
    				//get the three symbols
        			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			
        			//Get actual opcode: or
        			String opcode = tokenToIR.get(op.getKey());
        			
        			//get a int tempvar, this also adds it to symbol table, this stroes the result of the branch
        			String tempVar = getTempVar("int");
        			
        			//Assign tempVar to 0, this is 0 as branch is evaluated yet
        			String irAssign0 = "assign, " + tempVar + ", 0,"; 
        			
        			//Add it to IR List
        			IRCodeList.add(irAssign0);
        			
        			//Push the label on the label stack
        			String tempLabel = getLoopLabel();
        			
        			//Make an IR of branch, if branch is taken the result is 0
        			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempLabel;	
        			
        			//Add it to IR List
        			IRCodeList.add(irCode);
        			loopStack.push(tempLabel+":");
        			
        			//Assign tempVar to its idtype
        			String irAssign1 = "assign, " + tempVar + ", 1,";
        			
        			//Add it to IR List
        			IRCodeList.add(irAssign1);
        			IRCodeList.add(loopStack.pop());
        			
        			//Push temp val
        			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,"int"));
        			i++;
    			
    			}
    		}
    	}  	
    }
    
    //<term-comp> ::= <term> <term-comp-tail>
    private void generateTermComp(ParseTree termCompNode){
    	
    	//Check if node is ok
    	if(termCompNode!=null && termCompNode.getSymbolName().equals("<term-comp>")){
    		
    		//Generate Term ndoe
    		ParseTree termNode = termCompNode.getChildren().get(0);
    		generateTerm(termNode);
    		
    		//Genrate tail
    		ParseTree termCompTailNode = termCompNode.getChildren().get(1);
    		if(!termCompTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateTermCompTail(termCompTailNode);
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: or
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempVar = getTempVar(id2.getValue());
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
    		}
    		
    	}
    	
    }
    
    
    //<term-comp-tail> ::= NIL
    //<term-comp-tail> ::= <op-add> <term> <term-comp-tail>
    private void generateTermCompTail(ParseTree termCompTailNode){
    	
    	//check id node is ok
    	if(termCompTailNode!=null && termCompTailNode.getSymbolName().equals("<term-comp-tail>")){
    		//check if node is not nil
    		if(!termCompTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			
    			//int op index
    			int opIndex;
    			ParseTree opNode =  termCompTailNode.getChildren().get(0);
    			ParseTree termNode = termCompTailNode.getChildren().get(1);
    			exprList.add(new AbstractMap.SimpleEntry<>(opNode.getChildren().get(0).getTokenLiteral(),""));
    			opIndex = exprList.size()-1;
    			
    			generateTerm(termNode);
    			
    			//Run till you get NIL
    			ParseTree termTailChild = termCompTailNode.getChildren().get(2);
    			String termChildSymbolName = termTailChild.getChildren().get(0).getSymbolName();
    			while(!termChildSymbolName.equals("NIL")){
    				opNode =  termTailChild.getChildren().get(0);
    				termNode = termTailChild.getChildren().get(1);
    				exprList.add(new AbstractMap.SimpleEntry<>(opNode.getChildren().get(0).getTokenLiteral(),""));
    				generateTerm(termNode);
    				termTailChild = termTailChild.getChildren().get(2);
    				termChildSymbolName = termTailChild.getChildren().get(0).getSymbolName();
    			}
    			
    			for(int i=exprList.size()-1; i>opIndex+1;){
    				//get the three symbols
        			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			
        			//Get actual opcode: or
        			String opcode = tokenToIR.get(op.getKey());
        			
        			//get a tempvar, this also adds it to symbol table
        			String tempVar = getTempVar(id2.getValue());
        			
        			//Make an IR
        			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
        			
        			//Add it to IR List
        			IRCodeList.add(irCode);
        			
        			//Push temp val
        			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
        			i++;
    			}
    		}
    		
    	}
    }
   
    
    //<term> ::= <factor> <term-tail>
    private void generateTerm(ParseTree termNode){
    	
    	if(termNode!=null && termNode.getSymbolName().equals("<term>")){
    		
    		ParseTree factorNode = termNode.getChildren().get(0);
    		//Eval the factor on stack
    		generateFactor(factorNode);
    		//Eval the termNode
    		
    		
    		ParseTree tailNode = termNode.getChildren().get(1);
    		if(!tailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateTermTail(tailNode);
    			
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: or
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempVar = getTempVar(id2.getValue());
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
    			
    		}	
    	}
    	
    }
    
    
    //<term-tail> ::= <op-mul> <factor> <term-tail>
    //<term-tail> ::= NIL
    private void generateTermTail(ParseTree termTailNode){
    	
    	//check id node is ok
    	if(termTailNode!=null && termTailNode.getSymbolName().equals("<term-tail>")){
    		//check if node is not nil
    		if(!termTailNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			
    			//int op index
    			int opIndex;
    			ParseTree opNode =  termTailNode.getChildren().get(0);
    			ParseTree factorNode = termTailNode.getChildren().get(1);
    			exprList.add(new AbstractMap.SimpleEntry<>(opNode.getChildren().get(0).getTokenLiteral(),""));
    			opIndex = exprList.size()-1;
    			
    			generateFactor(factorNode);
    			
    			//Run till you get NIL
    			ParseTree termTailChild = termTailNode.getChildren().get(2);
    			String termChildSymbolName = termTailChild.getChildren().get(0).getSymbolName();
    			while(!termChildSymbolName.equals("NIL")){
    				opNode =  termTailChild.getChildren().get(0);
    				factorNode = termTailChild.getChildren().get(1);
    				exprList.add(new AbstractMap.SimpleEntry<>(opNode.getChildren().get(0).getTokenLiteral(),""));
    				generateFactor(factorNode);
    				termTailChild = termTailChild.getChildren().get(2);
    				termChildSymbolName = termTailChild.getChildren().get(0).getSymbolName();
    			}
    			
    			for(int i=exprList.size()-1; i>opIndex+1;){
    				//get the three symbols
        			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
        			exprList.remove(exprList.size()-1);
        			i--;
        			
        			//Get actual opcode: or
        			String opcode = tokenToIR.get(op.getKey());
        			
        			//get a tempvar, this also adds it to symbol table
        			String tempVar = getTempVar(id2.getValue());
        			
        			//Make an IR
        			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
        			
        			//Add it to IR List
        			IRCodeList.add(irCode);
        			
        			//Push temp val
        			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
        			i++;
    			}
    		}
    		
    	}
    }
    
    //<expr-no-ID> ::= <term-OR-no-ID> <expr-tail>
    private void generateExprNoID(ParseTree exprNoId){
    	//Check if term-AND-no-ID 
    	if(exprNoId!=null && exprNoId.getSymbolName().equals("<expr-no-ID>")){
    		//get both childs
    		ParseTree termOrNoId = exprNoId.getChildren().get(0);
    		ParseTree exprTail = exprNoId.getChildren().get(1);
    		
    		generateTermOrNoID(termOrNoId);
    		//if tail is not nil
    		if(!exprTail.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateExprTail(exprTail);
    			
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: or
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempVar = getTempVar(id2.getValue());
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
    			
    		}
    	}
    }
    
    //<term-OR-no-ID> ::= <term-AND-no-ID> <term-OR-tail>
    private void generateTermOrNoID(ParseTree termOrNoId){
    	//Check if term-AND-no-ID 
    	if(termOrNoId!=null && termOrNoId.getSymbolName().equals("<term-OR-no-ID>")){
    		//get both childs
    		ParseTree termAndNoId = termOrNoId.getChildren().get(0);
    		ParseTree termOrTail = termOrNoId.getChildren().get(1);
    		
    		generateTermAndNoID(termAndNoId);
    		//if tail is not nil
    		if(!termOrTail.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateTermOrTail(termOrTail);
    			
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: or
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempVar = getTempVar(id2.getValue());
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
    			
    		}
    	}
    }
    
    
    
    //<term-AND-no-ID> ::= <term-comp-no-ID> <term-AND-tail>
    private void generateTermAndNoID(ParseTree andNoIdNode){
    	
    	//Check if term-AND-no-ID 
    	if(andNoIdNode!=null && andNoIdNode.getSymbolName().equals("<term-AND-no-ID>")){
    		//get both childs
    		ParseTree termCompNoId = andNoIdNode.getChildren().get(0);
    		ParseTree termAndTail = andNoIdNode.getChildren().get(1);
    		
    		generateTermCompNoId(termCompNoId);
    		//if tail is not nil
    		if(!termAndTail.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateTermAndTail(termAndTail);
    			
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: and
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempVar = getTempVar(id2.getValue());
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
    			
    		}
    	}
    }
    
    //<term-comp-no-ID> ::= <term-no-ID> <term-comp-tail>
    private void generateTermCompNoId(ParseTree termCompNoId){
    	
    	//Check if term-comp-no-ID
    	if(termCompNoId!=null && termCompNoId.getSymbolName().equals("<term-comp-no-ID>")){
    		
    		//get both childs
    		ParseTree termNoId = termCompNoId.getChildren().get(0);
    		ParseTree termCompTail = termCompNoId.getChildren().get(1);
    		
    		generateTermNoId(termNoId);
    		if(!termCompTail.getChildren().get(0).getSymbolName().equals("NIL")){
    			generateTermCompTail(termCompTail);
    			
    			//get the three symbols
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: comp op
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempLabel = getLoopLabel();
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempLabel;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			loopStack.push(tempLabel+":");
    			
    		}
    		
    	}
    }
    
    
    //<term-no-ID> ::= <factor-no-ID> <term-tail>
    private void generateTermNoId(ParseTree termNoId){
    	
    	//Check if node is valid
    	if(termNoId!=null && termNoId.getSymbolName().equals("<term-no-ID>")){
    		
    		//get factor-no-id child and term tail child
    		ParseTree factorNoId = termNoId.getChildren().get(0);
    		ParseTree termTail = termNoId.getChildren().get(1);
    		
    		//generate factor-no-id
    		generateFactorNoId(factorNoId);
    		//if term tail is not nil
    		if(!termTail.getChildren().get(0).getSymbolName().equals("NIL")){
    			//generate term tail on stack
    			generateTermTail(termTail);
    			
    			//get the three symbols
    			Map.Entry<String, String> id2 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String>  op = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			Map.Entry<String, String> id1 = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			
    			//Get actual opcode: mul or div
    			String opcode = tokenToIR.get(op.getKey());
    			
    			//get a tempvar, this also adds it to symbol table
    			String tempVar = getTempVar(id2.getValue());
    			
    			//Make an IR
    			String irCode = opcode + ", " + id1.getKey() + ", " + id2.getKey() + ", " + tempVar;
    			
    			//Add it to IR List
    			IRCodeList.add(irCode);
    			
    			//Push temp val
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar,id2.getValue()));
    		}
    	}
    }
    
    
    
    //<factor> ::= <factor-no-ID>
    //<factor> ::= <lvalue> 
    private void generateFactor(ParseTree factorNode){
    	
    	//Check if node is valid
    	if(factorNode!=null&&factorNode.getSymbolName().equals("<factor>")){
    		
    		//check child
    		ParseTree factorChild = factorNode.getChildren().get(0);
    		if(factorChild.getSymbolName().equals("<factor-no-ID>")){
    			generateFactorNoId(factorChild);
    		}
    		else{
    			generateLvalue(factorChild);
    		}
    		
    	}
    	
    }
    
    //<factor-no-ID> ::= <const>
    //<factor-no-ID> ::= LPAREN <expr> RPAREN
    private void generateFactorNoId(ParseTree factorNoId){
    	
    	//Check if the node is factorID
    	if(factorNoId!=null && factorNoId.getSymbolName().equals("<factor-no-ID>")){
    		
    		//Check child:
    		ParseTree factorChild = factorNoId.getChildren().get(0);
    		if(factorChild.getSymbolName().equals("<const>")){
    			ParseTree constChild = factorChild.getChildren().get(0);
    			if(constChild.getSymbolName().equals("INTLIT")){
    				exprList.add(new AbstractMap.SimpleEntry<>(constChild.getTokenLiteral(),"int"));
    			}
    			else{
    				exprList.add(new AbstractMap.SimpleEntry<>(constChild.getTokenLiteral(),"float"));
    			}
    			
    		}
    		else{
    			ParseTree exprChild = factorNoId.getChildren().get(1);
    			generateExpr(exprChild);
    		}
    	}
    }
    
    
    //<expr-list-tail> ::= COMMA <expr> <expr-list-tail>
    //<expr-list-tail> ::= NIL
    private List<String> generateExprListTail(ParseTree exprListNode){
    	List<String> exprListTemp = new ArrayList<String>();
    	if(exprListNode!=null && exprListNode.getSymbolName().equals("<expr-list-tail>")){
    		if(!exprListNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			exprListTemp = generateExprListTail(exprListNode.getChildren().get(2));
    			generateExpr(exprListNode.getChildren().get(1));
    			Map.Entry<String, String> exprTuple = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			exprListTemp.add(0, exprTuple.getKey());
    			return exprListTemp;
    		}
    	}
    	return exprListTemp;
    }
    
    //<expr-list> ::= NIL
    //<expr-list> ::= <expr> <expr-list-tail>
    private List<String> generateExprList(ParseTree exprListNode){
    	List<String> exprListTemp = new ArrayList<String>();
    	if(exprListNode!=null && exprListNode.getSymbolName().equals("<expr-list>")){
    		if(!exprListNode.getChildren().get(0).getSymbolName().equals("NIL")){
    			exprListTemp = generateExprListTail(exprListNode.getChildren().get(1));
    			generateExpr(exprListNode.getChildren().get(0));
    			Map.Entry<String, String> exprTuple = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			exprListTemp.add(0, exprTuple.getKey());
    			return exprListTemp;
    		}
    	}
    	
    	return exprListTemp;
    }
    
    
    //<lvalue> ::= ID <lvalue-tail>
    //<lvalue-tail> ::= LBRACK <expr> RBRACK
    //<lvalue-tail> ::= NIL
    private void generateLvalue(ParseTree lvalue){
    	
    	if(lvalue!=null && lvalue.getSymbolName().equals("<lvalue>") && lvalue.getChildren().size()!=0){
    		ParseTree lvalueTail = lvalue.getChildren().get(1);
    		String id = lvalue.getChildren().get(0).getTokenLiteral();
    		
    		if(lvalueTail.getChildren().get(0).getSymbolName().equals("NIL")){
    			exprList.add(new AbstractMap.SimpleEntry<>(id, getType(id)));
    		}
    		
    		else if(lvalueTail.getChildren().get(0).getSymbolName().equals("LBRACK")){
    			ParseTree exprNode = lvalueTail.getChildren().get(1);
    			generateExpr(exprNode);
    			
    			Map.Entry<String, String> indexTuple = exprList.get(exprList.size()-1);
    			exprList.remove(exprList.size()-1);
    			String index = indexTuple.getKey();
    			
    			//Get a temproray var on the basis of type
    			String tempVar = getTempVar(getType(id));
    			
    			String irCode = "array_load, " + tempVar + ", "	+ id + ", " + index;
    			IRCodeList.add(irCode);
    			exprList.add(new AbstractMap.SimpleEntry<>(tempVar, getType(id)));
    		}
    	}
    }
    
    
    //Get a tempVar based on type
    private String getTempVar(String idType){
    	if(idType.equals("int") || idType.equals("_array_int")){
    		
    		//Get a temprary int
    		String tempVar = intTemp+Integer.toString(intCount++);
    		
    		//Add it to var table
    		VarRecord tempRec = new VarRecord();
    		tempRec.setTypeName(idType);
    		tempRec.setNumElements(0);
    		irVarTable.insert(tempVar, tempRec);
    		
    		//Return tempVar
    		return (tempVar);
    	}
    	else if(idType.equals("float") || idType.equals("_array_float")){
    		//Get a temprary int
    		String tempVar = floatTemp+Integer.toString(floatCount++);
    		
    		//Add it to var table
    		VarRecord tempRec = new VarRecord();
    		tempRec.setTypeName(idType);
    		tempRec.setNumElements(0);
    		irVarTable.insert(tempVar, tempRec);
    		
    		//Return tempVar
    		return (tempVar);
    	}
    	
    	return null;
    }
    
    //To get Type of an ID
    private String getType(String id){
    	if(irVarTable.contains(id)){
    		VarRecord idRec = irVarTable.lookUp(id);
    		String idType = idRec.getTypeName();
    		if(!idType.equals("_array_float") && !idType.equals("_array_int") && !idType.equals("int") && !idType.equals("float")){
    			TypeRecord idTypeRec = irTypeTable.lookUp(idType);
    			idType = idTypeRec.getSuperType();
    		}   		
    		return idType;  		
    	}	
    	return null;
    }
    
    //To  get loop label
    private String getLoopLabel(){
    	return loopTemp + Integer.toString(loopCount++);
    }
    
}
