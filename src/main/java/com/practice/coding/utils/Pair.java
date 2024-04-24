package com.practice.coding.utils;

public class Pair<K, V> {
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

    @Override
    public String toString() {
        return String.format("key: '%s', value: '%s'", this.getKey(), this.getValue());
    }
}
