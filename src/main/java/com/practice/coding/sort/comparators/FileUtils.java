package com.practice.coding.sort.comparators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public interface FileUtils {
    static Stream<String> getLineStream(List<String> files) {
        Objects.requireNonNull(files);
        return files.stream()
                .filter(Objects::nonNull)
                .map(Paths::get)
                .flatMap(FileUtils::readLines);
    }

    static Stream<String> readLines(Path filePath) {
        Objects.requireNonNull(filePath);
        try {
            return Files.readAllLines(filePath).stream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
