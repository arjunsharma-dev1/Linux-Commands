package com.practice.coding.sort;

public class SortOption {
    private String shortOption;
    private String longOption;

    public SortOption setLongOption(String longOption) {
        this.longOption = longOption;
        return this;
    }

    public String getLongOption() {
        return longOption;
    }

    public SortOption setShortOption(String shortOption) {
        this.shortOption = shortOption;
        return this;
    }

    public String getShortOption() {
        return shortOption;
    }

    public static SortOption of(String shortOption, String longOption) {
        return new SortOption()
                .setShortOption(shortOption)
                .setLongOption(longOption);
    }
}
