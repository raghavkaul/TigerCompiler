import java.io.File;
import java.util.Set;

/**
 * Driver class for middle-end.
 */
public class IRGenerator {
    public static void main(String[] args) {
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

            } else if (args[0].contains("tree")) {
                ParseTree parseTree = tigerParser.getParseTreeOld();
                parseTree.print();
            } else if (args[0].contains("check")) {

            } else if (args[0].contains("generate")) {

            }
        }
    }
}
