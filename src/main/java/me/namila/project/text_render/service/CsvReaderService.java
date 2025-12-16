package me.namila.project.text_render.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvReaderService {

    private static final Logger logger = LoggerFactory.getLogger(CsvReaderService.class);

    /**
     * Reads all lines from a CSV file, trims whitespace, and filters out empty lines.
     *
     * @param filePath the path to the CSV file
     * @return a list of trimmed, non-empty lines
     * @throws IOException if an I/O error occurs reading from the file
     */
    public List<String> readLines(Path filePath) throws IOException {
        logger.debug("Reading CSV file: {}", filePath);
        List<String> lines = Files.readAllLines(filePath).stream()
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .toList();
        logger.info("Read {} entries from CSV file: {}", lines.size(), filePath.getFileName());
        return lines;
    }
}
