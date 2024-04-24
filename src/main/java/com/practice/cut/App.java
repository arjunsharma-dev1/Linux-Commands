package com.practice.cut;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    /*private List<Pair<Integer, Integer>> getIntervals(String[] intervals) {
        for (var interval: intervals) {

        }
    }*/

    private static final int INVALID_INPUT = 2;

    private static final String INTERVAL_SEPARATOR = "-";
    @Override
    public List<Position> convert(String s) {
        var entries = s.split(",");

            return Arrays.stream(entries)
                .map((entry) -> {
                    var split = entry.split("-");
                    if (split.length == 0) {
                        System.err.println("cut: invalid range with no endpoint: -");
                        System.exit(INVALID_INPUT);
                    } else if (split.length > 2) {
                        System.err.println("cut: invalid field range");
                        System.exit(INVALID_INPUT);
                    }
                    if (entry.startsWith(INTERVAL_SEPARATOR)) {
                        return new RangePositionImpl(1, Integer.parseInt(split[1]));
                    } else if (entry.endsWith(INTERVAL_SEPARATOR)) {
                        return new RangePositionImpl(Integer.parseInt(split[0]), Integer.MAX_VALUE);
                    } else if (entry.contains("-")) {
                        return new RangePositionImpl(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                    } else {
                        return new PositionImpl(Integer.parseInt(split[0]));
                    }
                })
                .map(position -> (Position) position)
                .toList();
    }
}

interface Position {
    int getStart();
}

class PositionImpl implements Position {
    private final int start;

    PositionImpl(int start) {
        this.start = start - 1;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public String toString() {
        return String.format(" start: %s ", start);
    }
}

class RangePositionImpl extends PositionImpl {
    private final Integer end;

    RangePositionImpl(int start, int end) {
        super(start);
        this.end = end - 1;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return String.format("%s, end: %s", super.toString(), end);
    }
}


@Command(name = "cut", version = "1.0.0", mixinStandardHelpOptions = true)
class Cut implements Callable<List<String>> {
    @Option(names = {"-d"}, description = "Delimiter")
    private char delimiter = '\t';

    @Option(names = {"-b", "-c"}, description = "Specify Byte to Display", converter = PositionConverter.class)
    private List<Position> positions;

    @Option(names = {"-f"}, description = "Specify Fields to Display", converter = PositionConverter.class)
    private List<Position> fieldPositions;

    @Option(names = {"--output-delimiter"}, description = "Delimiter to be used to show for output data")
    private String outputDelimiter = String.valueOf(delimiter);
    @Parameters(index = "0",description = "file Path")
    private String filePath;

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
        Reader reader = getInput();
        if (Objects.isNull(reader)) {
            return List.of();
        }

        if (Objects.nonNull(positions)) {
            try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                return bufferedReader
                        .lines()
                        .map(line -> positions.stream()
                                .filter((bytePosition) -> line.length() > bytePosition.getStart())
                                .map(bytePosition -> {
                                    if (bytePosition instanceof RangePositionImpl rangeBytePosition) {
                                        return line.substring(rangeBytePosition.getStart(), rangeBytePosition.getEnd()+1);
                                    } else {
                                        return Character.toString(line.charAt(bytePosition.getStart()));
                                    }
                                })
                                .map(Object::toString)
                                .collect(Collectors.joining(""))
                        )
                        .toList();
            }
        } else {
            var escapedDelimiter = escapeDelimiterIfReserved(this.delimiter);
//            var unescapedDelimiter = String.valueOf(this.delimiter);
            try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                return bufferedReader
                        .lines()
                        .map(line -> {
                            var positionsForLine = fieldPositions.stream().filter(position -> position.getStart() < line.length()).toList();

                            var maxFieldColumnPosition = positionsForLine.stream()
                                    .mapToInt(position -> {
                                        if (position instanceof RangePositionImpl rangePosition) {
                                            return Math.max(rangePosition.getStart(), rangePosition.getEnd());
                                        } else {
                                            return position.getStart();
                                        }
                                    })
                                    .map(position -> position + 1)
                                    .max()
                                    .orElse(line.length()); 

                            var split = line.split(escapedDelimiter, maxFieldColumnPosition + 1);
                            return fieldPositions.stream()
                                    .flatMap(columnPosition -> {
                                        if (split.length <= columnPosition.getStart()) {
                                            return Stream.of("");
                                        }
                                        if (columnPosition instanceof  RangePositionImpl rangePosition) {
                                            return Arrays.stream(split).skip(rangePosition.getStart()).limit(rangePosition.getEnd() - rangePosition.getStart() + 1);
                                        } else {
                                            return Stream.of(split[columnPosition.getStart()]);
                                        }
                                    })
                                    .collect(Collectors.joining(outputDelimiter));
                        })
                        .toList();
            }
        }
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