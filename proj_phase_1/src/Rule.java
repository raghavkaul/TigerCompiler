import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rule {
    private String name;
    private List<String> expansion;
    private int lineno;

    public Rule(String production,int lineno){
        String[] productionList = production.split(" ::= ");
        if(productionList.length < 2){
            return;
        }
        else{
            name = productionList[0];
            name = name.trim();
            this.expansion = new ArrayList<>(Arrays.asList(productionList[1].trim().split(" ")));
            this.lineno  = lineno;
        }
    }

    public String getName(){
        return name;
    }
    public List<String> getExpansion(){
        return expansion;
    }
    public String toString(){
        return "No: " + lineno + " = " + name;
    }

}