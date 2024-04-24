package com.practice.coding.utils;

public interface StringUtils {
    static long getNumericPrefixValue(String content) {
        long number = 0L;
        for (int index = 0; index < content.length(); index++) {
            var ch = content.charAt(index);
            if (!Character.isDigit(ch)) {
                return number;
            }
            if (number == 0) {
                number = 1L;
            }
            number = (number * 10) + Integer.parseInt(Character.toString(ch));
        }
        return number;
    }
}
