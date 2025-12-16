package me.namila.project.text_render.integration;

import me.namila.project.text_render.cli.RenderCommand;
import me.namila.project.text_render.config.AppConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for BulkTextRenderer.
 * Tests the complete workflow from CLI input to rendered output files.
 */
class EndToEndTest {

    private static final Path TEMPLATE_PDF = Path.of("src/test/resources/template.pdf");
    private static final Path TEMPLATE_PNG = Path.of("src/test/resources/template.png");
    private static final Path TEMPLATE_JPEG = Path.of("src/test/resources/template.jpg");
    private static final Path SAMPLE_CSV = Path.of("src/test/resources/sample.csv");
    private static final Path SINGLE_LINE_CSV = Path.of("src/test/resources/single-line.csv");

    @TempDir
    Path tempDir;

    private AnnotationConfigApplicationContext context;
    private CommandLine cmd;
    private StringWriter stdout;
    private StringWriter stderr;

    @BeforeAll
    static void setUpTemplates() throws Exception {
        // Generate test templates if they don't exist
        TestResourceGenerator.ensureTemplatePdfExists(TEMPLATE_PDF);
        TestResourceGenerator.ensureTemplatePngExists(TEMPLATE_PNG);
        TestResourceGenerator.ensureTemplateJpegExists(TEMPLATE_JPEG);
    }

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        RenderCommand renderCommand = context.getBean(RenderCommand.class);
        cmd = new CommandLine(renderCommand);
        
        stdout = new StringWriter();
        stderr = new StringWriter();
        cmd.setOut(new PrintWriter(stdout));
        cmd.setErr(new PrintWriter(stderr));
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    @DisplayName("Should generate PDFs from CSV using PDF template")
    void shouldGeneratePdfsFromCsvUsingPdfTemplate() {
        // Given
        Path outputDir = tempDir.resolve("pdf-output");

        // When
        int exitCode = cmd.execute(
            "-t", TEMPLATE_PDF.toString(),
            "-c", SAMPLE_CSV.toString(),
            "-o", outputDir.toString(),
            "--x", "100",
            "--y", "500"
        );

        // Then
        assertThat(exitCode).isZero();
        assertThat(outputDir).exists();
        
        File[] files = outputDir.toFile().listFiles((dir, name) -> name.endsWith(".pdf"));
        assertThat(files).isNotNull();
        assertThat(files.length).isEqualTo(10); // 10 names in sample.csv
    }

    @Test
    @DisplayName("Should generate PNGs from CSV using PNG template")
    void shouldGeneratePngsFromCsvUsingPngTemplate() {
        // Given
        Path outputDir = tempDir.resolve("png-output");

        // When
        int exitCode = cmd.execute(
            "-t", TEMPLATE_PNG.toString(),
            "-c", SAMPLE_CSV.toString(),
            "-o", outputDir.toString(),
            "--x", "100",
            "--y", "100"
        );

        // Then
        assertThat(exitCode).isZero();
        assertThat(outputDir).exists();
        
        File[] files = outputDir.toFile().listFiles((dir, name) -> name.endsWith(".png"));
        assertThat(files).isNotNull();
        assertThat(files.length).isEqualTo(10); // 10 names in sample.csv
    }

    @Test
    @DisplayName("Should generate JPEGs from CSV using JPEG template")
    void shouldGenerateJpegsFromCsvUsingJpegTemplate() {
        // Given
        Path outputDir = tempDir.resolve("jpeg-output");

        // When
        int exitCode = cmd.execute(
            "-t", TEMPLATE_JPEG.toString(),
            "-c", SAMPLE_CSV.toString(),
            "-o", outputDir.toString(),
            "--x", "100",
            "--y", "100"
        );

        // Then
        assertThat(exitCode).isZero();
        assertThat(outputDir).exists();
        
        File[] files = outputDir.toFile().listFiles((dir, name) -> name.endsWith(".jpg"));
        assertThat(files).isNotNull();
        assertThat(files.length).isEqualTo(10); // 10 names in sample.csv
    }

    @Test
    @DisplayName("Should create output directory if it doesn't exist")
    void shouldCreateOutputDirectoryIfNotExists() {
        // Given
        Path outputDir = tempDir.resolve("new/nested/output");
        assertThat(outputDir).doesNotExist();

        // When
        int exitCode = cmd.execute(
            "-t", TEMPLATE_PDF.toString(),
            "-c", SINGLE_LINE_CSV.toString(),
            "-o", outputDir.toString(),
            "--x", "100",
            "--y", "500"
        );

        // Then
        assertThat(exitCode).isZero();
        assertThat(outputDir).exists();
    }

    @Test
    @DisplayName("Should generate files with custom prefix and postfix")
    void shouldGenerateFilesWithCustomPrefixPostfix() {
        // Given
        Path outputDir = tempDir.resolve("prefix-output");

        // When
        int exitCode = cmd.execute(
            "-t", TEMPLATE_PDF.toString(),
            "-c", SINGLE_LINE_CSV.toString(),
            "-o", outputDir.toString(),
            "--x", "100",
            "--y", "500",
            "--prefix", "wedding",
            "--postfix", "final"
        );

        // Then
        assertThat(exitCode).isZero();
        
        File[] files = outputDir.toFile().listFiles();
        assertThat(files).isNotNull();
        assertThat(files.length).isGreaterThan(0);
        
        // Verify filename contains prefix and postfix
        String filename = files[0].getName();
        assertThat(filename).startsWith("wedding-");
        assertThat(filename).contains("-final.pdf");
    }

