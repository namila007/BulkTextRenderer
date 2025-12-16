package me.namila.project.text_render.service;

import me.namila.project.text_render.model.Alignment;
import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.model.TextConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class JpegRendererServiceTest {

    private static Path templateJpeg;
    private static final int TEMPLATE_WIDTH = 800;
    private static final int TEMPLATE_HEIGHT = 600;
    private final JpegRendererService jpegRendererService = new JpegRendererService();

    @TempDir
    Path tempDir;

    @BeforeAll
    static void setUpTemplate(@TempDir Path sharedTempDir) throws Exception {
        templateJpeg = sharedTempDir.resolve("template.jpg");
        createBlankJpeg(templateJpeg, TEMPLATE_WIDTH, TEMPLATE_HEIGHT);
    }

    private static void createBlankJpeg(Path path, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        ImageIO.write(image, "JPEG", path.toFile());
    }

    @Test
    void shouldCreateOutputJpegFile() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("output.jpg");
        TextConfig config = new TextConfig(100, 300, Alignment.LEFT);
        RenderJob job = new RenderJob("Test Text", config, templateJpeg, outputPath);

        // When
        jpegRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThat(Files.size(outputPath)).isGreaterThan(0);
    }

    @Test
    void shouldInsertTextAtLeftAlignment() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("left-aligned.jpg");
        TextConfig config = new TextConfig(100, 300, Alignment.LEFT);
        RenderJob job = new RenderJob("Left Aligned Text", config, templateJpeg, outputPath);

        // When
        jpegRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatJpegIsValid(outputPath);
    }

    @Test
    void shouldInsertTextAtCenterAlignment() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("center-aligned.jpg");
        TextConfig config = new TextConfig(400, 300, Alignment.CENTER);
        RenderJob job = new RenderJob("Center Aligned Text", config, templateJpeg, outputPath);

        // When
        jpegRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatJpegIsValid(outputPath);
    }

    @Test
    void shouldInsertTextAtRightAlignment() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("right-aligned.jpg");
        TextConfig config = new TextConfig(700, 300, Alignment.RIGHT);
        RenderJob job = new RenderJob("Right Aligned Text", config, templateJpeg, outputPath);

        // When
        jpegRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatJpegIsValid(outputPath);
    }

    @Test
    void shouldUseSpecifiedFont() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("custom-font.jpg");
        TextConfig config = new TextConfig(100, 300, Alignment.LEFT, "SansSerif", 24.0f);
        RenderJob job = new RenderJob("Custom Font Text", config, templateJpeg, outputPath);

        // When
        jpegRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatJpegIsValid(outputPath);
    }

    @Test
    void shouldPreserveTemplateImageQuality() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("preserved-quality.jpg");
        TextConfig config = new TextConfig(100, 300, Alignment.LEFT);
        RenderJob job = new RenderJob("Quality Test", config, templateJpeg, outputPath);

        // When
        jpegRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        BufferedImage outputImage = ImageIO.read(outputPath.toFile());
        assertThat(outputImage.getWidth()).isEqualTo(TEMPLATE_WIDTH);
        assertThat(outputImage.getHeight()).isEqualTo(TEMPLATE_HEIGHT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"output.jpg", "output.jpeg"})
    void shouldSupportBothJpgAndJpegExtensions(String filename) throws Exception {
        // Given
        Path outputPath = tempDir.resolve(filename);
        TextConfig config = new TextConfig(100, 300, Alignment.LEFT);
        RenderJob job = new RenderJob("Extension Test", config, templateJpeg, outputPath);

        // When
        jpegRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatJpegIsValid(outputPath);
    }

    @Test
    void shouldWriteJpegToJpegExtensionFile() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("test.jpeg");
        TextConfig config = new TextConfig(100, 300, Alignment.LEFT);
        RenderJob job = new RenderJob("JPEG Extension", config, templateJpeg, outputPath);

        // When
        jpegRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        // Verify it's a valid JPEG by reading it
        BufferedImage image = ImageIO.read(outputPath.toFile());
        assertThat(image).isNotNull();
    }

    private void assertThatJpegIsValid(Path jpegPath) throws Exception {
        BufferedImage image = ImageIO.read(jpegPath.toFile());
        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(TEMPLATE_WIDTH);
        assertThat(image.getHeight()).isEqualTo(TEMPLATE_HEIGHT);
    }
}
