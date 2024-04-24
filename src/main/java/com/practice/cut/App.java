package com.practice.cut;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class App {
    public static void main( String[] args ) {
        var commandLine = new CommandLine(new Cut());
        var exitCode = commandLine.execute(args);
        var result = commandLine.getExecutionResult();

        var resultList = (List<String>) result;
        if (Objects.nonNull(resultList) && !resultList.isEmpty()) {
            for (var resultEntry : resultList) {
                System.out.println(resultEntry);
            }
        }
        System.exit(exitCode);
    }
}


class Pair<K, V> {
    final K key;
    final V value;

    private Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}

class PositionConverter implements ITypeConverter<List<Position>> {

    private List<Pair<Integer, Integer>> getIntervals(String[] intervals) {
        PriorityQueue<Pair<Integer, Integer>> pqueue = new PriorityQueue<>(Comparator.comparingInt(Pair::getKey));
        for (var interval: intervals) {
            var split = interval.split("-");
            if (split.length == 0) {
                System.err.println("cut: invalid range with no endpoint: -");
                System.exit(INVALID_INPUT);
            } else if (split.length > 2) {
                System.err.println("cut: invalid field range");
                System.exit(INVALID_INPUT);
            }
            var first = split[0].isBlank()? 1 : Integer.parseInt(split[0]);
            var second = split.length == 1 || split[1].isBlank()? Integer.MAX_VALUE: Integer.parseInt(split[1]);
            pqueue.add(Pair.of(first, second));
        }
        List<Pair<Integer, Integer>> mergedIntervals = new ArrayList<>();

        while(!pqueue.isEmpty()) {
            var first = pqueue.poll();
            if (pqueue.isEmpty()) {
                mergedIntervals.add(first);
                break;
            }
            var second = pqueue.poll();
            if (first.getValue() >= second.getKey()) {
                pqueue.add(Pair.of(
                        Math.min(first.getKey(), second.getKey()),
                        Math.max(first.getValue(), second.getValue())
                ));
            } else {
                mergedIntervals.add(first);
                pqueue.add(second);
            }
        }
        return mergedIntervals;
    }

    private static final int INVALID_INPUT = 2;

    private static final String INTERVAL_SEPARATOR = "-";
    @Override
    public List<Position> convert(String s) {
        var entries = s.split(",");

        var intervals = getIntervals(entries);

        return intervals.stream()
                .map(intervalPair -> new RangePositionImpl(IntervalStart.at(intervalPair.getKey()), IntervalEnd.at(intervalPair.getValue())))
                .map(position -> (Position) position)
                .toList();
    }
}

interface Position {
    int getStart();
    int getEnd();
}

interface IntervalValue {
    int getValue();
}

class IntervalStart implements IntervalValue {
    private final int position;

    IntervalStart(int position) {
        this.position = position;
    }
    public static IntervalStart atStart() {
        return new IntervalStart(1);
    }

    public static IntervalStart at(int position) {
        return new IntervalStart(position);
    }

    public static IntervalStart at(String position) {
        return new IntervalStart(Integer.parseInt(position));
    }

    @Override
    public int getValue() {
        return position;
    }
}

class IntervalEnd implements IntervalValue {
    private final int position;

    IntervalEnd(int position) {
        this.position = position;
    }

    public static IntervalEnd atEnd() {
        return new IntervalEnd(Integer.MAX_VALUE);
    }

    public static IntervalEnd at(int position) {
        return new IntervalEnd(position);
    }

    public static IntervalEnd at(String position) {
        return new IntervalEnd(Integer.parseInt(position));
    }

    @Override
    public int getValue() {
        return position;
    }
}

class RangePositionImpl implements Position {
    private IntervalStart start;

    private IntervalEnd end;

    RangePositionImpl(IntervalStart start) {
        this.start = start;
        this.end = IntervalEnd.atEnd();
    }

    RangePositionImpl(IntervalEnd end) {
        this(IntervalStart.atStart(), end);
    }

    @Override
    public int getStart() {
        return start.getValue() - 1;
    }

    RangePositionImpl(IntervalStart start, IntervalEnd end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int getEnd() {
        return end.getValue() - 1;
    }

    @Override
    public String toString() {
        return String.format(" start: %s , end: %s", start, end);
    }
}


@Command(name = "cut", version = "1.0.0", mixinStandardHelpOptions = true)
class Cut implements Callable<List<String>> {
    private static final char DEFAULT_INPUT_DELIMITER = '\t';
    @Option(names = {"-d"}, description = "Delimiter")
    private char delimiter = DEFAULT_INPUT_DELIMITER;

    @Option(names = {"-b", "-c"}, description = "select only these bytes", converter = PositionConverter.class)
    private List<Position> positions;

    @Option(names = {"-f"},
            description = "select only these fields;  also print any line that contains no delimiter character, unless the -s option is specified",
            converter = PositionConverter.class)
    private List<Position> fieldPositions;

