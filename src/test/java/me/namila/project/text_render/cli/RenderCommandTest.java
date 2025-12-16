package me.namila.project.text_render.cli;

import me.namila.project.text_render.model.Alignment;
import me.namila.project.text_render.service.CsvReaderService;
import me.namila.project.text_render.service.FontService;
import me.namila.project.text_render.service.ParallelExecutorService;
import me.namila.project.text_render.service.PdfRendererService;
import me.namila.project.text_render.service.PngRendererService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RenderCommandTest {

    private RenderCommand command;
    private CommandLine commandLine;
    private StringWriter stdout;
    private StringWriter stderr;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        command = new RenderCommand(
            new CsvReaderService(),
            new PdfRendererService(),
            new PngRendererService(),
            new ParallelExecutorService(),
            new FontService()
        );
        commandLine = new CommandLine(command);
        stdout = new StringWriter();
        stderr = new StringWriter();
        commandLine.setOut(new PrintWriter(stdout));
        commandLine.setErr(new PrintWriter(stderr));
    }

    @Test
    void shouldParseAllRequiredArguments() throws IOException {
        // Given
        Path templateFile = createTempFile("template.pdf", "dummy pdf content");
        Path csvFile = createTempFile("names.csv", "John Doe\nJane Smith");

        // When
        int exitCode = commandLine.execute(
            "-t", templateFile.toString(),
            "-c", csvFile.toString(),
            "-o", tempDir.resolve("output").toString(),
            "--x", "100",
            "--y", "200",
            "-a", "CENTER",
            "-f", "Helvetica",
            "-s", "14",
            "-p", "4"
        );

        // Then - should parse without error (file validation happens in call())
        assertThat(command.getTemplatePath()).isEqualTo(templateFile);
        assertThat(command.getCsvPath()).isEqualTo(csvFile);
        assertThat(command.getOutputFolder().toString()).isEqualTo(tempDir.resolve("output").toString());
        assertThat(command.getX()).isEqualTo(100f);
        assertThat(command.getY()).isEqualTo(200f);
        assertThat(command.getAlignment()).isEqualTo(Alignment.CENTER);
        assertThat(command.getFontName()).isEqualTo("Helvetica");
        assertThat(command.getFontSize()).isEqualTo(14f);
        assertThat(command.getParallelism()).isEqualTo(4);
    }

    @Test
    void shouldUseDefaultOutputFolder() throws IOException {
        // Given
        Path templateFile = createTempFile("template.pdf", "dummy");
        Path csvFile = createTempFile("names.csv", "Test");

        // When - no output folder specified
        commandLine.execute(
            "-t", templateFile.toString(),
            "-c", csvFile.toString(),
            "--x", "100",
            "--y", "200"
        );

        // Then
        assertThat(command.getOutputFolder().toString()).isEqualTo("./output");
    }

    @Test
    void shouldUseDefaultParallelism() throws IOException {
        // Given
        Path templateFile = createTempFile("template.pdf", "dummy");
        Path csvFile = createTempFile("names.csv", "Test");

        // When - no parallelism specified
        commandLine.execute(
            "-t", templateFile.toString(),
            "-c", csvFile.toString(),
            "--x", "100",
            "--y", "200"
        );

        // Then - should use available processors
        assertThat(command.getParallelism()).isEqualTo(Runtime.getRuntime().availableProcessors());
    }

    @Test
    void shouldValidateTemplateFileExists() throws IOException {
        // Given - non-existent template
        Path csvFile = createTempFile("names.csv", "Test");

        // When
        int exitCode = commandLine.execute(
            "-t", tempDir.resolve("nonexistent.pdf").toString(),
            "-c", csvFile.toString(),
            "--x", "100",
            "--y", "200"
        );

        // Then - should fail with error
        assertThat(exitCode).isNotEqualTo(0);
        assertThat(stderr.toString()).contains("Template file does not exist");
    }

    @Test
    void shouldValidateCsvFileExists() throws IOException {
        // Given - non-existent CSV
        Path templateFile = createTempFile("template.pdf", "dummy");

        // When
        int exitCode = commandLine.execute(
            "-t", templateFile.toString(),
            "-c", tempDir.resolve("nonexistent.csv").toString(),
            "--x", "100",
            "--y", "200"
        );

        // Then - should fail with error
        assertThat(exitCode).isNotEqualTo(0);
        assertThat(stderr.toString()).contains("CSV file does not exist");
    }

    @Test
    void shouldShowHelpWithMixinOption() {
        // When
        int exitCode = commandLine.execute("--help");

        // Then
        assertThat(exitCode).isEqualTo(0);
        assertThat(stdout.toString()).contains("--template");
        assertThat(stdout.toString()).contains("--csv");
        assertThat(stdout.toString()).contains("--output");
    }

    @Test
    void shouldShowVersion() {
        // When
        int exitCode = commandLine.execute("--version");

        // Then
        assertThat(exitCode).isEqualTo(0);
        assertThat(stdout.toString()).contains("1.0");
    }

    @Test
    void shouldUseDefaultAlignment() throws IOException {
        // Given
        Path templateFile = createTempFile("template.pdf", "dummy");
        Path csvFile = createTempFile("names.csv", "Test");

        // When - no alignment specified
        commandLine.execute(
            "-t", templateFile.toString(),
            "-c", csvFile.toString(),
            "--x", "100",
            "--y", "200"
        );

        // Then
        assertThat(command.getAlignment()).isEqualTo(Alignment.LEFT);
    }

    @Test
    void shouldUseDefaultFont() throws IOException {
        // Given
        Path templateFile = createTempFile("template.pdf", "dummy");
        Path csvFile = createTempFile("names.csv", "Test");

        // When - no font specified
        commandLine.execute(
            "-t", templateFile.toString(),
            "-c", csvFile.toString(),
            "--x", "100",
            "--y", "200"
        );

        // Then
        assertThat(command.getFontName()).isEqualTo("Times New Roman");
        assertThat(command.getFontSize()).isEqualTo(12f);
    }

    @Test
    void shouldFailWithoutRequiredArguments() {
        // When - missing template
        int exitCode = commandLine.execute(
            "-c", "some.csv",
            "--x", "100",
            "--y", "200"
        );

        // Then
        assertThat(exitCode).isNotEqualTo(0);
    }

    @Test
    void shouldAcceptPrefixOption() throws IOException {
        // Given
        Path templateFile = createTempFile("template.pdf", "dummy");
        Path csvFile = createTempFile("names.csv", "Test");

        // When
        commandLine.execute(
            "-t", templateFile.toString(),
            "-c", csvFile.toString(),
            "--x", "100",
            "--y", "200",
            "--prefix", "wedding"
        );

        // Then
        assertThat(command.getPrefix()).isEqualTo("wedding");
    }

    @Test
    void shouldAcceptPostfixOption() throws IOException {
        // Given
        Path templateFile = createTempFile("template.pdf", "dummy");
        Path csvFile = createTempFile("names.csv", "Test");

        // When
        commandLine.execute(
            "-t", templateFile.toString(),
            "-c", csvFile.toString(),
            "--x", "100",
            "--y", "200",
            "--postfix", "final"
        );

        // Then
        assertThat(command.getPostfix()).isEqualTo("final");
    }

    @Test
    void shouldAcceptBothPrefixAndPostfix() throws IOException {
        // Given
        Path templateFile = createTempFile("template.pdf", "dummy");
        Path csvFile = createTempFile("names.csv", "Test");

        // When
        commandLine.execute(
            "-t", templateFile.toString(),
            "-c", csvFile.toString(),
            "--x", "100",
            "--y", "200",
            "--prefix", "batch1",
            "--postfix", "v2"
        );

        // Then
        assertThat(command.getPrefix()).isEqualTo("batch1");
        assertThat(command.getPostfix()).isEqualTo("v2");
    }

    @Test
    void shouldListFontsWhenOptionProvided() {
        // When
        int exitCode = commandLine.execute("--list-fonts");

        // Then
        assertThat(exitCode).isEqualTo(0);
        assertThat(stdout.toString()).contains("=== Available Fonts ===");
        assertThat(stdout.toString()).contains("[Built-in]");
        assertThat(stdout.toString()).contains("[System]");
    }

    @Test
    void shouldHaveNullPrefixAndPostfixByDefault() throws IOException {
        // Given
        Path templateFile = createTempFile("template.pdf", "dummy");
        Path csvFile = createTempFile("names.csv", "Test");

        // When - no prefix/postfix specified
        commandLine.execute(
            "-t", templateFile.toString(),
            "-c", csvFile.toString(),
            "--x", "100",
            "--y", "200"
        );

        // Then
        assertThat(command.getPrefix()).isNull();
        assertThat(command.getPostfix()).isNull();
    }

    private Path createTempFile(String name, String content) throws IOException {
        Path file = tempDir.resolve(name);
        Files.writeString(file, content);
        return file;
    }
}
