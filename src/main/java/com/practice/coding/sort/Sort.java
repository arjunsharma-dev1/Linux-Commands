package com.practice.coding.sort;

import com.practice.coding.utils.Pair;
import com.practice.coding.utils.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
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

    @Option(names = {"-d", "--dictionary-order"}, description = {"consider only blanks and alphanumeric characters"})
    private boolean dictionaryOrder;

    @Option(names = {"-g", "--general-numeric-sort"}, description = {"compare according to general numerical value"})
    private boolean generalNumericSort;

    private static final String NON_BLANK_NON_ALPHANUMERIC_REGEX = "[^a-zA-Z0-9\\s]+";

    private Comparator<String> simpleOrder = String::compareTo;
    private Comparator<String> reverseOrder = simpleOrder.reversed();


    private Comparator<Pair<String, Object>> pairSimpleOrder = (first, second) -> {
        return simpleOrder.compare((String) first.getValue(), (String) second.getValue());
    };
    private Comparator<Pair<String, Object>> pairReverseOrder = pairSimpleOrder.reversed();

    private Comparator<Pair<String, Object>> numericalSortOrder = (first, second) -> {
        var valueOrder = Long.compare((Long) first.getValue(), (Long) second.getValue());
        if (valueOrder == 0) {
            return first.getKey().compareTo(second.getKey());
        }
        return valueOrder;
    };

    private Comparator<Pair<String, Object>> numericalSortOrderReverse = numericalSortOrder.reversed();

    @Override
    public String call() throws Exception {
        if (dictionaryOrder && generalNumericSort) {
            System.err.println("sort: options '-dg' are incompatible");
            return "";
        }

        Comparator<Pair<String, Object>> pairComparatorToUse = pairSimpleOrder;
        if (generalNumericSort) {
            pairComparatorToUse = numericalSortOrder;
        }
        if (reverse) {
            pairComparatorToUse = pairReverseOrder;
            if (generalNumericSort) {
                pairComparatorToUse = numericalSortOrderReverse;
            }
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

        Function<String, Pair<String, Object>> lineToPair = makeMutatedCopyOfLine();

        linesStream = linesStream.map(lineToPair)
                .sorted(pairComparatorToUse)
                .map(Pair::getKey);

        if (unique) {
            var distinct = new DistinctCharacter(ignoreCase);
            linesStream = linesStream.filter(distinct);
        }

        return linesStream.collect(Collectors.joining(System.lineSeparator()));
    }

    private Function<String, Pair<String, Object>> makeMutatedCopyOfLine() {
        Function<String, Pair<String, Object>> lineToPair = null;
//        TODO: it can be both `ignoreCase` & `dictionaryOrder`
        if (ignoreCase) {
            lineToPair = line -> Pair.of(line, line.toUpperCase());
        }

        if (dictionaryOrder) {
            if (Objects.nonNull(lineToPair)) {
                lineToPair = lineToPair.andThen(pair -> Pair.of(pair.getKey(), ((String) pair.getValue()).replaceAll(NON_BLANK_NON_ALPHANUMERIC_REGEX, "")));
            } else {
                lineToPair = line -> Pair.of(line, line.replaceAll(NON_BLANK_NON_ALPHANUMERIC_REGEX, ""));
            }
        } else if (generalNumericSort) {
            if (Objects.nonNull(lineToPair)) {
                lineToPair = lineToPair.andThen(pair -> Pair.of(pair.getKey(), StringUtils.getNumericPrefixValue(pair.getKey())));
            } else {
                lineToPair = line -> Pair.of(line, StringUtils.getNumericPrefixValue(line));
            }
        }

        if (Objects.isNull(lineToPair)) {
            lineToPair = line -> Pair.of(line, line);
        }
        
        return lineToPair;
    }
}

class DistinctCharacter implements Predicate<String> {

    private final boolean ignoreCase;
    private final Set<String> uniqueSet = new HashSet<>();

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
