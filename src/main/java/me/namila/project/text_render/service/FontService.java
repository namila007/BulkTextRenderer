package me.namila.project.text_render.service;

import com.lowagie.text.FontFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Service for managing fonts across PDF and PNG rendering.
 */
@Service
public class FontService {

    private static final Logger logger = LoggerFactory.getLogger(FontService.class);

    /**
     * Registers system fonts for PDF rendering.
     * This scans common system font directories and makes fonts available for PDF generation.
     */
    public void registerSystemFonts() {
        logger.info("Registering system fonts for PDF rendering...");
        FontFactory.registerDirectories();
        logger.debug("System fonts registered. Total registered: {}", FontFactory.getRegisteredFonts().size());
    }

    /**
     * Gets the set of available fonts for PDF rendering.
     *
     * @return set of registered PDF font names
     */
    public Set<String> getAvailablePdfFonts() {
        return FontFactory.getRegisteredFonts();
    }

    /**
     * Gets the set of available fonts for PNG rendering.
     *
     * @return set of available system font family names
     */
    public Set<String> getAvailablePngFonts() {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
        return new TreeSet<>(Arrays.asList(fonts));
    }

    /**
     * Checks if a font is available for the specified format.
     *
     * @param fontName the name of the font to check
     * @param format   the output format (pdf or png)
     * @return true if the font is available, false otherwise
     */
    public boolean isFontAvailable(String fontName, String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            return getAvailablePdfFonts().stream()
                .anyMatch(f -> f.equalsIgnoreCase(fontName));
        } else if ("png".equalsIgnoreCase(format)) {
            return getAvailablePngFonts().stream()
                .anyMatch(f -> f.equalsIgnoreCase(fontName));
        }
        return false;
    }
}
