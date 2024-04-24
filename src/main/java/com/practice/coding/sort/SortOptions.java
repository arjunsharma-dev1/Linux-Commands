package com.practice.coding.sort;

public interface SortOptions {
    SortOption reverse = SortOption.of("-r", "--reverse");
    SortOption unique = SortOption.of("-u", "--unique");
}