    @Test
    @DisplayName("Should list available fonts")
    void shouldListAvailableFonts() {
        // When
        int exitCode = cmd.execute("--list-fonts");

        // Then
        assertThat(exitCode).isZero();
        String output = stdout.toString();
        assertThat(output).contains("=== Available Fonts ===");
        assertThat(output).contains("[Built-in]");
        assertThat(output).contains("[System]");
    }

    @Test
    @DisplayName("Should apply text alignment correctly")
    void shouldApplyTextAlignmentCorrectly() {
        // Given
        Path outputDir = tempDir.resolve("aligned-output");

        // When
        int exitCode = cmd.execute(
            "-t", TEMPLATE_PDF.toString(),
            "-c", SINGLE_LINE_CSV.toString(),
            "-o", outputDir.toString(),
            "--x", "297", // Center of A4 page
            "--y", "500",
            "-a", "CENTER"
        );

        // Then
        assertThat(exitCode).isZero();
        
        File[] files = outputDir.toFile().listFiles((dir, name) -> name.endsWith(".pdf"));
        assertThat(files).isNotNull();
        assertThat(files.length).isGreaterThan(0);
        // Verify file is not empty (has content rendered)
        assertThat(files[0].length()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should apply custom font settings")
    void shouldApplyCustomFontSettings() {
        // Given
        Path outputDir = tempDir.resolve("font-output");

        // When
        int exitCode = cmd.execute(
            "-t", TEMPLATE_PDF.toString(),
            "-c", SINGLE_LINE_CSV.toString(),
            "-o", outputDir.toString(),
            "--x", "100",
            "--y", "500",
            "-f", "Helvetica",
            "-s", "24"
        );

        // Then
        assertThat(exitCode).isZero();
        
        File[] files = outputDir.toFile().listFiles((dir, name) -> name.endsWith(".pdf"));
        assertThat(files).isNotNull();
        assertThat(files.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should use specified thread count for parallel processing")
    void shouldUseSpecifiedThreadCount() {
        // Given
        Path outputDir = tempDir.resolve("parallel-output");

        // When
        int exitCode = cmd.execute(
            "-t", TEMPLATE_PDF.toString(),
            "-c", SAMPLE_CSV.toString(),
            "-o", outputDir.toString(),
            "--x", "100",
            "--y", "500",
            "-p", "2"
        );

        // Then
        assertThat(exitCode).isZero();
        
        File[] files = outputDir.toFile().listFiles((dir, name) -> name.endsWith(".pdf"));
        assertThat(files).isNotNull();
        assertThat(files.length).isEqualTo(10);
    }

    @Test
    @DisplayName("Should fail with missing template file")
    void shouldFailWithMissingTemplateFile() {
        // Given
        Path nonExistentTemplate = Path.of("nonexistent/template.pdf");
        Path outputDir = tempDir.resolve("error-output");

        // When
        int exitCode = cmd.execute(
            "-t", nonExistentTemplate.toString(),
            "-c", SAMPLE_CSV.toString(),
            "-o", outputDir.toString(),
            "--x", "100",
            "--y", "500"
        );

        // Then
        assertThat(exitCode).isNotZero();
    }

    @Test
    @DisplayName("Should fail with missing CSV file")
    void shouldFailWithMissingCsvFile() {
        // Given
        Path nonExistentCsv = Path.of("nonexistent/names.csv");
        Path outputDir = tempDir.resolve("error-output");

        // When
        int exitCode = cmd.execute(
            "-t", TEMPLATE_PDF.toString(),
            "-c", nonExistentCsv.toString(),
            "-o", outputDir.toString(),
            "--x", "100",
            "--y", "500"
        );

        // Then
        assertThat(exitCode).isNotZero();
    }

    @Test
    @DisplayName("Should fail with missing required options")
    void shouldFailWithMissingRequiredOptions() {
        // When - missing x and y coordinates
        int exitCode = cmd.execute(
            "-t", TEMPLATE_PDF.toString(),
            "-c", SAMPLE_CSV.toString()
        );

        // Then
        assertThat(exitCode).isNotZero();
        String error = stderr.toString();
        assertThat(error).contains("Missing required option");
    }

    @Test
    @DisplayName("Should display help when requested")
    void shouldDisplayHelpWhenRequested() {
        // When
        int exitCode = cmd.execute("--help");

        // Then
        assertThat(exitCode).isZero();
        String output = stdout.toString();
        assertThat(output).contains("bulk-render");
        assertThat(output).contains("--template");
        assertThat(output).contains("--csv");
    }

    @Test
    @DisplayName("Should display version when requested")
    void shouldDisplayVersionWhenRequested() {
        // When
        int exitCode = cmd.execute("--version");

        // Then
        assertThat(exitCode).isZero();
        String output = stdout.toString();
        assertThat(output).contains("1.0");
    }

    @Test
    @DisplayName("Should process PNG template with all options")
    void shouldProcessPngTemplateWithAllOptions() {
        // Given
        Path outputDir = tempDir.resolve("full-png-output");

        // When
        int exitCode = cmd.execute(
            "-t", TEMPLATE_PNG.toString(),
            "-c", SINGLE_LINE_CSV.toString(),
            "-o", outputDir.toString(),
            "--x", "100",
            "--y", "100",
            "-a", "RIGHT",
            "-f", "SansSerif",
            "-s", "18",
            "--prefix", "invite",
            "--postfix", "v1"
        );

        // Then
        assertThat(exitCode).isZero();
        
        File[] files = outputDir.toFile().listFiles((dir, name) -> name.endsWith(".png"));
        assertThat(files).isNotNull();
        assertThat(files.length).isGreaterThan(0);
        
        String filename = files[0].getName();
        assertThat(filename).startsWith("invite-");
        assertThat(filename).contains("-v1.png");
    }
}
