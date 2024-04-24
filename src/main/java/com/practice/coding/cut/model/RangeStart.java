package com.practice.coding.cut.model;

public class RangeStart implements RangeValue {
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
