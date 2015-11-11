import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by inseok on 11/10/15.
 */
public class TestSemanticChecker {
    SemanticChecker sc;
    String [] passingTests = {      "./data/test_prog/test1.tiger"
                                ,   "./data/test_prog/test12.tiger"};

    @Test
    public void passingTests() {
        for (String str: passingTests) {
            sc = new SemanticChecker(str);
            assertTrue(sc.returnSemantic());
        }
    }
}