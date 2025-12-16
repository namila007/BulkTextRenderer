package me.namila.project.text_render.service;

import com.lowagie.text.pdf.BaseFont;
import me.namila.project.text_render.model.FontCategory;
import me.namila.project.text_render.model.FontInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FontServiceTest {

    private FontService fontService;

    @BeforeEach
    void setUp() {
        fontService = new FontService();
    }

    @Test
    void shouldRegisterSystemFonts() {
        // When
        fontService.registerSystemFonts();

        // Then - after registration, should have some fonts available
        Set<String> fonts = fontService.getAvailablePdfFonts();
        assertThat(fonts).isNotEmpty();
    }

    @Test
    void shouldGetAvailablePdfFonts() {
        // Given
        fontService.registerSystemFonts();

        // When
        Set<String> fonts = fontService.getAvailablePdfFonts();

        // Then - should contain at least built-in fonts
        assertThat(fonts).isNotNull();
        assertThat(fonts).isNotEmpty();
    }

    @Test
    void shouldGetAvailablePngFonts() {
        // When
        Set<String> fonts = fontService.getAvailablePngFonts();

        // Then - should have system fonts available
        assertThat(fonts).isNotNull();
        assertThat(fonts).isNotEmpty();
    }

    @Test
    void shouldReturnTrueWhenPdfFontIsAvailable() {
        // Given
        fontService.registerSystemFonts();
        Set<String> availableFonts = fontService.getAvailablePdfFonts();
        // Get any available font for testing
        String existingFont = availableFonts.iterator().next();

        // When
        boolean result = fontService.isFontAvailable(existingFont, "pdf");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueWhenPngFontIsAvailable() {
        // Given
        Set<String> availableFonts = fontService.getAvailablePngFonts();
        // Get any available font for testing
        String existingFont = availableFonts.iterator().next();

        // When
        boolean result = fontService.isFontAvailable(existingFont, "png");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFontNotAvailable() {
        // Given
        fontService.registerSystemFonts();
        String nonExistentFont = "NonExistentFont12345XYZ";

        // When
        boolean pdfResult = fontService.isFontAvailable(nonExistentFont, "pdf");
        boolean pngResult = fontService.isFontAvailable(nonExistentFont, "png");

        // Then
        assertThat(pdfResult).isFalse();
        assertThat(pngResult).isFalse();
    }

    @Test
    void shouldReturnFalseForUnknownFormat() {
        // Given
        fontService.registerSystemFonts();

        // When
        boolean result = fontService.isFontAvailable("Arial", "xyz");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldBeCaseInsensitiveForFormat() {
        // Given
        fontService.registerSystemFonts();
        Set<String> pdfFonts = fontService.getAvailablePdfFonts();
        String existingFont = pdfFonts.iterator().next();

        // When
        boolean upperResult = fontService.isFontAvailable(existingFont, "PDF");
        boolean lowerResult = fontService.isFontAvailable(existingFont, "pdf");
        boolean mixedResult = fontService.isFontAvailable(existingFont, "Pdf");

        // Then
        assertThat(upperResult).isTrue();
        assertThat(lowerResult).isTrue();
        assertThat(mixedResult).isTrue();
    }

    // --- Unified Font Listing Tests ---

    @Test
    void shouldReturnUnifiedFontListContainingBuiltInFonts() {
        // Given
        fontService.registerSystemFonts();

        // When
        List<FontInfo> unifiedFonts = fontService.getUnifiedAvailableFonts();

        // Then - should contain built-in fonts
        assertThat(unifiedFonts).isNotEmpty();
        assertThat(unifiedFonts)
                .anyMatch(font -> font.name().equals("Helvetica") && font.category() == FontCategory.BUILT_IN);
        assertThat(unifiedFonts)
                .anyMatch(font -> font.name().equals("Courier") && font.category() == FontCategory.BUILT_IN);
        assertThat(unifiedFonts)
                .anyMatch(font -> font.name().equals("Times New Roman") && font.category() == FontCategory.BUILT_IN);
    }

    @Test
    void shouldReturnUnifiedFontListContainingSystemFonts() {
        // Given
        fontService.registerSystemFonts();

        // When
        List<FontInfo> unifiedFonts = fontService.getUnifiedAvailableFonts();

        // Then - should contain system fonts
        assertThat(unifiedFonts)
                .anyMatch(font -> font.category() == FontCategory.SYSTEM);
    }

    @Test
    void shouldReturnBuiltInFontsFirstInUnifiedList() {
        // Given
        fontService.registerSystemFonts();

        // When
        List<FontInfo> unifiedFonts = fontService.getUnifiedAvailableFonts();

        // Then - built-in fonts should come before system fonts
        int lastBuiltInIndex = -1;
        int firstSystemIndex = Integer.MAX_VALUE;
        
        for (int i = 0; i < unifiedFonts.size(); i++) {
            if (unifiedFonts.get(i).category() == FontCategory.BUILT_IN) {
                lastBuiltInIndex = i;
            } else if (unifiedFonts.get(i).category() == FontCategory.SYSTEM && firstSystemIndex == Integer.MAX_VALUE) {
                firstSystemIndex = i;
            }
        }
        
        assertThat(lastBuiltInIndex).isLessThan(firstSystemIndex);
    }

    @Test
    void shouldNotContainDuplicateFontsInUnifiedList() {
        // Given
        fontService.registerSystemFonts();

        // When
        List<FontInfo> unifiedFonts = fontService.getUnifiedAvailableFonts();
        List<String> fontNames = unifiedFonts.stream().map(FontInfo::name).toList();

        // Then - no duplicate font names
        assertThat(fontNames).doesNotHaveDuplicates();
    }

    @Test
    void shouldSortFontsAlphabeticallyWithinCategory() {
        // Given
        fontService.registerSystemFonts();

        // When
        List<FontInfo> unifiedFonts = fontService.getUnifiedAvailableFonts();

        // Then - fonts within each category should be sorted alphabetically
        List<FontInfo> builtInFonts = unifiedFonts.stream()
                .filter(f -> f.category() == FontCategory.BUILT_IN)
                .toList();
        List<FontInfo> systemFonts = unifiedFonts.stream()
                .filter(f -> f.category() == FontCategory.SYSTEM)
                .toList();

        // Check built-in fonts are sorted
        for (int i = 1; i < builtInFonts.size(); i++) {
            assertThat(builtInFonts.get(i - 1).name().compareToIgnoreCase(builtInFonts.get(i).name()))
                    .isLessThanOrEqualTo(0);
        }

        // Check system fonts are sorted
        for (int i = 1; i < systemFonts.size(); i++) {
            assertThat(systemFonts.get(i - 1).name().compareToIgnoreCase(systemFonts.get(i).name()))
                    .isLessThanOrEqualTo(0);
        }
    }
    
    // --- BaseFont Creation Tests ---
    
    @Test
    void shouldCreateBaseFontForBuiltInHelvetica() {
        // When
        BaseFont font = fontService.createBaseFontForPdf("Helvetica");
        
        // Then
        assertThat(font).isNotNull();
    }
    
    @Test
    void shouldCreateBaseFontForBuiltInCourier() {
        // When
        BaseFont font = fontService.createBaseFontForPdf("Courier");
        
        // Then
        assertThat(font).isNotNull();
    }
    
    @Test
    void shouldCreateBaseFontForBuiltInTimesRoman() {
        // When
        BaseFont font = fontService.createBaseFontForPdf("Times New Roman");
        
        // Then
        assertThat(font).isNotNull();
    }
    
    @Test
    void shouldCreateBaseFontForBuiltInTimesCaseInsensitive() {
        // When
        BaseFont font = fontService.createBaseFontForPdf("times");
        
        // Then
        assertThat(font).isNotNull();
    }
    
    @Test
    void shouldFallbackToTimesRomanForNullFont() {
        // When
        BaseFont font = fontService.createBaseFontForPdf(null);
        
        // Then
        assertThat(font).isNotNull();
    }
    
    @Test
    void shouldFallbackToTimesRomanForUnknownFont() {
        // When
        BaseFont font = fontService.createBaseFontForPdf("NonExistentFont12345XYZ");
        
        // Then - should return Times Roman fallback
        assertThat(font).isNotNull();
    }
    
    @Test
    void shouldCreateBaseFontForSystemFontIfAvailable() {
        // Given - find a system font that's available
        fontService.registerSystemFonts();
        Set<String> registeredFonts = fontService.getAvailablePdfFonts();
        
        // Find a font that's not a built-in (skip helvetica, courier, times)
        String systemFont = registeredFonts.stream()
            .filter(f -> !f.equalsIgnoreCase("helvetica") 
                && !f.equalsIgnoreCase("courier")
                && !f.equalsIgnoreCase("times")
                && !f.equalsIgnoreCase("times new roman"))
            .findFirst()
            .orElse(null);
        
        if (systemFont != null) {
            // When
            BaseFont font = fontService.createBaseFontForPdf(systemFont);
            
            // Then
            assertThat(font).isNotNull();
        }
        // Skip test if no system fonts available (shouldn't happen on real systems)
    }
    
    @Test
    void shouldCheckIfSystemFontIsAvailable() {
        // Given
        fontService.registerSystemFonts();
        
        // When/Then - check built-in font
        assertThat(fontService.isSystemFontAvailable("Helvetica")).isTrue();
        assertThat(fontService.isSystemFontAvailable("NonExistentFont12345")).isFalse();
    }
    
    @Test
    void shouldRegisterSystemFontsLazily() {
        // Given - fresh FontService
        FontService freshService = new FontService();
        
        // When - call createBaseFontForPdf without explicit registration
        BaseFont font = freshService.createBaseFontForPdf("Helvetica");
        
        // Then - should work (lazy registration)
        assertThat(font).isNotNull();
    }
    
    @Test
    void shouldOnlyRegisterSystemFontsOnce() {
        // Given
        FontService freshService = new FontService();
        
        // When - call registerSystemFonts multiple times
        freshService.registerSystemFonts();
        int countAfterFirst = freshService.getAvailablePdfFonts().size();
        freshService.registerSystemFonts();
        int countAfterSecond = freshService.getAvailablePdfFonts().size();
        
        // Then - should have same count (not re-registered)
        assertThat(countAfterFirst).isEqualTo(countAfterSecond);
    }
}
