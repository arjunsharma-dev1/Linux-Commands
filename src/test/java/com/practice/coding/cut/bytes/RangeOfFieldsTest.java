package com.practice.coding.cut.bytes;

import com.practice.coding.AppTest;
import junit.framework.TestCase;

public class RangeOfFieldsTest extends TestCase {

    private final String filePath = getClass()
            .getClassLoader()
            .getResource("fields/ListOfFields.csv")
            .getPath();

    public void testClosedRange() {
        var args = new String[]{"-b1-2,5-6", filePath};
        var expectedResult =
                """
                f12\t
                1\t3\t
                112\t
                212\t
                312\t
                412\t""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }

    public void testOpenEndRange() {
        var args = new String[]{"-b3-", filePath};
        var expectedResult =
                """
                \tf2\tf3\tf4\tf5\tf6
                2\t3\t4\t5\t6
                \t12\t13\t14\t15\t16
                \t22\t23\t24\t25\t26
                \t32\t33\t34\t35\t36
                \t42\t43\t44\t45\t46""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }

    public void testOpenStartRange() {
        var args = new String[]{"-b-3", filePath};
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

    public void testOverlappingRange() {
        var args = new String[]{"-b1-2,3-4,-4", filePath};
        var expectedResult =
                """ 
                f1\tf
                1\t2\t
                11\t1
                21\t2
                31\t3
                41\t4""";

        var result = AppTest.call(args);

        assertEquals(expectedResult, result);
    }
}
