import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSemanticChecker {
    private SemanticChecker semanticChecker;
    private static final String testProgFilePath = "./data/test_prog/test";
    private static final int NUM_TESTS = 9;
    private String[] filenames;
    private boolean[] testCorrectness;

    @Before
    public void init() {
        filenames = new String[NUM_TESTS];
        for (int i = 1; i <= 9; i++) {
            filenames[i-1] = testProgFilePath + i + ".tiger";
        }
    }

    @Test
    public void testSemanticChecker() {
        for (String filename : filenames) {
            boolean currFileIsSemanticallyCorrect = false;
            semanticChecker = new SemanticChecker(filename);


        }
    }
}
