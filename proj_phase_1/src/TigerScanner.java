import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by Raghav K on 9/17/15
 * Class representing high-level scanner
 */
public class TigerScanner {
    private File infile;
    private Scanner infileScanner;
    private DFA dfa;

    public TigerScanner(File infile) {
        this.infile = infile;

        try {
            infileScanner = new Scanner(this.infile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // TODO
        dfa = new DFA(null, null);
    }

    public void Scan() {
        while(infileScanner != null && infileScanner.hasNext()) {
            String currLine = infileScanner.nextLine();
            for (int i = 0; i < currLine.length(); i++) {

            }
        }
    }
}
