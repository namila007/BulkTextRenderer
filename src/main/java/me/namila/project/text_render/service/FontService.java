package me.namila.project.text_render.service;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import me.namila.project.text_render.model.FontCategory;
import me.namila.project.text_render.model.FontInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

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
    
    private boolean systemFontsRegistered = false;

    /**
     * Registers system fonts for PDF rendering.
     * This scans common system font directories and makes fonts available for PDF generation.
     * Called lazily on first font request.
     */
    public synchronized void registerSystemFonts() {
        if (systemFontsRegistered) {
            return;
        }
        logger.info("Registering system fonts for PDF rendering...");
        FontFactory.registerDirectories();
        systemFontsRegistered = true;
        logger.debug("System fonts registered. Total registered: {}", FontFactory.getRegisteredFonts().size());
    }
    
    /**
     * Creates a BaseFont for PDF rendering, supporting both built-in and system fonts.
     * 
     * @param fontName the name of the font
     * @return BaseFont for PDF rendering
     */
    public BaseFont createBaseFontForPdf(String fontName) {
        // Ensure system fonts are registered
        if (!systemFontsRegistered) {
            registerSystemFonts();
        }
        
        if (fontName == null) {
            logger.debug("No font specified, using default Times Roman");
            return createBuiltInFont(BaseFont.TIMES_ROMAN);
        }
        
        // First try built-in fonts (fastest)
        Optional<BaseFont> builtIn = tryBuiltInFont(fontName);
        if (builtIn.isPresent()) {
            logger.debug("Using built-in font: {}", fontName);
            return builtIn.get();
        }
        
        // Try to get font from FontFactory (system fonts)
        Optional<BaseFont> systemFont = trySystemFont(fontName);
        if (systemFont.isPresent()) {
            logger.debug("Using system font: {}", fontName);
            return systemFont.get();
        }
        
        // Fallback to Times Roman
        logger.warn("Font '{}' not available for PDF embedding, falling back to Times Roman. " +
            "If the font exists on your system but can't be used, it may have lib issue.", fontName);
        return createBuiltInFont(BaseFont.TIMES_ROMAN);
    }
    
    /**
     * Tries to create a built-in PDF font.
     */
    private Optional<BaseFont> tryBuiltInFont(String fontName) {
        String baseFontName = mapToBuiltInFontName(fontName);
        if (baseFontName != null) {
            return Optional.of(createBuiltInFont(baseFontName));
        }
        return Optional.empty();
    }
    
    /**
     * Maps user font name to built-in PDF font name.
     */
    private String mapToBuiltInFontName(String fontName) {
        return switch (fontName.toLowerCase()) {
            case "helvetica" -> BaseFont.HELVETICA;
            case "courier" -> BaseFont.COURIER;
            case "times new roman", "times" -> BaseFont.TIMES_ROMAN;
            default -> null;
        };
    }
    
    /**
     * Creates a built-in BaseFont.
     */
    private BaseFont createBuiltInFont(String baseFontName) {
        try {
            return BaseFont.createFont(baseFontName, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            logger.error("Failed to create built-in font: {}", baseFontName, e);
            throw new RuntimeException("Failed to create font: " + baseFontName, e);
        }
    }
    
    /**
     * Tries to create a system font using OpenPDF FontFactory.
     */
    private Optional<BaseFont> trySystemFont(String fontName) {
        // First try from registered fonts
        Optional<BaseFont> fromRegistry = tryFromRegistry(fontName);
        if (fromRegistry.isPresent()) {
            return fromRegistry;
        }
        
        // If not found in registry, try to find and register the font file directly
        return tryFromFontFile(fontName);
    }
    
    /**
     * Tries to load font from FontFactory registry.
     */
    private Optional<BaseFont> tryFromRegistry(String fontName) {
        try {
            // Check if font is registered (case-insensitive search)
            Set<String> registeredFonts = FontFactory.getRegisteredFonts();
            String matchedFontName = findMatchingFont(fontName, registeredFonts);
            
            if (matchedFontName == null) {
                logger.debug("Font '{}' not found in registered fonts", fontName);
                return Optional.empty();
            }
            
            // Get font from FontFactory with Unicode encoding for full character support
            Font font = FontFactory.getFont(matchedFontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, 12);
            BaseFont baseFont = font.getBaseFont();
            
            if (baseFont != null) {
                logger.debug("Successfully loaded system font from registry: {} (matched: {})", fontName, matchedFontName);
                return Optional.of(baseFont);
            }
            
            logger.debug("FontFactory returned null BaseFont for: {}", matchedFontName);
            return Optional.empty();
            
        } catch (Exception e) {
            logger.debug("Failed to load system font '{}' from registry: {}", fontName, e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Tries to find and load font directly from font file.
     * This handles fonts that aren't properly registered by FontFactory.registerDirectories().
     */
    private Optional<BaseFont> tryFromFontFile(String fontName) {
        logger.debug("Searching for font file matching: {}", fontName);
        
        // Get font directories to search
        List<Path> fontDirs = getFontDirectories();
        
        // Normalize font name for file matching
        String normalizedName = fontName.toLowerCase()
            .replaceAll("[^a-z0-9]", ""); // Remove special chars for matching
        
        for (Path fontDir : fontDirs) {
            Optional<BaseFont> font = searchFontInDirectory(fontDir, fontName, normalizedName);
            if (font.isPresent()) {
                return font;
            }
        }
        
        logger.debug("No font file found for: {}", fontName);
        return Optional.empty();
    }
    
    /**
     * Gets list of font directories to search.
     */
    private List<Path> getFontDirectories() {
        List<Path> dirs = new ArrayList<>();
        
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        
        if (os.contains("mac")) {
            dirs.add(Path.of(userHome, "Library/Fonts"));
            dirs.add(Path.of("/Library/Fonts"));
            dirs.add(Path.of("/System/Library/Fonts"));
        } else if (os.contains("win")) {
            dirs.add(Path.of(System.getenv("WINDIR"), "Fonts"));
            dirs.add(Path.of(userHome, "AppData/Local/Microsoft/Windows/Fonts"));
        } else {
            // Linux
            dirs.add(Path.of(userHome, ".fonts"));
            dirs.add(Path.of(userHome, ".local/share/fonts"));
            dirs.add(Path.of("/usr/share/fonts"));
            dirs.add(Path.of("/usr/local/share/fonts"));
        }
        
        return dirs.stream()
            .filter(Files::exists)
            .toList();
    }
    
    /**
     * Searches for a font file in a directory.
     */
    private Optional<BaseFont> searchFontInDirectory(Path fontDir, String fontName, String normalizedName) {
        try (Stream<Path> files = Files.walk(fontDir, 3)) {  // Increased depth for Supplemental folders
            List<Path> fontFiles = files
                .filter(Files::isRegularFile)
                .filter(p -> {
                    String fileName = p.getFileName().toString().toLowerCase();
                    return fileName.endsWith(".ttf") || fileName.endsWith(".otf") || fileName.endsWith(".ttc");
                })
                .filter(p -> {
                    String fileName = p.getFileName().toString().toLowerCase()
                        .replaceAll("\\.(ttf|otf|ttc)$", "")
                        .replaceAll("[^a-z0-9]", "");
                    return fileName.contains(normalizedName) || normalizedName.contains(fileName);
                })
                .toList();
            
            for (Path fontFile : fontFiles) {
                Optional<BaseFont> font = tryLoadFontFile(fontFile, fontName);
                if (font.isPresent()) {
                    return font;
                }
            }
        } catch (IOException e) {
            logger.debug("Error searching font directory {}: {}", fontDir, e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Tries to load a font file, attempting different embedding strategies.
     */
    private Optional<BaseFont> tryLoadFontFile(Path fontFile, String fontName) {
        logger.debug("Trying to load font file: {}", fontFile);
        
        // Try with embedding first (best quality)
        Optional<BaseFont> embedded = tryLoadFontWithEmbedding(fontFile, fontName, BaseFont.EMBEDDED);
        if (embedded.isPresent()) {
            return embedded;
        }
        
        // Try without embedding (for fonts with licensing restrictions)
        Optional<BaseFont> notEmbedded = tryLoadFontWithEmbedding(fontFile, fontName, BaseFont.NOT_EMBEDDED);
        if (notEmbedded.isPresent()) {
            logger.info("Font '{}' loaded without embedding (may require font installed on viewer's system)", fontName);
            return notEmbedded;
        }
        
        return Optional.empty();
    }
    
    /**
     * Attempts to load font with specified embedding option.
     */
    private Optional<BaseFont> tryLoadFontWithEmbedding(Path fontFile, String fontName, boolean embedded) {
        String filePath = fontFile.toString();
        String fileName = fontFile.getFileName().toString().toLowerCase();
        
        // Handle TrueType Collection (.ttc) files - need to specify font index
        if (fileName.endsWith(".ttc")) {
            return tryLoadTtcFont(filePath, fontName, embedded);
        }
        
        try {
            // Try to create BaseFont directly from file
            BaseFont baseFont = BaseFont.createFont(
                filePath, 
                BaseFont.IDENTITY_H, 
                embedded
            );
            
            if (baseFont != null) {
                logger.debug("Successfully loaded font from file: {} (embedded={})", fontFile, embedded);
                return Optional.of(baseFont);
            }
        } catch (Exception e) {
            String message = e.getMessage();
            // Check if it's a licensing issue
            if (message != null && message.contains("licensing restrictions")) {
                logger.debug("Font file {} has embedding restrictions: {}", fontFile, message);
                // Try with subset embedding which sometimes bypasses restrictions
                return trySubsetEmbedding(fontFile, fontName);
            }
            logger.debug("Failed to load font file {} (embedded={}): {}", fontFile, embedded, message);
        }
        return Optional.empty();
    }
    
    /**
     * Tries to load a font from a TrueType Collection (.ttc) file.
     * TTC files contain multiple fonts and require an index to specify which one to use.
     */
    private Optional<BaseFont> tryLoadTtcFont(String filePath, String fontName, boolean embedded) {
        // Try first 10 font indices in the collection
        for (int i = 0; i < 10; i++) {
            try {
                String fontPath = filePath + "," + i;
                BaseFont baseFont = BaseFont.createFont(
                    fontPath,
                    BaseFont.IDENTITY_H,
                    embedded
                );
                
                if (baseFont != null) {
                    logger.debug("Successfully loaded TTC font from: {} (index={}, embedded={})", filePath, i, embedded);
                    return Optional.of(baseFont);
                }
            } catch (Exception e) {
                String message = e.getMessage();
                if (message != null && message.contains("licensing restrictions")) {
                    logger.debug("TTC font {} index {} has embedding restrictions", filePath, i);
                    continue;
                }
                // If we get "index out of range" or similar, stop trying more indices
                if (message != null && (message.contains("index") || message.contains("not recognized"))) {
                    break;
                }
                logger.debug("Failed to load TTC font {} index {}: {}", filePath, i, message);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Tries to load font with subset embedding as a workaround for some licensing restrictions.
     */
    private Optional<BaseFont> trySubsetEmbedding(Path fontFile, String fontName) {
        try {
            // Some fonts allow subset embedding even when full embedding is restricted
            BaseFont baseFont = BaseFont.createFont(
                fontFile.toString(), 
                BaseFont.IDENTITY_H, 
                BaseFont.EMBEDDED,
                true,  // cached
                null,  // ttfAfm
                null,  // pfb
                true   // noThrow - important for handling partial failures
            );
            
            if (baseFont != null) {
                // Try to enable subset embedding
                baseFont.setSubset(true);
                logger.info("Font '{}' loaded with subset embedding from: {}", fontName, fontFile);
                return Optional.of(baseFont);
            }
        } catch (Exception e) {
            logger.debug("Subset embedding also failed for {}: {}", fontFile, e.getMessage());
        }
        return Optional.empty();
    }
    
    /**
     * Finds a matching font name (case-insensitive).
     */
    private String findMatchingFont(String fontName, Set<String> registeredFonts) {
        String normalizedSearch = fontName.toLowerCase().trim();
        
        // First try exact match (case-insensitive)
        for (String registered : registeredFonts) {
            if (registered.equalsIgnoreCase(fontName)) {
                return registered;
            }
        }
        
        // Try partial match (font name contains search term or vice versa)
        for (String registered : registeredFonts) {
            String normalizedRegistered = registered.toLowerCase();
            if (normalizedRegistered.contains(normalizedSearch) || 
                normalizedSearch.contains(normalizedRegistered)) {
                return registered;
            }
        }
        
        return null;
    }
    
    /**
     * Checks if a font is available as a system font for PDF.
     */
    public boolean isSystemFontAvailable(String fontName) {
        if (!systemFontsRegistered) {
            registerSystemFonts();
        }
        return findMatchingFont(fontName, FontFactory.getRegisteredFonts()) != null;
    }
    
    /**
     * Gets a unified list of available fonts categorized by format support.
     * 
     * Categories:
     * - BUILT_IN: Built-in PDF fonts (work everywhere)
     * - SYSTEM: PDF-registered system fonts (work for PDF, PNG, JPEG)
     * - Other fonts: Available only for PNG/JPEG rendering
     *
     * @return sorted list of FontInfo objects
     */
    public List<FontInfo> getUnifiedAvailableFonts() {
        // Ensure fonts are registered
        if (!systemFontsRegistered) {
            registerSystemFonts();
        }
        
        List<FontInfo> fonts = new ArrayList<>();
        Set<String> addedFonts = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        
        // Add built-in fonts first (work for all formats), sorted
        List<String> sortedBuiltIns = new ArrayList<>(BUILT_IN_FONTS);
        sortedBuiltIns.sort(String.CASE_INSENSITIVE_ORDER);
        for (String font : sortedBuiltIns) {
            fonts.add(new FontInfo(font, FontCategory.BUILT_IN));
            addedFonts.add(font);
        }
        
        // Get PDF-registered fonts (excluding built-ins)
        Set<String> pdfFonts = FontFactory.getRegisteredFonts();
        Set<String> pdfRegisteredFonts = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String font : pdfFonts) {
            if (!addedFonts.contains(font)) {
                pdfRegisteredFonts.add(font);
                addedFonts.add(font);
            }
        }
        
        // Get all system fonts from GraphicsEnvironment
        String[] allSystemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
        
        // Add fonts that are available for PNG/JPEG but not registered for PDF
        Set<String> pngJpegOnlyFonts = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String font : allSystemFonts) {
            if (!addedFonts.contains(font)) {
                pngJpegOnlyFonts.add(font);
            }
        }
        
        // Combine all system fonts and sort them together
        Set<String> allSystemFontsSorted = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        allSystemFontsSorted.addAll(pdfRegisteredFonts);
        allSystemFontsSorted.addAll(pngJpegOnlyFonts);
        
        // Add all system fonts in sorted order
        for (String font : allSystemFontsSorted) {
            fonts.add(new FontInfo(font, FontCategory.SYSTEM));
        }
        
        int pdfRegistered = pdfRegisteredFonts.size();
        int pngJpegOnly = pngJpegOnlyFonts.size();
        logger.debug("Retrieved {} fonts ({} built-in, {} PDF-registered, {} PNG/JPEG-only)", 
            fonts.size(), BUILT_IN_FONTS.size(), pdfRegistered, pngJpegOnly);
        
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
