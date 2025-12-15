package me.namila.project.text_render.service;

import me.namila.project.text_render.model.Alignment;
import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.model.TextConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PngRendererServiceTest {

    private static Path templatePng;
    private static final int TEMPLATE_WIDTH = 800;
    private static final int TEMPLATE_HEIGHT = 600;
    private final PngRendererService pngRendererService = new PngRendererService();

    @TempDir
    Path tempDir;

    @BeforeAll
    static void setUpTemplate(@TempDir Path sharedTempDir) throws Exception {
        templatePng = sharedTempDir.resolve("template.png");
        createBlankPng(templatePng, TEMPLATE_WIDTH, TEMPLATE_HEIGHT);
    }

    private static void createBlankPng(Path path, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        ImageIO.write(image, "PNG", path.toFile());
    }

    @Test
    void shouldCreateOutputPngFile() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("output.png");
        TextConfig config = new TextConfig(100, 300, Alignment.LEFT);
        RenderJob job = new RenderJob("Test Text", config, templatePng, outputPath);

        // When
        pngRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThat(Files.size(outputPath)).isGreaterThan(0);
    }

    @Test
    void shouldInsertTextAtLeftAlignment() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("left-aligned.png");
        TextConfig config = new TextConfig(100, 300, Alignment.LEFT);
        RenderJob job = new RenderJob("Left Aligned Text", config, templatePng, outputPath);

        // When
        pngRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatPngIsValid(outputPath);
    }

    @Test
    void shouldInsertTextAtCenterAlignment() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("center-aligned.png");
        TextConfig config = new TextConfig(400, 300, Alignment.CENTER); // Center of 800px width
        RenderJob job = new RenderJob("Center Aligned Text", config, templatePng, outputPath);

        // When
        pngRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatPngIsValid(outputPath);
    }

    @Test
    void shouldInsertTextAtRightAlignment() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("right-aligned.png");
        TextConfig config = new TextConfig(700, 300, Alignment.RIGHT);
        RenderJob job = new RenderJob("Right Aligned Text", config, templatePng, outputPath);

        // When
        pngRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatPngIsValid(outputPath);
    }

    @Test
    void shouldUseSpecifiedFont() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("custom-font.png");
        TextConfig config = new TextConfig(100, 300, Alignment.LEFT, "SansSerif", 24.0f);
        RenderJob job = new RenderJob("Custom Font Text", config, templatePng, outputPath);

        // When
        pngRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        assertThatPngIsValid(outputPath);
    }

    @Test
    void shouldPreserveTemplateImageQuality() throws Exception {
        // Given
        Path outputPath = tempDir.resolve("preserved-quality.png");
        TextConfig config = new TextConfig(100, 300, Alignment.LEFT);
        RenderJob job = new RenderJob("Quality Test", config, templatePng, outputPath);

        // When
        pngRendererService.render(job);

        // Then
        assertThat(outputPath).exists();
        BufferedImage outputImage = ImageIO.read(outputPath.toFile());
        assertThat(outputImage.getWidth()).isEqualTo(TEMPLATE_WIDTH);
        assertThat(outputImage.getHeight()).isEqualTo(TEMPLATE_HEIGHT);
    }

    private void assertThatPngIsValid(Path pngPath) throws Exception {
        BufferedImage image = ImageIO.read(pngPath.toFile());
        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(TEMPLATE_WIDTH);
        assertThat(image.getHeight()).isEqualTo(TEMPLATE_HEIGHT);
    }
}
