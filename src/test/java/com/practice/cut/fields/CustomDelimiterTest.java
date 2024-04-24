package com.practice.cut.fields;

import com.practice.cut.AppTest;
import junit.framework.TestCase;

public class CustomDelimiterTest extends TestCase {

    private final String filePathWithComma = getClass()
            .getClassLoader()
            .getResource("fields/WithCommaDelimiter.csv")
            .getPath();

    private final String filePathWithDash = getClass()
            .getClassLoader()
            .getResource("fields/WithDashDelimiter.csv")
            .getPath();


    private final String filepathWithSomeNonDelimitedLines = getClass()
            .getClassLoader()
            .getResource("fields/WithSomeNonDelimitedLines")
            .getPath();

    public void testWithCommaDelimiter() {
        var args = new String[]{"-d,", "-f1-4", filePathWithComma};
        var expectedResult =
                """ 
                f1,f2,f3,f4
                1,2,3,4
                11,12,13,14
                21,22,23,24
                31,32,33,34
                41,42,43,44""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }

    public void testWithDashDelimiter() {
        var args = new String[]{"-d-", "-f1-4", filePathWithDash};
        var expectedResult =
                """ 
                f1-f2-f3-f4
                1-2-3-4
                11-12-13-14
                21-22-23-24
                31-32-33-34
                41-42-43-44""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }

    public void testCustomOutputDelimiter() {
        var args = new String[]{"--output-delimiter=/", "-d-", "-f1-4", filePathWithDash};
        var expectedResult =
                """ 
                f1/f2/f3/f4
                1/2/3/4
                11/12/13/14
                21/22/23/24
                31/32/33/34
                41/42/43/44""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }

    public void testCustomOutputDelimiterAndOnlyDelimitedFlag() {
        var args = new String[]{"-s", "--output-delimiter=/", "-d-", "-f1-4", filepathWithSomeNonDelimitedLines };
        var expectedResult =
                """ 
                f1/f2/f3/f4
                11/12/13/14
                21/22/23/24
                41/42/43/44""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }
}
