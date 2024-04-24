package com.practice.coding.sort;

import com.practice.coding.utils.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class DistinctCharacter implements Predicate<Pair<String, Object>> {
    private final Set<Object> uniqueSet = new HashSet<>();

    public DistinctCharacter() {}

    @Override
    public boolean test(Pair<String, Object> line) {
        return uniqueSet.add(line.getValue());
    }
}
