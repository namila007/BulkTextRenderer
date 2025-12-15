package me.namila.project.text_render.service;

import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import me.namila.project.text_render.model.Alignment;
import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.model.TextConfig;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;

@Service
public class PdfRendererService implements RendererService {

    @Override
    public void render(RenderJob job) throws Exception {
        PdfReader reader = new PdfReader(job.templatePath().toString());
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(job.outputPath().toFile()));

        try {
            PdfContentByte canvas = stamper.getOverContent(1);
            TextConfig config = job.textConfig();

            BaseFont baseFont = createBaseFont(config.fontName());
            canvas.setFontAndSize(baseFont, config.fontSize());

            int alignment = mapAlignment(config.alignment());

            canvas.beginText();
            canvas.showTextAligned(alignment, job.text(), config.x(), config.y(), 0);
            canvas.endText();
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
