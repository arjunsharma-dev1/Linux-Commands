package com.practice.coding.cut.model;

public class RangeImpl implements Range {
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

    public RangeImpl(RangeStart start, RangeEnd end) {
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
