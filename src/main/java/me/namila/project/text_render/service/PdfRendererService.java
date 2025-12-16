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

import java.io.FileOutputStream;

/**
 * PDF renderer service that inserts text onto PDF templates.
 * 
 * <p>Coordinate System:
 * The coordinate system uses top-left origin (0,0) for consistency with PNG rendering.
 * User-provided Y coordinates are automatically transformed from top-left to PDF's
 * native bottom-left coordinate system.
 * </p>
 */
@Service
public class PdfRendererService implements RendererService {

    private static final Logger logger = LoggerFactory.getLogger(PdfRendererService.class);

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

            BaseFont baseFont = createBaseFont(config.fontName());
            canvas.setFontAndSize(baseFont, config.fontSize());

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

    private BaseFont createBaseFont(String fontName) throws Exception {
        String baseFontName = mapToBaseFontName(fontName);
        return BaseFont.createFont(baseFontName, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    }

    private String mapToBaseFontName(String fontName) {
        if (fontName == null) {
            return BaseFont.TIMES_ROMAN;
        }

        return switch (fontName.toLowerCase()) {
            case "helvetica" -> BaseFont.HELVETICA;
            case "courier" -> BaseFont.COURIER;
            case "times new roman", "times" -> BaseFont.TIMES_ROMAN;
            default -> BaseFont.TIMES_ROMAN;
        };
    }

    private int mapAlignment(Alignment alignment) {
        return switch (alignment) {
            case LEFT -> PdfContentByte.ALIGN_LEFT;
            case CENTER -> PdfContentByte.ALIGN_CENTER;
            case RIGHT -> PdfContentByte.ALIGN_RIGHT;
        };
    }
}
