package com.practice.coding.sort;

import java.util.HashMap;
import java.util.Map;
import java.util.random.RandomGenerator;

public class RandomSortOrder {
    private final Map<String, Long> orderMap = new HashMap<>();

    private final RandomGenerator randomNumberGenerator = RandomGenerator.getDefault();

    public long getOrder(String value) {
        var randomOrder = randomNumberGenerator.nextLong();
        var order = orderMap.getOrDefault(value, randomOrder);
        orderMap.putIfAbsent(value, order);
        return order;
    }
}
