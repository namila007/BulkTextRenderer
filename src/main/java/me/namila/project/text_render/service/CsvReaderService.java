package me.namila.project.text_render.service;

import me.namila.project.text_render.model.CsvEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class CsvReaderService {

    private static final Logger logger = LoggerFactory.getLogger(CsvReaderService.class);
    private static final Pattern HEADER_PATTERN = Pattern.compile("^name\\s*,\\s*prefix\\s*,\\s*postfix\\s*$", Pattern.CASE_INSENSITIVE);

    /**
     * Reads all lines from a CSV file, trims whitespace, and filters out empty lines.
     * This method is kept for backward compatibility.
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

    /**
     * Reads CSV file and parses entries in format: name,prefix,postfix
     * 
     * <p>Supports both multi-column format and single-column format (backward compatible):</p>
     * <ul>
     *   <li>Multi-column: {@code Adam Smith,Mr.,Jr.}</li>
     *   <li>Single-column: {@code Adam Smith} (prefix/postfix will be empty)</li>
     * </ul>
     * 
     * <p>Header row (name,prefix,postfix) is automatically detected and skipped.</p>
     *
     * @param filePath the path to the CSV file
     * @return a list of CsvEntry objects
     * @throws IOException if an I/O error occurs reading from the file
     */
    public List<CsvEntry> readEntries(Path filePath) throws IOException {
        logger.debug("Reading CSV entries from file: {}", filePath);
        
        List<String> lines = Files.readAllLines(filePath).stream()
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .filter(line -> !isHeaderRow(line))
            .toList();
        
        List<CsvEntry> entries = lines.stream()
            .map(this::parseLine)
            .toList();
        
        logger.info("Read {} entries from CSV file: {}", entries.size(), filePath.getFileName());
        return entries;
    }

    /**
     * Checks if a line is the header row (name,prefix,postfix).
     */
    private boolean isHeaderRow(String line) {
        return HEADER_PATTERN.matcher(line).matches();
    }

    /**
     * Parses a single CSV line into a CsvEntry.
     * Handles both multi-column (name,prefix,postfix) and single-column (name only) formats.
     */
    private CsvEntry parseLine(String line) {
        String[] parts = line.split(",", -1); // -1 to preserve trailing empty strings
        
        String name = parts[0].trim();
        String prefix = parts.length > 1 ? parts[1].trim() : "";
        String postfix = parts.length > 2 ? parts[2].trim() : "";
        
        logger.debug("Parsed CSV entry: name='{}', prefix='{}', postfix='{}'", name, prefix, postfix);
        return new CsvEntry(name, prefix, postfix);
    }
}
