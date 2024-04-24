package com.practice.coding;

import com.practice.coding.cut.Cut;
import com.practice.coding.sort.Sort;
import picocli.CommandLine;

import java.util.*;


public class App {
    public static void main(String[] args) {
        /*var commandLine = new CommandLine(new Cut());
        var exitCode = commandLine.execute(args);
        var result = commandLine.getExecutionResult();

        var resultList = (List<String>) result;
        if (Objects.nonNull(resultList) && !resultList.isEmpty()) {
            for (var resultEntry : resultList) {
                System.out.println(resultEntry);
            }
        }*/
        var commandLine = new CommandLine(new Sort());
        var exitCode = commandLine.execute(args);
        var result = commandLine.getExecutionResult();
        System.out.println((String) result);
        System.exit(exitCode);
    }
}

