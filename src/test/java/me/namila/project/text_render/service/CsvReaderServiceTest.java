package me.namila.project.text_render.service;

import me.namila.project.text_render.model.CsvEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class CsvReaderServiceTest {

    private CsvReaderService csvReaderService;

    @BeforeEach
    void setUp() {
        csvReaderService = new CsvReaderService();
    }

    // --- Legacy readLines tests (backward compatibility) ---

    @Test
    void shouldReadSingleLineFromCsv() throws IOException {
        Path testFile = getResourcePath("single-line.csv");

        List<String> result = csvReaderService.readLines(testFile);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo("John Doe");
    }

    @Test
    void shouldReadMultipleLinesFromCsv() throws IOException {
        Path testFile = getResourcePath("multi-line.csv");

        List<String> result = csvReaderService.readLines(testFile);

        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("Alice Smith", "Bob Johnson", "Charlie Brown");
    }

    @Test
    void shouldHandleEmptyFile() throws IOException {
        Path testFile = getResourcePath("empty.csv");

        List<String> result = csvReaderService.readLines(testFile);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldTrimWhitespaceFromLines() throws IOException {
        Path testFile = getResourcePath("whitespace.csv");

        List<String> result = csvReaderService.readLines(testFile);

        assertThat(result).allSatisfy(line -> {
            assertThat(line).doesNotStartWith(" ");
            assertThat(line).doesNotEndWith(" ");
        });
        assertThat(result).contains("Trimmed Name", "Another Name");
    }

    @Test
    void shouldSkipEmptyLines() throws IOException {
        Path testFile = getResourcePath("whitespace.csv");

        List<String> result = csvReaderService.readLines(testFile);

        assertThat(result).allSatisfy(line -> assertThat(line).isNotEmpty());
    }

    // --- New readEntries tests (multi-column CSV) ---

    @Test
    void shouldParseMultiColumnCsv() throws IOException {
        Path testFile = getResourcePath("multi-column.csv");

        List<CsvEntry> result = csvReaderService.readEntries(testFile);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).name()).isEqualTo("Adam Smith");
        assertThat(result.get(0).prefix()).isEqualTo("Mr.");
        assertThat(result.get(0).postfix()).isEmpty();
    }

    @Test
    void shouldParseMultiColumnCsvWithAllFields() throws IOException {
        Path testFile = getResourcePath("multi-column.csv");

        List<CsvEntry> result = csvReaderService.readEntries(testFile);

        // Second entry: Jane Doe,Dr.,PhD
        CsvEntry entry = result.get(1);
        assertThat(entry.name()).isEqualTo("Jane Doe");
        assertThat(entry.prefix()).isEqualTo("Dr.");
        assertThat(entry.postfix()).isEqualTo("PhD");
        assertThat(entry.getDisplayText()).isEqualTo("Dr. Jane Doe PhD");
    }

    @Test
    void shouldParseMultiColumnCsvWithPostfixOnly() throws IOException {
        Path testFile = getResourcePath("multi-column.csv");

        List<CsvEntry> result = csvReaderService.readEntries(testFile);

        // Third entry: John Williams,,Jr.
        CsvEntry entry = result.get(2);
        assertThat(entry.name()).isEqualTo("John Williams");
        assertThat(entry.prefix()).isEmpty();
        assertThat(entry.postfix()).isEqualTo("Jr.");
        assertThat(entry.getDisplayText()).isEqualTo("John Williams Jr.");
    }

    @Test
    void shouldParseSingleColumnCsvForBackwardCompatibility() throws IOException {
        Path testFile = getResourcePath("single-line.csv");

        List<CsvEntry> result = csvReaderService.readEntries(testFile);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("John Doe");
        assertThat(result.get(0).prefix()).isEmpty();
        assertThat(result.get(0).postfix()).isEmpty();
        assertThat(result.get(0).getDisplayText()).isEqualTo("John Doe");
    }

    @Test
    void shouldSkipHeaderRowInMultiColumnCsv() throws IOException {
        Path testFile = getResourcePath("multi-column-with-header.csv");

        List<CsvEntry> result = csvReaderService.readEntries(testFile);

        // Should skip "name,prefix,postfix" header
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Adam Smith");
    }

    @Test
    void shouldHandleEmptyPrefixPostfixColumns() throws IOException {
        Path testFile = getResourcePath("multi-column.csv");

        List<CsvEntry> result = csvReaderService.readEntries(testFile);

        // First entry has empty postfix: Adam Smith,Mr.,
        assertThat(result.get(0).postfix()).isEmpty();
        assertThat(result.get(0).getDisplayText()).isEqualTo("Mr. Adam Smith");
    }

    @Test
    void shouldTrimWhitespaceInMultiColumnCsv() throws IOException {
        Path testFile = getResourcePath("multi-column-whitespace.csv");

        List<CsvEntry> result = csvReaderService.readEntries(testFile);

        assertThat(result.get(0).name()).isEqualTo("Adam Smith");
        assertThat(result.get(0).prefix()).isEqualTo("Mr.");
    }

    @Test
    void shouldHandleEmptyMultiColumnCsv() throws IOException {
        Path testFile = getResourcePath("empty.csv");

        List<CsvEntry> result = csvReaderService.readEntries(testFile);

        assertThat(result).isEmpty();
    }

    private Path getResourcePath(String filename) {
        return Path.of(Objects.requireNonNull(
            getClass().getClassLoader().getResource(filename)
        ).getPath());
    }
}
