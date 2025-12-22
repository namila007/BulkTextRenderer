package me.namila.project.text_render.service;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import me.namila.project.text_render.model.Alignment;
import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.model.TextConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PdfRendererServiceTest {

    private Path templatePdf;
    private final FontService fontService = new FontService();
    private final PdfRendererService pdfRendererService = new PdfRendererService(fontService);

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUpTemplate() throws Exception {
        // Create template in same tempDir as output to avoid Windows cross-volume issues
        templatePdf = tempDir.resolve("template.pdf");
        createBlankPdf(templatePdf);
    }

    private static void createBlankPdf(Path path) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(path.toFile()));
        document.open();
        document.add(new com.lowagie.text.Paragraph(" ")); // Add minimal content
        document.close();
    }

    @Test
    void shouldCreateOutputPdfFile() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("output.pdf");
        TextConfig config = new TextConfig(100, 700, Alignment.LEFT);
        RenderJob job = new RenderJob("Test Text", config, templatePdf, outputPath);

        // When
        pdfRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThat(Files.size(outputPath)).isGreaterThan(0);
    }

    @Test
    void shouldInsertTextAtLeftAlignment() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("left-aligned.pdf");
        TextConfig config = new TextConfig(100, 700, Alignment.LEFT);
        RenderJob job = new RenderJob("Left Aligned Text", config, templatePdf, outputPath);

        // When
        pdfRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatPdfIsValid(outputPath);
    }

    @Test
    void shouldInsertTextAtCenterAlignment() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("center-aligned.pdf");
        TextConfig config = new TextConfig(297.5f, 700, Alignment.CENTER); // Center of A4 width
        RenderJob job = new RenderJob("Center Aligned Text", config, templatePdf, outputPath);

        // When
        pdfRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatPdfIsValid(outputPath);
    }

    @Test
    void shouldInsertTextAtRightAlignment() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("right-aligned.pdf");
        TextConfig config = new TextConfig(500, 700, Alignment.RIGHT);
        RenderJob job = new RenderJob("Right Aligned Text", config, templatePdf, outputPath);

        // When
        pdfRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatPdfIsValid(outputPath);
    }

    @Test
    void shouldUseSpecifiedFont() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("custom-font.pdf");
        TextConfig config = new TextConfig(100, 700, Alignment.LEFT, "Helvetica", 16.0f);
        RenderJob job = new RenderJob("Custom Font Text", config, templatePdf, outputPath);

        // When
        pdfRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatPdfIsValid(outputPath);
    }

    @Test
    void shouldUseDefaultFontWhenNotSpecified() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("default-font.pdf");
        TextConfig config = new TextConfig(100, 700, Alignment.LEFT); // Uses default font
        RenderJob job = new RenderJob("Default Font Text", config, templatePdf, outputPath);

        // When
        pdfRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatPdfIsValid(outputPath);
    }

    @Test
    void shouldHandleOutputPathWithNoParent() throws Exception {
        // Given - output path in current directory (no parent)
        Path outputPath = Path.of("test-output.pdf");
        TextConfig config = new TextConfig(100, 700, Alignment.LEFT);
        RenderJob job = new RenderJob("Test", config, templatePdf, outputPath);

        try {
            // When
            pdfRendererService.render(job);

            // Then - should handle null parent gracefully
            assertThat(outputPath).exists();
        } finally {
            // Cleanup
            Files.deleteIfExists(outputPath);
        }
    }

    @Test
    void shouldCreateNestedOutputDirectories() throws Exception {
        // Given - output in deeply nested directory
        Path nestedOutput = tempDir.resolve("level1/level2/level3/output.pdf");
        TextConfig config = new TextConfig(100, 700, Alignment.LEFT);
        RenderJob job = new RenderJob("Nested Test", config, templatePdf, nestedOutput);

        // When
        pdfRendererService.render(job);

        // Then - should create all parent directories
        assertThat(nestedOutput).exists();
        assertThat(nestedOutput.getParent()).exists();
        assertThatPdfIsValid(nestedOutput);
    }

    private void assertThatPdfIsValid(Path pdfPath) throws Exception {
        PdfReader reader = new PdfReader(pdfPath.toString());
        assertThat(reader.getNumberOfPages()).isGreaterThan(0);
        reader.close();
    }
}
