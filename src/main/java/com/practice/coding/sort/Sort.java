package com.practice.coding.sort;

import com.practice.coding.utils.MonthUtils;
import com.practice.coding.utils.Pair;
import com.practice.coding.utils.StringUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.io.Writer;
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

    /*interface SortOptionConstants {
        String REVERSE_SHORT = "-r";
        String REVERSE_LONG = "--reverse";

        String UNIQUE_SHORT = "-u";
        String UNIQUE_LONG = "--unique";
    }*/

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

    @Option(names = {"-n", "--numeric-sort"}, description = {"compare according to string numerical value"})
    private boolean numericSort;

    @Option(names = {"-h", "--human-numeric-sort"}, description = {"compare human readable numbers (e.g., 2K 1G)"})
    private boolean humanNumericSort;

    @Option(names = {"-V", "--version-sort"}, description = {"natural sort of (version) numbers within text"})
    private boolean versionSort;

    @Option(names = {"-R", "--random-sort"}, description = {"shuffle, but group identical keys"})
    private boolean randomSort;

    private static final String NON_BLANK_NON_ALPHANUMERIC_REGEX = "[^a-zA-Z0-9\\s]+";

    private final Comparator<String> simpleOrder = String::compareTo;
    private final Comparator<String> reverseOrder = simpleOrder.reversed();


    private final Comparator<Pair<String, Object>> pairSimpleOrder = (first, second) -> {
        return simpleOrder.compare((String) first.getValue(), (String) second.getValue());
    };
    private final Comparator<Pair<String, Object>> pairReverseOrder = pairSimpleOrder.reversed();

    private final Comparator<Pair<String, Object>> monthSortOrder = (first, second) -> {
            var monthOrder = Integer.compare((Integer) first.getValue(), (Integer) second.getValue());
            if (monthOrder == 0) {
                return first.getKey().toUpperCase().compareTo(second.getKey().toUpperCase());
            }
            return monthOrder;
    };

    private final Comparator<Pair<String, Object>> monthSortReverseOrder = monthSortOrder.reversed();

    private final Comparator<Pair<String, Object>> generalNumericalSortOrder = (first, second) -> {
        var valueOrder = Double.compare((Double) first.getValue(), (Double) second.getValue());
        if (valueOrder == 0) {
            return first.getKey().compareTo(second.getKey());
        }
        return valueOrder;
    };

    private final Comparator<Pair<String, Object>> generalNumericalSortOrderReverse = generalNumericalSortOrder.reversed();

    private final Comparator<Pair<String, Object>> numericalSortOrder = (first, second) -> {
        var valueOrder = Float.compare((Float) first.getValue(), (Float) second.getValue());
        if (valueOrder == 0) {
            return first.getKey().compareTo(second.getKey());
        }
        return valueOrder;
    };

    private final Comparator<Pair<String, Object>> numericalSortOrderReverse = numericalSortOrder.reversed();

    private final Comparator<Pair<String, Object>> humanNumericalSortOrder = (first, second) -> {
        var firstPair = (Pair<Float, Integer>) first.getValue();
        var secondPair = (Pair<Float, Integer>) second.getValue();

        var humanReadableUnitOrder = Integer.compare(firstPair.getValue(), secondPair.getValue());
        if (humanReadableUnitOrder == 0) {
            var magnitude = Float.compare(firstPair.getValue(),secondPair.getValue());
//            TODO: Need to revisit this
            if (magnitude == 0) {
                return first.getKey().compareTo(second.getKey());
            }
            return magnitude;
        }
        return humanReadableUnitOrder;
    };

    private final Comparator<Pair<String, Object>> humanNumericalSortReverseOrder = generalNumericalSortOrder.reversed();


    private final Comparator<Pair<String, Object>> versionSortOrder = (first, second) -> {
        var firstValue = (String) first.getValue();
        var secondValue = (String) second.getValue();
        var isFirstAlphabetic = firstValue.chars().anyMatch(StringUtils.isAlphabetic);
        var isSecondAlphabetic = secondValue.chars().anyMatch(StringUtils.isAlphabetic);

        if (isFirstAlphabetic && isSecondAlphabetic) {
            return firstValue.compareTo(secondValue);
        } else if (isFirstAlphabetic) {
            return 1;
        } else if (isSecondAlphabetic) {
            return -1;
        } else {
            return firstValue.compareTo(secondValue);
        }
    };

    private final Comparator<Pair<String, Object>> versionSortReverseOrder = versionSortOrder.reversed();


    private final Comparator<Pair<String, Object>> randomSortOrder = (first, second) -> {
        long firstOrder = (long) first.getValue();
        long secondOrder = (long) second.getValue();
        return Long.compare(firstOrder, secondOrder);
    };
    private final Comparator<Pair<String, Object>> randomSortReverseOrder = randomSortOrder.reversed();

    @Override
    public String call() throws Exception {
//        TODO: need to define `type` for each flag & use FlagConverter get the boolean value in that `type`
//        TODO: create a method which take all of these flag `types` and throw errors on illegal combinations


        reportIncompatiblePairs(
                generalNumericSort,
                dictionaryOrder,
                monthSort,
                numericSort,
                versionSort,
                randomSort
        );

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

    private void reportIncompatiblePairs(boolean generalNumericSort,
                                         boolean dictionaryOrder,
                                         boolean monthSort,
                                         boolean numericSort,
                                         boolean versionSort,
                                         boolean randomSort) {
        var sb = new StringBuilder();
        if (generalNumericSort) {
            sb.append("g");
        }
        if (dictionaryOrder) {
            sb.append("d");
        }
        if (monthSort) {
            sb.append("M");
        }
        if (numericSort) {
            sb.append("n");
        }
        if (versionSort) {
            sb.append("V");
        }
        if (randomSort) {
            sb.append("R");
        }

        if (sb.length() > 1) {
            System.err.printf("sort: options '-%s' are incompatible%n", sb);
            System.exit(2);
        }
    }

    private Comparator<Pair<String, Object>> getPairComparator() {
        Comparator<Pair<String, Object>> pairComparatorToUse = pairSimpleOrder;
        if (generalNumericSort) {
            pairComparatorToUse = generalNumericalSortOrder;
        } else if (numericSort) {
            pairComparatorToUse = numericalSortOrder;
        } else if (monthSort) {
            pairComparatorToUse = monthSortOrder;
        } else if (humanNumericSort) {
            pairComparatorToUse = humanNumericalSortOrder;
        } else if (versionSort) {
            pairComparatorToUse = versionSortOrder;
        } else if (randomSort) {
            pairComparatorToUse = randomSortOrder;
        }
        if (reverse) {
            pairComparatorToUse = pairReverseOrder;
            if (generalNumericSort) {
                pairComparatorToUse = generalNumericalSortOrderReverse;
            } else if (numericSort) {
                pairComparatorToUse = numericalSortOrderReverse;
            } else if (monthSort) {
                pairComparatorToUse = monthSortReverseOrder;
            } else if (humanNumericSort) {
                pairComparatorToUse = humanNumericalSortReverseOrder;
            } else if (versionSort) {
                pairComparatorToUse = versionSortReverseOrder;
            } else if (randomSort) {
                pairComparatorToUse = randomSortReverseOrder;
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
                lineToPair = lineToPair.andThen(pair -> Pair.of(pair.getKey(), StringUtils.getNumericPrefixValueForGeneral(pair.getKey())));
            } else {
                lineToPair = line -> Pair.of(line, StringUtils.getNumericPrefixValueForGeneral(line));
            }
        } else if (numericSort) {
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
        } else if (humanNumericSort) {
            if (Objects.nonNull(lineToPair)) {
                lineToPair = lineToPair.andThen(pair -> Pair.of(pair.getKey(), StringUtils.getHumanNumericUnitOrder((String) pair.getValue())));
            } else {
                lineToPair = line -> Pair.of(line, StringUtils.getHumanNumericUnitOrder(line));
            }
        } else if (versionSort) {
            if (Objects.nonNull(lineToPair)) {
                lineToPair = lineToPair.andThen(pair -> Pair.of(pair.getKey(), StringUtils.getAlphanumeric((String) pair.getValue())));
            } else {
                lineToPair = line -> Pair.of(line, StringUtils.getAlphanumeric(line));
            }
        } else if (randomSort) {
            var randomOrder = new RandomSortOrder();
            if (Objects.nonNull(lineToPair)) {
                lineToPair = lineToPair.andThen(pair -> Pair.of(pair.getKey(), randomOrder.getOrder((String) pair.getValue())));
            } else {
                lineToPair = line -> Pair.of(line, randomOrder.getOrder(line));
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
