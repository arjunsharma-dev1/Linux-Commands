package com.practice.coding.sort.comparators;

import com.practice.coding.utils.Pair;
import com.practice.coding.utils.SortUtils;

import java.util.Comparator;

public interface ComparatorUtils {
    Comparator<String> simpleOrder = String::compareTo;
    Comparator<String> reverseOrder = simpleOrder.reversed();


    Comparator<Pair<String, Object>> pairSimpleOrder = (first, second) -> {
        return simpleOrder.compare((String) first.getValue(), (String) second.getValue());
    };
    Comparator<Pair<String, Object>> pairReverseOrder = pairSimpleOrder.reversed();

    Comparator<Pair<String, Object>> monthSortOrder = (first, second) -> {
        var monthOrder = Integer.compare((Integer) first.getValue(), (Integer) second.getValue());
        if (monthOrder == 0) {
            return first.getKey().toUpperCase().compareTo(second.getKey().toUpperCase());
        }
        return monthOrder;
    };

    Comparator<Pair<String, Object>> monthSortReverseOrder = monthSortOrder.reversed();

    Comparator<Pair<String, Object>> generalNumericalSortOrder = (first, second) -> {
        var valueOrder = Double.compare((Double) first.getValue(), (Double) second.getValue());
        if (valueOrder == 0) {
            return first.getKey().compareTo(second.getKey());
        }
        return valueOrder;
    };

    Comparator<Pair<String, Object>> generalNumericalSortOrderReverse = generalNumericalSortOrder.reversed();

    Comparator<Pair<String, Object>> numericalSortOrder = (first, second) -> {
        var valueOrder = Float.compare((Float) first.getValue(), (Float) second.getValue());
        if (valueOrder == 0) {
            return first.getKey().compareTo(second.getKey());
        }
        return valueOrder;
    };

    Comparator<Pair<String, Object>> numericalSortOrderReverse = numericalSortOrder.reversed();

    Comparator<Pair<String, Object>> humanNumericalSortOrder = (first, second) -> {
        var firstPair = (Pair<Float, Integer>) first.getValue();
        var secondPair = (Pair<Float, Integer>) second.getValue();

        var humanReadableUnitOrder = Integer.compare(firstPair.getValue(), secondPair.getValue());
        if (humanReadableUnitOrder == 0) {
            var magnitude = Float.compare(firstPair.getValue(),secondPair.getValue());
//            TODO: Need to revisit this
            if (magnitude == 0) {
                return first.getKey().compareTo(second.getKey());
            }
            return magnitude;
        }
        return humanReadableUnitOrder;
    };

    Comparator<Pair<String, Object>> humanNumericalSortReverseOrder = generalNumericalSortOrder.reversed();


    Comparator<Pair<String, Object>> versionSortOrder = (first, second) -> {
        var firstValue = (String) first.getValue();
        var secondValue = (String) second.getValue();
        var isFirstAlphabetic = firstValue.chars().anyMatch(SortUtils.isAlphabetic);
        var isSecondAlphabetic = secondValue.chars().anyMatch(SortUtils.isAlphabetic);

        if (isFirstAlphabetic && isSecondAlphabetic) {
            return firstValue.compareTo(secondValue);
        } else if (isFirstAlphabetic) {
            return 1;
        } else if (isSecondAlphabetic) {
            return -1;
        } else {
            return firstValue.compareTo(secondValue);
        }
    };

    Comparator<Pair<String, Object>> versionSortReverseOrder = versionSortOrder.reversed();


    Comparator<Pair<String, Object>> randomSortOrder = (first, second) -> {
        long firstOrder = (long) first.getValue();
        long secondOrder = (long) second.getValue();
        return Long.compare(firstOrder, secondOrder);
    };
    Comparator<Pair<String, Object>> randomSortReverseOrder = randomSortOrder.reversed();


    static Comparator<Pair<String, Object>> getPairComparator(boolean reverse,
                                                              boolean generalNumericSort,
                                                              boolean numericSort,
                                                              boolean monthSort,
                                                              boolean humanNumericSort,
                                                              boolean versionSort,
                                                              boolean randomSort) {
        Comparator<Pair<String, Object>> pairComparatorToUse = ComparatorUtils.pairSimpleOrder;
        if (generalNumericSort) {
            pairComparatorToUse = ComparatorUtils.generalNumericalSortOrder;
        } else if (numericSort) {
            pairComparatorToUse = ComparatorUtils.numericalSortOrder;
        } else if (monthSort) {
            pairComparatorToUse = ComparatorUtils.monthSortOrder;
        } else if (humanNumericSort) {
            pairComparatorToUse = ComparatorUtils.humanNumericalSortOrder;
        } else if (versionSort) {
            pairComparatorToUse = ComparatorUtils.versionSortOrder;
        } else if (randomSort) {
            pairComparatorToUse = ComparatorUtils.randomSortOrder;
        }
        if (reverse) {
            pairComparatorToUse = ComparatorUtils.pairReverseOrder;
            if (generalNumericSort) {
                pairComparatorToUse = ComparatorUtils.generalNumericalSortOrderReverse;
            } else if (numericSort) {
                pairComparatorToUse = ComparatorUtils.numericalSortOrderReverse;
            } else if (monthSort) {
                pairComparatorToUse = ComparatorUtils.monthSortReverseOrder;
            } else if (humanNumericSort) {
                pairComparatorToUse = ComparatorUtils.humanNumericalSortReverseOrder;
            } else if (versionSort) {
                pairComparatorToUse = ComparatorUtils.versionSortReverseOrder;
            } else if (randomSort) {
                pairComparatorToUse = ComparatorUtils.randomSortReverseOrder;
            }
        }
        return pairComparatorToUse;
    }
}
