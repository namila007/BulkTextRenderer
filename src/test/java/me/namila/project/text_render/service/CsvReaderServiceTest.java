package me.namila.project.text_render.service;

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

    private Path getResourcePath(String filename) {
        return Path.of(Objects.requireNonNull(
            getClass().getClassLoader().getResource(filename)
        ).getPath());
    }
}
