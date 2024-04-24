package com.practice.coding.sort;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command(name = "sort", description = "Write sorted concatenation of all FILE(s) to standard output.")
public class Sort implements Callable<String> {
    @Parameters(index = "0",description = "file Path")
    private String filePath;

    @Option(names = {"-r", "--reverse"}, description = {"reverse the result of comparisons"})
    private boolean reverse;

    @Option(names = {"-u", "--unique"}, description = {"with -c, check for strict ordering; without -c, output only  the first of an equal run"})
    private boolean unique;

    @Override
    public String call() throws Exception {
        var fileLines = Files.readAllLines(Paths.get(filePath));
        Comparator<String> simpleOrder = String::compareTo;
        Comparator<String> reverseOrder = Comparator.reverseOrder();

        Comparator<String> toUse = simpleOrder;
        if (reverse) {
            toUse = reverseOrder;
        }

        var lineStream = fileLines.stream().sorted(toUse);
        if (unique) {
            lineStream = lineStream.distinct();
        }
        return lineStream.collect(Collectors.joining(System.lineSeparator()));
    }
}
