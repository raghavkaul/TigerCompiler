import com.sun.org.apache.bcel.internal.classfile.Code;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by tyuyhnbnm on 11/29/2015.
 */
public class CodeGeneratorTest {

    CodeGenerator cg;

    @Before
    public void setup() {
        cg = new CodeGenerator("");
    }

    @Test
    public void testFillData() throws Exception {
        // Single variable assignment
        String test = "assign X, 10,";
        test = test.replace(",", "");
        String[] split = test.split(" ");
        assertEquals(cg.fillData(split[0], split[1], split[2], ""), "\tX:\t.word\t10");

        // Array assignment
        test = "assign Y, 100, 10";
        test = test.replace(",", "");
        split = test.split(" ");
        assertEquals(cg.fillData(split[0], split[1], split[2], split[3]), "\tY:\t.word\t10:100");

        // Variable reuse
        test = "assign W, 10,";
        test = test.replace(",", "");
        split = test.split(" ");
        assertEquals(cg.fillData(split[0], split[1], split[2], ""), "\tW:\t.word\t10");

        test = "assign Z, 100, X";
        test = test.replace(",", "");
        split = test.split(" ");
        assertEquals(cg.fillData(split[0], split[1], split[2], split[3]), "\tZ:\t.word\t10:100");
    }

    @Test
    public void testGetInstrThreeAddrType() throws Exception {

    }
}