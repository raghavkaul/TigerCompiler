import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Class representing high-level scanner
 */
public class TigerScanner {
    private File infile;

    public TigerScanner(File infile) {
        this.infile = infile;
        Scanner infileScanner;

        try {
            infileScanner = new Scanner(this.infile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
