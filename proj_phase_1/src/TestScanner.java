import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by Inseok on 9/29/15.
 * JUnit tests for making sure proper tokens are generated
 */
public class TestScanner {
    TigerScanner ts;
    private static final String programFilename = "./data/test_prog/example1.tiger",
            statesFilename = "./data/states.csv",
            transitionsFilename = "./data/transitions.csv";


    public void setup(String programFilename) throws Exception {
        ts = new TigerScanner(new File(programFilename), new File(statesFilename), new File(transitionsFilename));
    }

    @Test
    public void testPeekToken() throws Exception {
        ts = new TigerScanner(new File(programFilename), new File(statesFilename), new File(transitionsFilename));
        System.out.println(ts.nextToken().toString());
    }

    @Test
    public void testExamples() throws Exception {
        for (int i = 1; i <= 7; i++ ) {
            setup("./data/test_prog/test" + i + ".tiger");
            while (ts.peekToken().getType() != TokenType.EOF_TOKEN)
                System.out.print(ts.nextToken().getType() + " ");
            System.out.println(ts.peekToken().getType());
        }
    }

}