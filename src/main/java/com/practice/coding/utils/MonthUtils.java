package com.practice.coding.utils;

import java.util.Map;
import java.util.Objects;

public interface MonthUtils {

    Map<String, Integer> monthShortMap = Map.ofEntries(
            Map.entry("jan", 1),
            Map.entry("feb", 2),
            Map.entry("mar", 3),
            Map.entry("apr", 4),
            Map.entry("may", 5),
            Map.entry("jun", 6),
            Map.entry("jul", 7),
            Map.entry("aug", 8),
            Map.entry("sep", 9),
            Map.entry("oct", 10),
            Map.entry("nov", 11),
            Map.entry("dec", 12)
    );

    Map<String, String> monthFullMap = Map.ofEntries(
            Map.entry("jan","january"),
            Map.entry("feb","february"),
            Map.entry("mar","march"),
            Map.entry("apr","april"),
            Map.entry("may","may"),
            Map.entry("jun","june"),
            Map.entry("jul","july"),
            Map.entry("aug","august"),
            Map.entry("sep","september"),
            Map.entry("oct","october"),
            Map.entry("nov","november"),
            Map.entry("dec","december")
    );

    static Integer getOrder(String month) {
        if (Objects.isNull(month) || month.length() < 3) {
            return 0;
        }
        var monthLowerCase = month.toLowerCase();
        var monthShort = monthLowerCase.substring(0,3);
        var monthOrder = monthShortMap.getOrDefault(monthShort, 0);
        if (monthOrder == 0) {
            return 0;
        }

        var monthFullForm = monthFullMap.get(monthShort);
        if (monthLowerCase.length() > monthFullForm.length()
                || !monthFullForm.contains(monthLowerCase)) {
            return 0;
        }
        return monthOrder;
    }
}
