package com.practice.coding;

import com.practice.coding.cut.Cut;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }


    public static String call(String... args) {
        var commandLine = new CommandLine(new Cut());
        var exitCode = commandLine.execute(args);
        var result = commandLine.getExecutionResult();
        return ((List<String>) result).stream().collect(Collectors.joining("\n"));
    }
}
