import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Driver class for middle-end.
 */
public class IRGenerator {
    public static void main(String[] args) throws Exception {
        String helpStr = "Tiger language IR generator options:" +
                "\n Usage : java IRGenerator [options] [filename]" +
                "\n --symbols:\t displays symbol tables" +
                "\n --tree:\t displays parse tree" +
                "\n --check:\t performs semantic checking. Silent pass" +
                "if no errors." +
                "\n --generate:\t generates IR code" +
                "\n You must pass in only one option and one filename";

        if (args.length > 2 || args.length < 1) {
            System.out.println(helpStr);
        } else if (args.length == 2) {
            TigerParser tigerParser = new TigerParser(new File(args[1]));

            tigerParser.parse();

            if (args[0].contains("symbols")) {
                System.out.println("-------- Types --------");
                for (Map.Entry<String, TypeRecord> me : tigerParser.getTypeTable().getTable().entrySet()) {
                    System.out.println(me.getKey() + "\t\t" + me.getValue());
                }

                System.out.println("-------- Vars --------");
                for (Map.Entry<String, VarRecord> me : tigerParser.getVarTable().getTable().entrySet()) {
                    System.out.println(me.getKey() + "\t\t" + me.getValue());
                }

                System.out.println("-------- Functions --------");
                for (Map.Entry<String, FunctionRecord> me : tigerParser.getFunctionTable().getTable().entrySet()) {
                    System.out.println(me.getKey() + "\t\t" + me.getValue());
                }
            } else if (args[0].contains("tree")) {
                ParseTree parseTree = tigerParser.getParseTreeOld();
                parseTree.print();
            } else if (args[0].contains("check")) {
                boolean isSemanticallyCorrect = false;
                SemanticChecker sc = new SemanticChecker(args[1]);
            } else if (args[0].contains("generate")) {
                File outfile = new File(args[1].substring(0, args[1].length()-6) + ".irgen");
                SemanticChecker semanticChecker = new SemanticChecker(args[1]);
                if (!semanticChecker.tp.hasErrors) {
                   if (!semanticChecker.returnSemantic()) {
                       System.out.println("Contains semantic error");
                       System.exit(-1);
                   }
                } else {
                    System.out.println("Contains parse errors");
                    System.exit(-1);
                }
                PrintWriter writer = new PrintWriter(outfile);
                IRCodeGenerator ircg = new IRCodeGenerator(tigerParser.getParseTreeOld(), tigerParser.getVarTable(), tigerParser.getTypeTable(), tigerParser.getFunctionTable());
                List<String> instrs = ircg.generateIrCode();
                for (String instr : instrs) {
                    writer.println(instr);
                }
                writer.close();
            }
        }
    }
}