    @Option(names = {"--output-delimiter"}, description = "Delimiter to be used to show for output data")
    private String outputDelimiter = String.valueOf(delimiter);

    @Option(names = {"--complement"}, description = "complement the set of selected bytes, characters or fields")
    private boolean complement;

    @Parameters(index = "0",description = "file Path")
    private String filePath;

    @Option(names = {"-s", "--only-delimited"}, description = "do not print lines not containing delimiters")
    private boolean onlyDelimited;

    private Reader getInput() {
        if (Objects.isNull(filePath) || filePath.isBlank() || "-".equals(filePath.trim())) {
            return new InputStreamReader(System.in);
        } else {
            var file = Paths.get(filePath).toFile();
            if(!file.exists()) {
                System.err.printf("cut: %s: No such file or directory%s", file.getName(), System.lineSeparator());
                return null;
            }

            try {
                return new FileReader(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<String> call() throws Exception {
        if (Objects.nonNull(positions)) {
            if (delimiter != DEFAULT_INPUT_DELIMITER) {
                System.err.println("cut: an input delimiter may be specified only when operating on fields");
            }
            if (onlyDelimited) {
                System.err.println("cut: suppressing non-delimited lines makes sense only when operating on fields");
            }
        }

        Reader reader = getInput();
        if (Objects.isNull(reader)) {
            return List.of();
        }

        if (complement) {
            if (Objects.nonNull(positions)) {
                positions = complementIntervals(positions);
            } else if (Objects.nonNull(fieldPositions)) {
                fieldPositions = complementIntervals(fieldPositions);
            }
        }

        if (Objects.nonNull(positions)) {
            try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                return bufferedReader
                        .lines()
                        .map(line -> positions.stream()
                                .filter((bytePosition) -> line.length() > bytePosition.getStart())
                                .map(bytePosition -> {
                                    return line.substring(bytePosition.getStart(), Math.min(line.length(), bytePosition.getEnd() + 1));
                                })
                                .map(Object::toString)
                                .collect(Collectors.joining(""))
                        )
                        .toList();
            }
        } else {
            var escapedDelimiter = escapeDelimiterIfReserved(this.delimiter);
            try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                return bufferedReader
                        .lines()
                        .map(line -> {
                            var positionsForLine = fieldPositions.stream().filter(position -> position.getStart() < line.length()).toList();

                            var maxFieldColumnPosition = positionsForLine.stream()
                                    .mapToInt(position -> {
                                            return Math.max(position.getStart(), position.getEnd());
                                    })
                                    .map(position -> position + 1)
                                    .max()
                                    .orElse(line.length()); 

                            var split = line.split(escapedDelimiter, maxFieldColumnPosition + 1);
                            return fieldPositions.stream()
                                    .filter(entry -> {
                                        if (split.length == 1 && onlyDelimited) {
                                            return false;
                                        }
                                        return true;
                                    })
                                    .flatMap(columnPosition -> {
                                        if (split.length <= columnPosition.getStart()) {
                                            return Stream.of("");
                                        }
                                        return Arrays.stream(split)
                                                .skip(columnPosition.getStart())
                                                .limit(columnPosition.getEnd() - columnPosition.getStart() + 1);
                                    })
                                    .collect(Collectors.joining(outputDelimiter));
                        })
                        .filter(response -> {
                            if (onlyDelimited && response.isBlank()) {
                                return false;
                            }
                            return true;
                        })
                        .toList();
            }
        }
    }

    private List<Position> complementIntervals(List<Position> positions) {
        List<Position> complementRanges = new ArrayList<>();
        if (!positions.isEmpty()) {
            var start = positions.get(0);
            if (start.getStart() != 0) {
                complementRanges.add(new RangePositionImpl(IntervalStart.atStart(), IntervalEnd.at(start.getStart())));
            }
        }
        for (int index = 0; index < positions.size() - 1; index++) {
            var current = positions.get(index);
            var next = positions.get(index + 1);
            if (next.getStart() - current.getEnd() > 0) {
                complementRanges.add(new RangePositionImpl(IntervalStart.at(current.getEnd()+2), IntervalEnd.at(next.getStart())));
            }
        }

        if (positions.size() > 1) {
            var last = positions.get(positions.size()-1);
            var end = last.getEnd() + 1;
            if (end < Integer.MAX_VALUE) {
                complementRanges.add(new RangePositionImpl(IntervalStart.at(last.getEnd() + 2), IntervalEnd.atEnd()));
            }
        }
        return complementRanges;
    }

    private String escapeDelimiterIfReserved(char delimiter) {
        var reserved = ".^$*+?|()[]{}\\";
        var toString = String.valueOf(delimiter);
        if (reserved.contains(toString)) {
            return "\\" + toString;
        }
        return toString;
    }
}