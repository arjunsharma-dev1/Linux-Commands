package com.practice.coding.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface StringUtils {

    Pattern simpleNumberPattern = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+");
    static float getNumericPrefixValue(String content) {
        var matcher = simpleNumberPattern.matcher(content);
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

    static String getAlphanumeric(String line) {
        return line.chars()
                .filter(isAlphanumeric)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    IntPredicate isLowercaseAlphabet = character -> {
        return character >= 'a' && character <= 'z';
    };

    IntPredicate isUppercaseAlphabet = character -> {
        return character >= 'A' && character <= 'Z';
    };

    IntPredicate isAlphabetic = character -> isLowercaseAlphabet.or(isUppercaseAlphabet).test(character);

    IntPredicate isDigit = character -> '0'<= character && '9' >= character;

    IntPredicate isAlphanumeric = character -> isAlphabetic.or(isDigit).test(character);

    enum HumanNumeric {
        B(1), K(2), KB(2), G(3), GB(3), T(4), TB(4),
        P(5), PB(5), E(6), EB(6), Z(7), ZB(7), Y(8), YB(8);

        private final int order;

        HumanNumeric(int order) {
            this.order = order;
        }

        public int getOrder() {
            return order;
        }

        public static int getOrder(String identifier) {
            if (Objects.isNull(identifier) || identifier.isBlank()) {
                return 0;
            }
            return Arrays.stream(HumanNumeric.values()).filter(entry -> entry.name().equalsIgnoreCase(identifier)).map(HumanNumeric::getOrder).findFirst().orElse(0);
        }
    }

    Pattern humanNumericPattern = Pattern.compile("^([-+]?[0-9]*\\.?[0-9]+)(B|K|KB|G|GB|T|TB|P|PB|E|EX|ZB|YB)$");
    static Pair<Float, Integer> getHumanNumericUnitOrder(String line) {
        var lineTrimmed = line.trim();
        var matcher = humanNumericPattern.matcher(lineTrimmed);
        if (matcher.find()) {
            var number = Float.parseFloat(matcher.group(1));
            var order = HumanNumeric.getOrder(matcher.group(2));
            return Pair.of(number, order);
        }
        return Pair.of(0f, 0);
    }
}
