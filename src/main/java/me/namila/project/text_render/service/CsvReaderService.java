package me.namila.project.text_render.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvReaderService {

    /**
     * Reads all lines from a CSV file, trims whitespace, and filters out empty lines.
     *
     * @param filePath the path to the CSV file
     * @return a list of trimmed, non-empty lines
     * @throws IOException if an I/O error occurs reading from the file
     */
    public List<String> readLines(Path filePath) throws IOException {
        return Files.readAllLines(filePath).stream()
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .toList();
    }
}
