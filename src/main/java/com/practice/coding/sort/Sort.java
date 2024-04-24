package com.practice.coding.sort;

import com.practice.coding.sort.comparators.ComparatorUtils;
import com.practice.coding.sort.comparators.FileUtils;
import com.practice.coding.utils.MonthUtils;
import com.practice.coding.utils.Pair;
import com.practice.coding.utils.SortUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

@Command(name = "sort", description = "Write sorted concatenation of all FILE(s) to standard output.")
public class Sort implements Callable<String> {

    @Parameters(index = "0", description = "file Path")
    private List<String> filePaths;

    @Option(names = {"-r", "--reverse"},
            description = {"reverse the result of comparisons"})
    private boolean reverse;

    @Option(names = {"-u", "--unique"},
            description = {"with -c, check for strict ordering; without -c, output only  the first of an equal run"})
    private boolean unique;

    @Option(names = {"-b", "--ignore-leading-blanks"},
            description = {"ignore leading blanks"})
    private boolean ignoreLeadingBlanks;

    @Option(names = {"-f", "--ignore-case"},
            description = {"fold lower case to upper case characters"})
    private boolean ignoreCase;

    @Option(names = {"-d", "--dictionary-order"},
            description = {"consider only blanks and alphanumeric characters"})
    private boolean dictionaryOrder;

    @Option(names = {"-g", "--general-numeric-sort"},
            description = {"compare according to general numerical value"})
    private boolean generalNumericSort;

    @Option(names = {"-M", "--month-sort"},
            description = {"compare (unknown) < 'JAN' < ... < 'DEC'"})
    private boolean monthSort;

    @Option(names = {"-n", "--numeric-sort"},
            description = {"compare according to string numerical value"})
    private boolean numericSort;

    @Option(names = {"-h", "--human-numeric-sort"},
            description = {"compare human readable numbers (e.g., 2K 1G)"})
    private boolean humanNumericSort;

    @Option(names = {"-V", "--version-sort"},
            description = {"natural sort of (version) numbers within text"})
    private boolean versionSort;

    @Option(names = {"-R", "--random-sort"},
            description = {"shuffle, but group identical keys"})
    private boolean randomSort;

    private static final String NON_BLANK_NON_ALPHANUMERIC_REGEX = "[^a-zA-Z0-9\\s]+";

    @Override
    public String call() throws Exception {
//        TODO: need to define `type` for each flag & use FlagConverter get the boolean value in that `type`
//        TODO: create a method which take all of these flag `types` and throw errors on illegal combinations

        ErrorUtils.reportIncompatiblePairs(
                generalNumericSort,
                dictionaryOrder,
                monthSort,
                numericSort,
                versionSort,
                randomSort
        );

        var pairComparatorToUse = ComparatorUtils.getPairComparator(
                reverse,
                generalNumericSort,
                numericSort,
                monthSort,
                humanNumericSort,
                versionSort,
                randomSort
        );

        var linesStream = FileUtils.getLineStream(filePaths);

        if (ignoreLeadingBlanks) {
            linesStream = linesStream.map(String::stripLeading);
        }

        var lineToPair = makeMutatedCopyOfLine();

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
                lineToPair = lineToPair.andThen(pair -> Pair.of(pair.getKey(), SortUtils.getNumericPrefixValueForGeneral(pair.getKey())));
            } else {
                lineToPair = line -> Pair.of(line, SortUtils.getNumericPrefixValueForGeneral(line));
            }
        } else if (numericSort) {
            if (Objects.nonNull(lineToPair)) {
                lineToPair = lineToPair.andThen(pair -> Pair.of(pair.getKey(), SortUtils.getNumericPrefixValue(pair.getKey())));
            } else {
                lineToPair = line -> Pair.of(line, SortUtils.getNumericPrefixValue(line));
            }
        } else if (monthSort) {
            if (Objects.nonNull(lineToPair)) {
                lineToPair = lineToPair.andThen(pair -> Pair.of(pair.getKey(), MonthUtils.getOrder((String) pair.getValue())));
            } else {
                lineToPair = line -> Pair.of(line, MonthUtils.getOrder(line));
            }
        } else if (humanNumericSort) {
            if (Objects.nonNull(lineToPair)) {
                lineToPair = lineToPair.andThen(pair -> Pair.of(pair.getKey(), SortUtils.getHumanNumericUnitOrder((String) pair.getValue())));
            } else {
                lineToPair = line -> Pair.of(line, SortUtils.getHumanNumericUnitOrder(line));
            }
        } else if (versionSort) {
            if (Objects.nonNull(lineToPair)) {
                lineToPair = lineToPair.andThen(pair -> Pair.of(pair.getKey(), SortUtils.getAlphanumeric((String) pair.getValue())));
            } else {
                lineToPair = line -> Pair.of(line, SortUtils.getAlphanumeric(line));
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
