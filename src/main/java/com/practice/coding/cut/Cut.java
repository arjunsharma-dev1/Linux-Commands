package com.practice.coding.cut;

import com.practice.coding.cut.type_converter.PositionConverter;
import com.practice.coding.cut.model.Range;
import com.practice.coding.cut.model.RangeEnd;
import com.practice.coding.cut.model.RangeImpl;
import com.practice.coding.cut.model.RangeStart;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "cut", version = "1.0.0", mixinStandardHelpOptions = true)
public class Cut implements Callable<List<String>> {
    private static final char DEFAULT_INPUT_DELIMITER = '\t';
    @Option(names = {"-d"}, description = "Delimiter")
    private char delimiter = DEFAULT_INPUT_DELIMITER;

    @Option(names = {"-b", "-c"}, description = {"select only these bytes", "select only these characters"}, converter = PositionConverter.class)
    private List<Range> ranges;

    @Option(names = {"-f"},
            description = "select only these fields;  also print any line that contains no delimiter character, unless the -s option is specified",
            converter = PositionConverter.class)
    private List<Range> fieldRanges;

    @Option(names = {"--output-delimiter"}, description = "Delimiter to be used to show for output data")
    private char outputDelimiter = delimiter;

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
                System.exit(2);
                return Reader.nullReader();
            }

            try {
                return new FileReader(file);
            } catch (FileNotFoundException e) {
                System.exit(2);
            }
        }
        return Reader.nullReader();
    }

    @Override
    public List<String> call() throws Exception {
        var isByteOrCharQuery = Objects.nonNull(ranges);
        if (isByteOrCharQuery) {
            if (delimiter != DEFAULT_INPUT_DELIMITER) {
                System.err.println("cut: an input delimiter may be specified only when operating on fields");
                System.exit(2);
            }
            if (onlyDelimited) {
                System.err.println("cut: suppressing non-delimited lines makes sense only when operating on fields");
                System.exit(2);
            }
        }

        if (outputDelimiter == DEFAULT_INPUT_DELIMITER) {
            outputDelimiter = delimiter;
        }

        Reader reader = getInput();

        if (complement) {
            if (Objects.nonNull(ranges)) {
                ranges = complementIntervals(ranges);
            } else if (Objects.nonNull(fieldRanges)) {
                fieldRanges = complementIntervals(fieldRanges);
            }
        }

        if (isByteOrCharQuery) {
            try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                return bufferedReader
                        .lines()
                        .map(line -> ranges.stream()
                                .filter((byteRange) -> line.length() > byteRange.getStart())
                                .map(byteRange -> {
                                    return line.substring(byteRange.getStart(), Math.min(line.length(), byteRange.getEnd() + 1));
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
                            var positionsForLine = fieldRanges.stream().filter(range -> range.getStart() < line.length()).toList();

                            var maxFieldColumnPosition = positionsForLine.stream()
                                    .mapToInt(range -> {
                                        return Math.max(range.getStart(), range.getEnd());
                                    })
                                    .map(position -> position + 1)
                                    .max()
                                    .orElse(line.length());

                            var split = line.split(escapedDelimiter, maxFieldColumnPosition + 1);
                            return fieldRanges.stream()
                                    .filter(entry -> {
                                        if (split.length == 1 && onlyDelimited) {
                                            return false;
                                        }
                                        return true;
                                    })
                                    .flatMap(columnRange -> {
                                        if (split.length <= columnRange.getStart()) {
                                            return Stream.of("");
                                        }
                                        return Arrays.stream(split)
                                                .skip(columnRange.getStart())
                                                .limit(columnRange.getEnd() - columnRange.getStart() + 1);
                                    })
                                    .collect(Collectors.joining(String.valueOf(outputDelimiter)));
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

    private List<Range> complementIntervals(List<Range> ranges) {
        List<Range> complementRanges = new ArrayList<>();
        if (!ranges.isEmpty()) {
            var start = ranges.get(0);
            if (start.getStart() != 0) {
                complementRanges.add(new RangeImpl(RangeStart.atStart(), RangeEnd.at(start.getStart())));
            }
        }
        for (int index = 0; index < ranges.size() - 1; index++) {
            var current = ranges.get(index);
            var next = ranges.get(index + 1);
            if (next.getStart() - current.getEnd() > 0) {
                complementRanges.add(new RangeImpl(RangeStart.at(current.getEnd()+2), RangeEnd.at(next.getStart())));
            }
        }

        if (ranges.size() > 1) {
            var last = ranges.get(ranges.size()-1);
            var end = last.getEnd() + 1;
            if (end < Integer.MAX_VALUE) {
                complementRanges.add(new RangeImpl(RangeStart.at(last.getEnd() + 2), RangeEnd.atEnd()));
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
