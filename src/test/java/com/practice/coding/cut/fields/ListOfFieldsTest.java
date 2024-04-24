package com.practice.coding.cut.fields;

import com.practice.coding.AppTest;
import junit.framework.TestCase;

public class ListOfFieldsTest extends TestCase {

    private final String filePath = getClass()
            .getClassLoader()
            .getResource("fields/ListOfFields.csv")
            .getPath();
    public void testCase() throws Exception {
        var args = new String[]{"-f1,2,3", filePath};
        var expectedResult =
                """
                f1\tf2\tf3
                1\t2\t3
                11\t12\t13
                21\t22\t23
                31\t32\t33
                41\t42\t43""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }
}
