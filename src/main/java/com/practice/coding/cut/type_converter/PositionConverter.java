package com.practice.coding.cut.type_converter;

import com.practice.coding.utils.Pair;
import com.practice.coding.cut.model.Range;
import com.practice.coding.cut.model.RangeEnd;
import com.practice.coding.cut.model.RangeImpl;
import com.practice.coding.cut.model.RangeStart;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class PositionConverter implements CommandLine.ITypeConverter<List<Range>> {

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
