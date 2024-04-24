package com.practice.coding.sort;

import com.practice.coding.utils.MonthUtils;
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

    @Option(names = {"-M", "--month-sort"}, description = {"compare (unknown) < 'JAN' < ... < 'DEC'"})
    private boolean monthSort;

    private static final String NON_BLANK_NON_ALPHANUMERIC_REGEX = "[^a-zA-Z0-9\\s]+";

    private Comparator<String> simpleOrder = String::compareTo;
    private Comparator<String> reverseOrder = simpleOrder.reversed();


    private Comparator<Pair<String, Object>> pairSimpleOrder = (first, second) -> {
        return simpleOrder.compare((String) first.getValue(), (String) second.getValue());
    };
    private Comparator<Pair<String, Object>> pairReverseOrder = pairSimpleOrder.reversed();

    private Comparator<Pair<String, Object>> monthSortOrder = (first, second) -> {
            var monthOrder = Integer.compare((Integer) first.getValue(), (Integer) second.getValue());
            if (monthOrder == 0) {
                return first.getKey().toUpperCase().compareTo(second.getKey().toUpperCase());
            }
            return monthOrder;
    };

    private Comparator<Pair<String, Object>> monthSortReverseOrder = monthSortOrder.reversed();

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
        if (generalNumericSort && monthSort && dictionaryOrder) {
            System.err.println("sort: options '-dgM' are incompatible");
            return "";
        }

        if (dictionaryOrder && generalNumericSort) {
            System.err.println("sort: options '-dg' are incompatible");
            return "";
        }

        if (generalNumericSort && monthSort) {
            System.err.println("sort: options '-dM' are incompatible");
            return "";
        }

        if (monthSort && dictionaryOrder) {
            System.err.println("sort: options '-gM' are incompatible");
            return "";
        }


        Comparator<Pair<String, Object>> pairComparatorToUse = getPairComparator();

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

        var pairsStream = linesStream.map(lineToPair);

//        unique filtration must happen before sorting, inorder to avoid this dependence we will need to pass original order of the entry as well with Pair & use it in sorting
        if (unique) {
            var distinct = new DistinctCharacter();
            pairsStream = pairsStream.filter(distinct);
        }

        pairsStream = pairsStream.sorted(pairComparatorToUse);

        linesStream = pairsStream.map(Pair::getKey);


        return linesStream.collect(Collectors.joining(System.lineSeparator()));
    }

    private Comparator<Pair<String, Object>> getPairComparator() {
        Comparator<Pair<String, Object>> pairComparatorToUse = pairSimpleOrder;
        if (generalNumericSort) {
            pairComparatorToUse = numericalSortOrder;
        } else if (monthSort) {
            pairComparatorToUse = monthSortOrder;
        }
        if (reverse) {
            pairComparatorToUse = pairReverseOrder;
            if (generalNumericSort) {
                pairComparatorToUse = numericalSortOrderReverse;
            } else if (monthSort) {
                pairComparatorToUse = monthSortReverseOrder;
            }
        }
        return pairComparatorToUse;
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
        } else if (monthSort) {
            if (Objects.nonNull(lineToPair)) {
                lineToPair = lineToPair.andThen(pair -> Pair.of(pair.getKey(), MonthUtils.getOrder((String) pair.getValue())));
            } else {
                lineToPair = line -> Pair.of(line, MonthUtils.getOrder(line));
            }
        }

        if (Objects.isNull(lineToPair)) {
            lineToPair = line -> Pair.of(line, line);
        }
        
        return lineToPair;
    }
}

class DistinctCharacter implements Predicate<Pair<String, Object>> {
    private final Set<Object> uniqueSet = new HashSet<>();

    public DistinctCharacter() {}

    @Override
    public boolean test(Pair<String, Object> line) {
        return uniqueSet.add(line.getValue());
    }
}
