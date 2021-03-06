import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestSemanticChecker {
    private static final String testProgFilePath = "./data/test_prog/test";
    private static final int NUM_TESTS = 13;
    private String[] filenames;
    private boolean[] checkerExpectedResult;

    @Before
    public void init() {
        filenames = new String[NUM_TESTS];
        checkerExpectedResult = new boolean[NUM_TESTS];

        for (int i = 1; i <= 3; i++) {
            checkerExpectedResult[i-1] = true;
        }

        checkerExpectedResult[9] = false;
        checkerExpectedResult[10] = false;
        checkerExpectedResult[11] = false;
        checkerExpectedResult[12] = false;

        for (int i = 1; i <= 12; i++) {
            filenames[i-1] = testProgFilePath + i + ".tiger";
        }
    }

    @Test
    public void testSemanticChecker() {
        for (int i = 0; i < 7; i++) {
            System.out.println("------- " + filenames[i] + " -------");
            SemanticChecker semanticChecker = new SemanticChecker(filenames[i]);

            System.out.println(i);
            if (!semanticChecker.tp.hasErrors) {
                assertEquals(checkerExpectedResult[i], semanticChecker.returnSemantic());
            }
            System.out.println("Semantic Checked without issue");
        }
    }

    @Test
    public void testCheckerUnit() {
        filenames = new String[] {testProgFilePath + "1.tiger", testProgFilePath + "2.tiger"};
        TigerParser tp = new TigerParser(new File(filenames[0]));
        tp.parse(); ParseTree pt = tp.getParseTreeOld();
        SemanticChecker semanticChecker = new SemanticChecker(filenames[0]);
        pt.print();
        assertEquals(true, semanticChecker.returnSemantic());
    }

    @Test
    public void testWrongUnit() {
        for (int i = 9; i < 12; i++) {
            System.out.println("------- " + filenames[i] + " -------");
            SemanticChecker semanticChecker = new SemanticChecker(filenames[i]);

            System.out.println(i);
            if (!semanticChecker.tp.hasErrors) {
                assertEquals(checkerExpectedResult[i], semanticChecker.returnSemantic());
            }
            System.out.println("Semantic Checked without issue");
        }
    }
}
