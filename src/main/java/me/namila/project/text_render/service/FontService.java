package me.namila.project.text_render.service;

import com.lowagie.text.FontFactory;
import me.namila.project.text_render.model.FontCategory;
import me.namila.project.text_render.model.FontInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Service for managing fonts across PDF and PNG rendering.
 */
@Service
public class FontService {

    private static final Logger logger = LoggerFactory.getLogger(FontService.class);
    
    /**
     * Built-in fonts that work without system dependencies.
     */
    private static final List<String> BUILT_IN_FONTS = List.of(
        "Helvetica",
        "Courier",
        "Times New Roman"
    );

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
     * Gets a unified list of available fonts with category information.
     * Built-in fonts are listed first, followed by system fonts.
     *
     * @return sorted list of FontInfo objects
     */
    public List<FontInfo> getUnifiedAvailableFonts() {
        List<FontInfo> fonts = new ArrayList<>();
        Set<String> addedFonts = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        
        // Add built-in fonts first
        for (String font : BUILT_IN_FONTS) {
            fonts.add(new FontInfo(font, FontCategory.BUILT_IN));
            addedFonts.add(font);
        }
        
        // Add system fonts (excluding duplicates)
        String[] systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
        
        for (String font : systemFonts) {
            if (!addedFonts.contains(font)) {
                fonts.add(new FontInfo(font, FontCategory.SYSTEM));
                addedFonts.add(font);
            }
        }
        
        // Sort by category then by name
        fonts.sort(FontInfo::compareTo);
        
        logger.debug("Retrieved {} fonts ({} built-in, {} system)", 
            fonts.size(), BUILT_IN_FONTS.size(), fonts.size() - BUILT_IN_FONTS.size());
        
        return fonts;
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
