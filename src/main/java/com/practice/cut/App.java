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

class PositionConverter implements ITypeConverter<List<Range>> {

    private static final int INVALID_INPUT = 2;

    private static final String INTERVAL_SEPARATOR = "-";

    private List<Pair<Integer, Integer>> getIntervals(String[] intervals) {
        PriorityQueue<Pair<Integer, Integer>> pqueue = new PriorityQueue<>(Comparator.comparingInt(Pair::getKey));
        for (var interval: intervals) {
            var split = interval.split(INTERVAL_SEPARATOR);
            if (split.length == 0) {
                System.err.println("cut: invalid range with no endpoint: -");
                System.exit(INVALID_INPUT);
            } else if (split.length > 2) {
                System.err.println("cut: invalid field range");
                System.exit(INVALID_INPUT);
            }
            var first = split[0].isBlank()? 1 : Integer.parseInt(split[0]);
            var second = first;
            if (interval.contains("-")) {
                if (split.length == 1 || split[1].isBlank()) {
                    second = Integer.MAX_VALUE;
                } else {
                    second = Integer.parseInt(split[1]);
                }
            }
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

    @Override
    public List<Range> convert(String s) {
        var entries = s.split(",");

        var intervals = getIntervals(entries);

        return intervals.stream()
                .map(intervalPair -> new RangeImpl(RangeStart.at(intervalPair.getKey()), RangeEnd.at(intervalPair.getValue())))
                .map(position -> (Range) position)
                .toList();
    }
}

interface Range {
    int getStart();
    int getEnd();
}

interface RangeValue {
    int getValue();
}

class RangeStart implements RangeValue {
    private final int position;

    RangeStart(int position) {
        this.position = position;
    }
    public static RangeStart atStart() {
        return new RangeStart(1);
    }

    public static RangeStart at(int position) {
        return new RangeStart(position);
    }

    public static RangeStart at(String position) {
        return new RangeStart(Integer.parseInt(position));
    }

    @Override
    public int getValue() {
        return position;
    }
}

class RangeEnd implements RangeValue {
    private final int position;

    RangeEnd(int position) {
        this.position = position;
    }

    public static RangeEnd atEnd() {
        return new RangeEnd(Integer.MAX_VALUE);
    }

    public static RangeEnd at(int position) {
        return new RangeEnd(position);
    }

    public static RangeEnd at(String position) {
        return new RangeEnd(Integer.parseInt(position));
    }

    @Override
    public int getValue() {
        return position;
    }
}

class RangeImpl implements Range {
    private RangeStart start;

    private RangeEnd end;

    RangeImpl(RangeStart start) {
        this.start = start;
        this.end = RangeEnd.atEnd();
    }

    RangeImpl(RangeEnd end) {
        this(RangeStart.atStart(), end);
    }

    @Override
    public int getStart() {
        return start.getValue() - 1;
    }

    RangeImpl(RangeStart start, RangeEnd end) {
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