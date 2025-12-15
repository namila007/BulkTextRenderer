package me.namila.project.text_render.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
