package com.practice.cut.bytes;

import com.practice.cut.AppTest;
import junit.framework.TestCase;

public class ListOfFieldsTest extends TestCase {

    private final String filePath = getClass()
            .getClassLoader()
            .getResource("fields/ListOfFields.csv")
            .getPath();

    public void testCase() throws Exception {
        var args = new String[]{"-b1,2,3", filePath};
        var expectedResult =
                """
                f1\t
                1\t2
                11\t
                21\t
                31\t
                41\t""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }
}
