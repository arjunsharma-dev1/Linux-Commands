package com.practice.cut.fields;

import com.practice.cut.AppTest;
import junit.framework.TestCase;

public class RangeOfFieldsTest extends TestCase {

    private final String filePath = getClass()
            .getClassLoader()
            .getResource("fields/ListOfFields.csv")
            .getPath();

    public void testClosedRange() {
        var args = new String[]{"-f1-2,5-6", filePath};
        var expectedResult =
                """
                f1\tf2\tf5\tf6
                1\t2\t5\t6
                11\t12\t15\t16
                21\t22\t25\t26
                31\t32\t35\t36
                41\t42\t45\t46""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }

    public void testOpenEndRange() {
        var args = new String[]{"-f3-", filePath};
        var expectedResult =
                """
                f3\tf4\tf5\tf6
                3\t4\t5\t6
                13\t14\t15\t16
                23\t24\t25\t26
                33\t34\t35\t36
                43\t44\t45\t46""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }

    public void testOpenStartRange() {
        var args = new String[]{"-f-3", filePath};
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

    public void testOverlappingRange() {
        var args = new String[]{"-f1-2,3-4,-4", filePath};
        var expectedResult =
                """ 
                f1\tf2\tf3\tf4
                1\t2\t3\t4
                11\t12\t13\t14
                21\t22\t23\t24
                31\t32\t33\t34
                41\t42\t43\t44""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }
}
