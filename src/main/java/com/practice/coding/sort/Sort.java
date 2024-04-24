package com.practice.coding.sort;

import com.practice.coding.utils.Pair;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(name = "sort", description = "Write sorted concatenation of all FILE(s) to standard output.")
public class Sort implements Callable<String> {
    @Parameters(index = "0",description = "file Path")
    private List<String> filePath;

    @Option(names = {"-r", "--reverse"}, description = {"reverse the result of comparisons"})
    private boolean reverse;

    @Option(names = {"-u", "--unique"}, description = {"with -c, check for strict ordering; without -c, output only  the first of an equal run"})
    private boolean unique;

    @Option(names = {"-b", "--ignore-leading-blanks"}, description = {"ignore leading blanks"})
    private boolean ignoreLeadingBlanks;

    @Option(names = {"-f", "--ignore-case"}, description = {"fold lower case to upper case characters"})
    private boolean ignoreCase;

    private Comparator<String> simpleOrder = String::compareTo;
    private Comparator<String> reverseOrder = simpleOrder.reversed();

    private Comparator<Pair<String, String>> pairSimpleOrder = (first, second) -> {
        return simpleOrder.compare(first.getValue(), second.getValue());
    };
    private Comparator<Pair<String, String>> pairReverseOrder = pairSimpleOrder.reversed();

    @Override
    public String call() throws Exception {
        Comparator<String> toUse = simpleOrder;
        Comparator<Pair<String, String>> pairComparatorToUse = pairSimpleOrder;
        if (reverse) {
            toUse = reverseOrder;
            pairComparatorToUse = pairReverseOrder;
        }

        var linesStream = filePath.stream().map(Paths::get).flatMap(path -> {
            try {
                return Files.readAllLines(path).stream();
            } catch (IOException e) {
                return Stream.of();
            }
        });

        if (ignoreLeadingBlanks) {
            linesStream = linesStream.map(String::stripLeading);
        }

        if (ignoreCase) {
            linesStream = linesStream.map(line -> Pair.of(line, line.toUpperCase()))
                    .sorted(pairComparatorToUse)
                    .map(Pair::getKey);
        } else {
            linesStream = linesStream.sorted(toUse);
        }

        if (unique) {
            var distinct = new DistinctCharacter(ignoreCase);
            linesStream = linesStream.filter(distinct);
        }

        return linesStream.collect(Collectors.joining(System.lineSeparator()));
    }
}

class DistinctCharacter implements Predicate<String> {

    private boolean ignoreCase;
    private Set<String> uniqueSet = new HashSet<>();

    DistinctCharacter(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    @Override
    public boolean test(String line) {
        if (ignoreCase) {
            line = line.toUpperCase();
        }
        return uniqueSet.add(line);
    }
}
