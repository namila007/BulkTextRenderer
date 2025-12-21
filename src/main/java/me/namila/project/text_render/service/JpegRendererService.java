package me.namila.project.text_render.service;

import me.namila.project.text_render.model.Alignment;
import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.model.TextConfig;
import me.namila.project.text_render.util.NativeImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;

/**
 * Renderer service for JPEG/JPG images.
 * Supports both .jpg and .jpeg file extensions.
 */
@Service
public class JpegRendererService implements RendererService {

    private static final Logger logger = LoggerFactory.getLogger(JpegRendererService.class);

    @Override
    public void render(RenderJob job) throws Exception {
        logger.debug("Rendering JPEG for text: '{}' at ({}, {})", job.text(), job.textConfig().x(), job.textConfig().y());
        
        BufferedImage image;
        try {
            image = ImageIO.read(job.templatePath().toFile());
        } catch (UnsatisfiedLinkError e) {
            // Provide a helpful error message for GraalVM native-image AWT issue
            throw new IllegalStateException(
                "JPEG rendering failed due to missing AWT support. " +
                "On GraalVM native-image (macOS), AWT native libraries are not bundled. " +
                "Use 'java -jar' mode for PNG/JPEG rendering. " +
                "See: https://github.com/oracle/graal/issues/4124", e
            );
        }
        
        if (image == null) {
            if (NativeImageUtil.isNativeImage()) {
                throw new IllegalStateException(
                    "Failed to read JPEG image from " + job.templatePath() + 
                    ". This may be due to missing AWT support in GraalVM native-image on macOS. " +
                    "Use 'java -jar' mode for JPEG rendering."
                );
            }
            throw new IllegalStateException("Failed to read image from " + job.templatePath() + 
                " - no suitable ImageReader found.");
        }
        
        // JPEG doesn't support transparency, convert to RGB if necessary
        BufferedImage rgbImage = ensureRgbImage(image);
        Graphics2D g2d = rgbImage.createGraphics();

        try {
            configureRenderingQuality(g2d);

            TextConfig config = job.textConfig();
            Font font = createFont(config.fontName(), config.fontSize(), config.fontStyle().getAwtStyle());
            g2d.setFont(font);
            
            // Apply color from config, default to black if not specified
            Color textColor = config.color() != null ? config.color() : Color.BLACK;
            g2d.setColor(textColor);

            int adjustedX = calculateAlignedX(g2d, job.text(), config.x(), config.alignment());
            g2d.drawString(job.text(), adjustedX, (int) config.y());
            
            logger.debug("Text rendered with font: {}, size: {}, style: {}, color: {}, alignment: {}", 
                config.fontName(), config.fontSize(), config.fontStyle(), textColor, config.alignment());
        } finally {
            g2d.dispose();
        }

        // Ensure parent directory exists
        if (job.outputPath().getParent() != null) {
            Files.createDirectories(job.outputPath().getParent());
        }

        ImageIO.write(rgbImage, "JPEG", job.outputPath().toFile());
        logger.debug("Successfully rendered JPEG to: {}", job.outputPath());
    }

    /**
     * Ensures the image is in RGB format for JPEG output.
     * JPEG doesn't support alpha channel, so we need to convert ARGB images.
     */
    private BufferedImage ensureRgbImage(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }
        
        BufferedImage rgbImage = new BufferedImage(
            image.getWidth(), 
            image.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g2d = rgbImage.createGraphics();
        try {
            // Fill with white background (for transparency)
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            // Draw the original image
            g2d.drawImage(image, 0, 0, null);
        } finally {
            g2d.dispose();
        }
        
        return rgbImage;
    }

    private void configureRenderingQuality(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    private Font createFont(String fontName, float fontSize, int fontStyle) {
        String mappedFontName = mapToSystemFontName(fontName);
        return new Font(mappedFontName, fontStyle, (int) fontSize);
    }

    private String mapToSystemFontName(String fontName) {
        if (fontName == null) {
            return Font.SERIF;
        }

        return switch (fontName.toLowerCase()) {
            case "helvetica", "sansserif", "sans-serif" -> Font.SANS_SERIF;
            case "courier", "monospace" -> Font.MONOSPACED;
            case "times new roman", "times", "serif" -> Font.SERIF;
            default -> fontName;
        };
    }

    private int calculateAlignedX(Graphics2D g2d, String text, float x, Alignment alignment) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);

        return switch (alignment) {
            case LEFT -> (int) x;
            case CENTER -> (int) x - (textWidth / 2);
            case RIGHT -> (int) x - textWidth;
        };
    }
}
