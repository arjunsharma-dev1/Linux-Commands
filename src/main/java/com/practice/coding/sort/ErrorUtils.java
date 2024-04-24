package com.practice.coding.sort;

public interface ErrorUtils {
    static void reportIncompatiblePairs(boolean generalNumericSort,
                                 boolean dictionaryOrder,
                                 boolean monthSort,
                                 boolean numericSort,
                                 boolean versionSort,
                                 boolean randomSort) {
        var sb = new StringBuilder();
        if (generalNumericSort) {
            sb.append("g");
        }
        if (dictionaryOrder) {
            sb.append("d");
        }
        if (monthSort) {
            sb.append("M");
        }
        if (numericSort) {
            sb.append("n");
        }
        if (versionSort) {
            sb.append("V");
        }
        if (randomSort) {
            sb.append("R");
        }

        if (sb.length() > 1) {
            System.err.printf("sort: options '-%s' are incompatible%n", sb);
            System.exit(2);
        }
    }
}
