package me.namila.project.text_render.service;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Renderer service for JPEG/JPG images.
 * Supports both .jpg and .jpeg file extensions.
 * Extends AbstractImageRendererService and adds JPEG-specific preprocessing.
 */
@Service
public class JpegRendererService extends AbstractImageRendererService {

    @Override
    protected String getFormatName() {
        return "JPEG";
    }

    @Override
    protected String getImageFormat() {
        return "JPEG";
    }

    /**
     * Preprocesses the image to ensure RGB format for JPEG output.
     * JPEG doesn't support alpha channel, so ARGB images are converted.
     */
    @Override
    protected BufferedImage preprocessImage(BufferedImage image) {
        return ensureRgbImage(image);
    }

    /**
     * Adds JPEG-specific interpolation hint for better quality.
     */
    @Override
    protected void configureRenderingQuality(Graphics2D g2d) {
        super.configureRenderingQuality(g2d);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    /**
     * Ensures the image is in RGB format for JPEG output.
     * JPEG doesn't support alpha channel, so we need to convert ARGB images.
     * 
     * @param image the original image
     * @return RGB-converted image
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
}
