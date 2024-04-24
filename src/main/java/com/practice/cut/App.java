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
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;


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


class BytePositionConverter implements ITypeConverter<List<Position>> {

    @Override
    public List<Position> convert(String s) {
        var entries = s.split(",");
        return Arrays.stream(entries)
                .map(entry -> entry.split("-"))
                .map(entrySplit -> {
                    if (entrySplit.length == 1) {
                        var start = Integer.parseInt(entrySplit[0]);
                        return (Position) new BytePosition(start);
                    } else if (entrySplit.length == 2) {
                        var start = Integer.parseInt(entrySplit[0]);
                        var end = Integer.parseInt(entrySplit[1]);
                        return new RangeBytePosition(start, end);
                    } else {
                        return null;
                    }
                })
                .toList();
    }
}

interface Position {
    int getStart();
}

class BytePosition implements Position {
    private final int start;

    BytePosition(int start) {
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

class RangeBytePosition extends BytePosition {
    private final Integer end;

    RangeBytePosition(int start, int end) {
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

    @Option(names = {"-f"}, description = "Parameter Starting with -", split = ",")
    private int[] fieldColumnPosition;

    @Option(names = {"-d"}, description = "Delimiter")
    private char delimiter = '\t';

    @Option(names = {"-b", "-c"}, description = "Specify Byte to Display", converter = BytePositionConverter.class)
    private List<Position> bytesPosition;

    /*@Option(names = {"-c"}, description = "Specify Character to Display", split = ",")
    private int[] charsPositions;*/

    @Parameters(index = "0",description = "file Path")
    private String filePath;

    private Reader getInput() {
        if (Objects.isNull(filePath) || filePath.isBlank() || "-".equals(filePath.trim())) {
            return new InputStreamReader(System.in);
        } else {
            File file = Paths.get(filePath).toFile();
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

        if (Objects.nonNull(bytesPosition)) {
            try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                return bufferedReader
                        .lines()
                        .map(line -> bytesPosition.stream()
                                .filter((bytePosition) -> line.length() > bytePosition.getStart())
                                .map(bytePosition -> {
                                    if (bytePosition instanceof RangeBytePosition rangeBytePosition) {
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
            var unescapedDelimiter = String.valueOf(this.delimiter);
            try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                return bufferedReader
                        .lines()
                        .map(line -> {
                            var maxFieldColumnPosition = Arrays.stream(fieldColumnPosition).max().getAsInt();
                            var split = line.split(escapedDelimiter, maxFieldColumnPosition + 1);
                            return Arrays.stream(fieldColumnPosition)
                                    .mapToObj(columnPosition -> {
                                        if (split.length <= columnPosition - 1) {
                                            return "";
                                        }
                                        return split[columnPosition - 1];
                                    })
                                    .collect(Collectors.joining(unescapedDelimiter));
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