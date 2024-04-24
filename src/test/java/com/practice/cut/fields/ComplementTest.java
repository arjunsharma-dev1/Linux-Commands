
package com.practice.cut.fields;

import com.practice.cut.AppTest;
import junit.framework.TestCase;

public class ComplementTest extends TestCase {

    private final String filePathWithComma = getClass()
            .getClassLoader()
            .getResource("fields/WithCommaDelimiter.csv")
            .getPath();

    public void testCustomOutputDelimiterWithComplement() {
        var args = new String[]{"--complement", "--output-delimiter=/", "-d,", "-f1-2,3-4", filePathWithComma};
        var expectedResult =
                """ 
                f5/f6
                5/6
                15/16
                25/26
                35/36
                45/46""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }

    public void testCustomOutputDelimiterWithComplement1() {
        var args = new String[]{"--complement", "--output-delimiter=/", "-d,", "-f1,2,3,4", filePathWithComma};
        var expectedResult =
                """ 
                f5/f6
                5/6
                15/16
                25/26
                35/36
                45/46""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }

    public void testCustomOutputDelimiterWithComplement2() {
        var args = new String[]{"--complement", "--output-delimiter=/", "-d,", "-f1-2,4", filePathWithComma};
        var expectedResult =
                """ 
                f3/f5/f6
                3/5/6
                13/15/16
                23/25/26
                33/35/36
                43/45/46""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }
}
