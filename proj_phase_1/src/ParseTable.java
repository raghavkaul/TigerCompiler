import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Raghav K on 9/26/15.
 */
public class ParseTable {
    private Map<RuleTokenWrapper, ParseRule> ruleTable;


    /**
     * Add rules
     */
    public ParseTable(File rulesFile) {
        Scanner rulesFileScanner = null;

        try {
            rulesFileScanner = new Scanner(rulesFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (rulesFileScanner.hasNextLine()) {

        }

    }
}
