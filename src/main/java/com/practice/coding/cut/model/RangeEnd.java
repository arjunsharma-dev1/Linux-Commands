package com.practice.coding.cut.model;

public class RangeEnd implements RangeValue {
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
