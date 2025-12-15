package me.namila.project.text_render.service;

import me.namila.project.text_render.model.Alignment;
import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.model.TextConfig;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

@Service
public class PngRendererService implements RendererService {

    @Override
    public void render(RenderJob job) throws Exception {
        BufferedImage image = ImageIO.read(job.templatePath().toFile());
        Graphics2D g2d = image.createGraphics();

        try {
            configureRenderingQuality(g2d);

            TextConfig config = job.textConfig();
            Font font = createFont(config.fontName(), config.fontSize());
            g2d.setFont(font);
            g2d.setColor(Color.BLACK);

            int adjustedX = calculateAlignedX(g2d, job.text(), config.x(), config.alignment());
            g2d.drawString(job.text(), adjustedX, (int) config.y());
        } finally {
            g2d.dispose();
        }

        ImageIO.write(image, "PNG", job.outputPath().toFile());
    }

    private void configureRenderingQuality(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    private Font createFont(String fontName, float fontSize) {
        String mappedFontName = mapToSystemFontName(fontName);
        return new Font(mappedFontName, Font.PLAIN, (int) fontSize);
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
