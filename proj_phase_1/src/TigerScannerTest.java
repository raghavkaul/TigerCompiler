import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by Inseok on 9/29/15.
 * JUnit tests for making sure proper tokens are generated
 */
public class TigerScannerTest {
    TigerScanner ts;
    private static final String programFilename = "./data/example1.tiger",
            statesFilename = "./data/states.csv",
            transitionsFilename = "./data/transitions.csv";


    @Before
    public void setup() throws Exception {
        ts = new TigerScanner(new File(programFilename), new File(statesFilename), new File(transitionsFilename));
    }

    @Test
    public void testPeekToken() throws Exception {
        System.out.println(ts.nextToken().toString());
    }

    @Test
    public void testExample1() throws Exception {
        while (ts.peekToken().getType() != TokenType.EOF_TOKEN)
            System.out.print(ts.nextToken().getType() + " ");
        System.out.println(ts.peekToken().getType());
    }
}