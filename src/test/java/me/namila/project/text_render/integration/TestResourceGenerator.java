package me.namila.project.text_render.integration;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class to generate test resource files (PDF and PNG templates).
 */
public final class TestResourceGenerator {

    private TestResourceGenerator() {
        // Utility class
    }

    /**
     * Creates a simple single-page PDF template at the specified path.
     *
     * @param outputPath the path where the PDF will be created
     * @throws IOException if an error occurs during file creation
     */
    public static void createTemplatePdf(Path outputPath) throws IOException {
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputPath.toFile()));
            document.open();
            document.add(new Paragraph(" ")); // Add minimal content to make valid PDF
            document.close();
        } catch (Exception e) {
            throw new IOException("Failed to create template PDF", e);
        }
    }

    /**
     * Creates a simple white PNG image at the specified path.
     *
     * @param outputPath the path where the PNG will be created
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @throws IOException if an error occurs during file creation
     */
    public static void createTemplatePng(Path outputPath, int width, int height) throws IOException {
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        try {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
        } finally {
            g2d.dispose();
        }
        
        ImageIO.write(image, "PNG", outputPath.toFile());
    }

    /**
     * Creates a simple white PNG image with default size (200x200).
     *
     * @param outputPath the path where the PNG will be created
     * @throws IOException if an error occurs during file creation
     */
    public static void createTemplatePng(Path outputPath) throws IOException {
        createTemplatePng(outputPath, 200, 200);
    }

    /**
     * Ensures the test template PDF exists, creating it if necessary.
     *
     * @param path the path to check/create
     * @throws IOException if an error occurs
     */
    public static void ensureTemplatePdfExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            createTemplatePdf(path);
        }
    }

    /**
     * Ensures the test template PNG exists, creating it if necessary.
     *
     * @param path the path to check/create
     * @throws IOException if an error occurs
     */
    public static void ensureTemplatePngExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            createTemplatePng(path);
        }
    }

    /**
     * Creates a simple white JPEG image at the specified path.
     *
     * @param outputPath the path where the JPEG will be created
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @throws IOException if an error occurs during file creation
     */
    public static void createTemplateJpeg(Path outputPath, int width, int height) throws IOException {
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        try {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
        } finally {
            g2d.dispose();
        }
        
        ImageIO.write(image, "JPEG", outputPath.toFile());
    }

    /**
     * Creates a simple white JPEG image with default size (200x200).
     *
     * @param outputPath the path where the JPEG will be created
     * @throws IOException if an error occurs during file creation
     */
    public static void createTemplateJpeg(Path outputPath) throws IOException {
        createTemplateJpeg(outputPath, 200, 200);
    }

    /**
     * Ensures the test template JPEG exists, creating it if necessary.
     *
     * @param path the path to check/create
     * @throws IOException if an error occurs
     */
    public static void ensureTemplateJpegExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            createTemplateJpeg(path);
        }
    }
}
