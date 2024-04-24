package com.practice.coding.sort;


public interface  SortOptions {
    SortOption reverse = SortOption.of("-r", "--reverse");
    SortOption unique = SortOption.of("-u", "--unique");
    SortOption ignoreLeadingBlanks = SortOption.of("-b", "--ignore-leading-blanks");
    SortOption ignoreCase = SortOption.of("-f", "--ignore-case");
    SortOption  dictionaryCase = SortOption.of("-d", "--dictionary-order");
    SortOption  generalNumericSort = SortOption.of("-g", "--general-numeric-sort");
    SortOption  monthSort = SortOption.of("-M", "--month-sort");
    SortOption  numericSort = SortOption.of("-n", "--numeric-sort");
    SortOption  humanNumericSort= SortOption.of("-h", "--human-numeric-sort");
    SortOption  versionSort = SortOption.of("-V", "--version-sort");
    SortOption  randomSort= SortOption.of("-R", "--random-sort");
}
