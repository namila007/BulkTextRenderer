package me.namila.project.text_render.service;

import me.namila.project.text_render.model.Alignment;
import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.model.TextConfig;
import me.namila.project.text_render.util.NativeImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;

/**
 * Abstract base class for image-based renderer services (PNG, JPEG).
 * Provides common functionality for loading images, rendering text, and saving output.
 * 
 * <p>This class implements the Template Method pattern, allowing subclasses to customize
 * specific aspects of the rendering process while sharing common logic.</p>
 */
public abstract class AbstractImageRendererService implements RendererService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractImageRendererService.class);

    /**
     * Main rendering method implementing the template method pattern.
     * 
     * @param job the render job containing template, output path, and text configuration
     * @throws Exception if rendering fails
     */
    @Override
    public final void render(RenderJob job) throws Exception {
        logger.debug("Rendering {} for text: '{}' at ({}, {})", 
            getFormatName(), job.text(), job.textConfig().x(), job.textConfig().y());
        
        BufferedImage image = loadImageWithErrorHandling(job);
        BufferedImage processedImage = preprocessImage(image);
        
        renderTextOnImage(processedImage, job);
        
        ensureOutputDirectoryExists(job.outputPath());
        saveImage(processedImage, job.outputPath());
        
        logger.debug("Successfully rendered {} to: {}", getFormatName(), job.outputPath());
    }

    /**
     * Returns the format name for logging purposes (e.g., "PNG", "JPEG").
     * 
     * @return the format name
     */
    protected abstract String getFormatName();

    /**
     * Returns the image format identifier for ImageIO.write() (e.g., "PNG", "JPEG").
     * 
     * @return the image format identifier
     */
    protected abstract String getImageFormat();

    /**
     * Preprocesses the loaded image before text rendering.
     * Default implementation returns the image unchanged.
     * Subclasses can override to perform format-specific preprocessing (e.g., RGB conversion for JPEG).
     * 
     * @param image the loaded image
     * @return the preprocessed image
     */
    protected BufferedImage preprocessImage(BufferedImage image) {
        return image;
    }

    /**
     * Configures rendering quality hints for Graphics2D.
     * Can be overridden by subclasses to add format-specific hints.
     * 
     * @param g2d the Graphics2D context
     */
    protected void configureRenderingQuality(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    /**
     * Loads the template image with proper error handling for GraalVM native-image limitations.
     * 
     * @param job the render job containing the template path
     * @return the loaded BufferedImage
     * @throws Exception if image loading fails
     */
    private BufferedImage loadImageWithErrorHandling(RenderJob job) throws Exception {
        BufferedImage image;
        try {
            image = ImageIO.read(job.templatePath().toFile());
        } catch (UnsatisfiedLinkError e) {
            throw new IllegalStateException(
                getFormatName() + " rendering failed due to missing AWT support. " +
                "On GraalVM native-image (macOS), AWT native libraries are not bundled. " +
                "Use 'java -jar' mode for " + getFormatName() + " rendering. " +
                "See: https://github.com/oracle/graal/issues/4124", e
            );
        }

        if (image == null) {
            if (NativeImageUtil.isNativeImage()) {
                throw new IllegalStateException(
                    "Failed to read " + getFormatName() + " image from " + job.templatePath() + 
                    ". This may be due to missing AWT support in GraalVM native-image on macOS. " +
                    "Use 'java -jar' mode for " + getFormatName() + " rendering."
                );
            }
            throw new IllegalStateException("Failed to read image from " + job.templatePath());
        }

        return image;
    }

    /**
     * Renders text on the provided image using Graphics2D.
     * 
     * @param image the image to render text on
     * @param job the render job containing text and configuration
     */
    private void renderTextOnImage(BufferedImage image, RenderJob job) {
        Graphics2D g2d = image.createGraphics();

        try {
            configureRenderingQuality(g2d);

            TextConfig config = job.textConfig();
            Font font = createFont(config.fontName(), config.fontSize(), config.fontStyle().getAwtStyle());
            g2d.setFont(font);
            
            Color textColor = config.color() != null ? config.color() : Color.BLACK;
            g2d.setColor(textColor);

            int adjustedX = calculateAlignedX(g2d, job.text(), config.x(), config.alignment());
            g2d.drawString(job.text(), adjustedX, (int) config.y());
            
            logger.debug("Text rendered with font: {}, size: {}, style: {}, color: {}, alignment: {}", 
                config.fontName(), config.fontSize(), config.fontStyle(), textColor, config.alignment());
        } finally {
            g2d.dispose();
        }
    }

    /**
     * Ensures the output directory exists before saving the image.
     * 
     * @param outputPath the output file path
     * @throws Exception if directory creation fails
     */
    private void ensureOutputDirectoryExists(java.nio.file.Path outputPath) throws Exception {
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
    }

    /**
     * Saves the rendered image to the output path.
     * 
     * @param image the rendered image
     * @param outputPath the output file path
     * @throws Exception if saving fails
     */
    private void saveImage(BufferedImage image, java.nio.file.Path outputPath) throws Exception {
        ImageIO.write(image, getImageFormat(), outputPath.toFile());
    }

    /**
     * Creates a Font object with the specified parameters.
     * 
     * @param fontName the font name
     * @param fontSize the font size
     * @param fontStyle the AWT font style (Font.PLAIN, Font.BOLD, etc.)
     * @return the created Font
     */
    protected Font createFont(String fontName, float fontSize, int fontStyle) {
        String mappedFontName = mapToSystemFontName(fontName);
        return new Font(mappedFontName, fontStyle, (int) fontSize);
    }

    /**
     * Maps PDF-style font names to AWT system font names.
     * 
     * @param fontName the font name to map
     * @return the mapped system font name
     */
    protected String mapToSystemFontName(String fontName) {
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

    /**
     * Calculates the X coordinate adjusted for text alignment.
     * 
     * @param g2d the Graphics2D context
     * @param text the text to render
     * @param x the base X coordinate
     * @param alignment the text alignment
     * @return the adjusted X coordinate
     */
    protected int calculateAlignedX(Graphics2D g2d, String text, float x, Alignment alignment) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);

        return switch (alignment) {
            case LEFT -> (int) x;
            case CENTER -> (int) x - (textWidth / 2);
            case RIGHT -> (int) x - textWidth;
        };
    }
}
