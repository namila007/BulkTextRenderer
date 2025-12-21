package me.namila.project.text_render.service;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import me.namila.project.text_render.model.Alignment;
import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.model.TextConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.FileOutputStream;

/**
 * PDF renderer service that inserts text onto PDF templates.
 * 
 * <p>Coordinate System:
 * The coordinate system uses top-left origin (0,0) for consistency with PNG rendering.
 * User-provided Y coordinates are automatically transformed from top-left to PDF's
 * native bottom-left coordinate system.
 * </p>
 * 
 * <p>Font Support:
 * Supports both built-in PDF fonts (Helvetica, Courier, Times Roman) and system fonts.
 * System fonts are automatically discovered and registered. If a requested font is not
 * available, falls back to Times Roman.
 * </p>
 * 
 * <p>Font Styling:
 * Supports font color (any hex color), bold, italic, and bold-italic styles.
 * Note: Bold/italic requires the font to have the corresponding variant available.
 * </p>
 */
@Service
public class PdfRendererService implements RendererService {

    private static final Logger logger = LoggerFactory.getLogger(PdfRendererService.class);
    
    private final FontService fontService;
    
    public PdfRendererService(FontService fontService) {
        this.fontService = fontService;
    }

    @Override
    public void render(RenderJob job) throws Exception {
        logger.debug("Rendering PDF for text: '{}' at ({}, {})", job.text(), job.textConfig().x(), job.textConfig().y());
        
        PdfReader reader = new PdfReader(job.templatePath().toString());
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(job.outputPath().toFile()));

        try {
            PdfContentByte canvas = stamper.getOverContent(1);
            TextConfig config = job.textConfig();

            // Get page dimensions for coordinate transformation
            Rectangle pageSize = reader.getPageSize(1);
            float pageHeight = pageSize.getHeight();
            
            // Transform Y coordinate from top-left origin to PDF's bottom-left origin
            // In PDF, Y=0 is at bottom; in our API, Y=0 is at top (like PNG)
            float transformedY = pageHeight - config.y();
            logger.debug("Transformed Y coordinate: {} -> {} (page height: {})", config.y(), transformedY, pageHeight);

            // Use FontService to create font with system font support and style
            BaseFont baseFont = fontService.createBaseFontForPdf(config.fontName(), config.fontStyle());
            canvas.setFontAndSize(baseFont, config.fontSize());
            logger.debug("Using font: {} at size: {} with style: {}", config.fontName(), config.fontSize(), config.fontStyle());

            // Apply font color
            Color color = config.color();
            canvas.setRGBColorFill(color.getRed(), color.getGreen(), color.getBlue());
            logger.debug("Applied font color: #{}", String.format("%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));

            int alignment = mapAlignment(config.alignment());

            canvas.beginText();
            canvas.showTextAligned(alignment, job.text(), config.x(), transformedY, 0);
            canvas.endText();
            
            logger.debug("Successfully rendered PDF to: {}", job.outputPath());
        } finally {
            stamper.close();
            reader.close();
        }
    }

    private int mapAlignment(Alignment alignment) {
        return switch (alignment) {
            case LEFT -> PdfContentByte.ALIGN_LEFT;
            case CENTER -> PdfContentByte.ALIGN_CENTER;
            case RIGHT -> PdfContentByte.ALIGN_RIGHT;
        };
    }
}
