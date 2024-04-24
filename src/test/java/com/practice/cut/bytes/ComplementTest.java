
package com.practice.cut.bytes;

import com.practice.cut.AppTest;
import junit.framework.Assert;
import junit.framework.TestCase;

public class ComplementTest extends TestCase {

    private final String filePathWithComma = getClass()
            .getClassLoader()
            .getResource("fields/WithCommaDelimiter.csv")
            .getPath();

    /*public void customOutputDelimiterWithComplement() {
        var args = new String[]{"--complement", "--output-delimiter=/", "-d,", "-b1-2,3-4", filePathWithComma};

        AppTest.call(args);
        Assert.assertEquals("", "cut: an input delimiter may be specified only when operating on fields");
    }*/


    public void testCustomOutputDelimiterWithComplement0() {
        var args = new String[]{"--complement", "--output-delimiter=/", "-b1-2,3-4", filePathWithComma};
        var expectedResult =
                """
                2,f3,f4,f5,f6
                3,4,5,6
                2,13,14,15,16
                2,23,24,25,26
                2,33,34,35,36
                2,43,44,45,46""";

        var response = AppTest.call(args);
        Assert.assertEquals(response, expectedResult);
    }

    public void testCustomOutputDelimiterWithComplement2() {
        var args = new String[]{"--complement", "-b1-2,4", filePathWithComma};
        var expectedResult =
                """
                ,2,f3,f4,f5,f6
                23,4,5,6
                ,2,13,14,15,16
                ,2,23,24,25,26
                ,2,33,34,35,36
                ,2,43,44,45,46""";

        var result = AppTest.call(args);

        Assert.assertEquals(expectedResult, result);
    }

    public void customOutputDelimiterWithComplement1() {
        var args = new String[]{"--complement", "--output-delimiter=/", "-b1,2,3,4", filePathWithComma};
        var expectedResult =
                """
                2,f3,f4,f5,f6
                3,4,5,6
                2,13,14,15,16
                2,23,24,25,26
                2,33,34,35,36
                2,43,44,45,46""";

        var result = AppTest.call(args);

        Assert.assertEquals(expectedResult, result);
    }
}
