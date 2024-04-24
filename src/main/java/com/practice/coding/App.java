package com.practice.coding;

import com.practice.coding.cut.Cut;
import com.practice.coding.sort.Sort;
import picocli.CommandLine;

import javax.naming.OperationNotSupportedException;
import java.util.*;
import java.util.stream.Collectors;


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
        var sortCLI = new CommandLine(new Sort());
        var cutCLI = new CommandLine(new Cut());
        var command = args[0];


        var cli = switch (command.trim().toLowerCase()) {
            case "sort" -> sortCLI;
            case "cut" -> cutCLI;
            default -> throw new RuntimeException(String.format("Command '%s' Not Supported", command));
        };

        var commandArgs = Arrays.stream(args).skip(1).toArray(String[]::new);

        var exitCode = cli.execute(commandArgs);
        var result = cli.getExecutionResult();
        System.out.println((String) result);
        System.exit(exitCode);
    }
}

