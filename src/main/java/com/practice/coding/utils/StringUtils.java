package com.practice.coding.utils;

import java.util.regex.Pattern;

public interface StringUtils {

    Pattern numberPattern = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+");
    static float getNumericPrefixValue(String content) {
        var matcher = numberPattern.matcher(content);
        if (matcher.find()) {
//            TODO: NumberFormatException
            return Float.parseFloat(matcher.group(0));
        }
        return 0f;
    }

    Pattern numberGeneralPattern = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");

    static double getNumericPrefixValueForGeneral(String line) {
        var matcher = numberGeneralPattern.matcher(line);
        if (matcher.find()) {
//            TODO: NumberFormatException
            return Double.parseDouble(matcher.group(0));
        }
        return 0d;
    }
}
