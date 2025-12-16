package me.namila.project.text_render.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FontInfoTest {

    @Test
    void shouldCreateFontInfoWithNameAndCategory() {
        // When
        FontInfo fontInfo = new FontInfo("Arial", FontCategory.SYSTEM);

        // Then
        assertThat(fontInfo.name()).isEqualTo("Arial");
        assertThat(fontInfo.category()).isEqualTo(FontCategory.SYSTEM);
    }

    @Test
    void shouldCreateBuiltInFontInfo() {
        // When
        FontInfo fontInfo = new FontInfo("Helvetica", FontCategory.BUILT_IN);

        // Then
        assertThat(fontInfo.name()).isEqualTo("Helvetica");
        assertThat(fontInfo.category()).isEqualTo(FontCategory.BUILT_IN);
    }

    @Test
    void shouldCompareByCategory_BuiltInBeforeSystem() {
        // Given
        FontInfo builtIn = new FontInfo("Zebra", FontCategory.BUILT_IN);
        FontInfo system = new FontInfo("Arial", FontCategory.SYSTEM);

        // When
        int result = builtIn.compareTo(system);

        // Then - BUILT_IN should come before SYSTEM regardless of name
        assertThat(result).isLessThan(0);
    }

    @Test
    void shouldCompareByNameWithinSameCategory() {
        // Given
        FontInfo arial = new FontInfo("Arial", FontCategory.SYSTEM);
        FontInfo verdana = new FontInfo("Verdana", FontCategory.SYSTEM);

        // When
        int result = arial.compareTo(verdana);

        // Then - Arial should come before Verdana alphabetically
        assertThat(result).isLessThan(0);
    }

    @Test
    void shouldCompareCaseInsensitivelyByName() {
        // Given
        FontInfo lower = new FontInfo("arial", FontCategory.SYSTEM);
        FontInfo upper = new FontInfo("ARIAL", FontCategory.SYSTEM);

        // When
        int result = lower.compareTo(upper);

        // Then - case insensitive comparison should be equal
        assertThat(result).isEqualTo(0);
    }

    @Test
    void shouldSortCorrectlyInList() {
        // Given
        List<FontInfo> fonts = new ArrayList<>();
        fonts.add(new FontInfo("Zebra", FontCategory.SYSTEM));
        fonts.add(new FontInfo("Arial", FontCategory.SYSTEM));
        fonts.add(new FontInfo("Times New Roman", FontCategory.BUILT_IN));
        fonts.add(new FontInfo("Helvetica", FontCategory.BUILT_IN));
        fonts.add(new FontInfo("Courier", FontCategory.BUILT_IN));

        // When
        Collections.sort(fonts);

        // Then - built-in fonts first (alphabetically), then system fonts (alphabetically)
        assertThat(fonts.get(0).name()).isEqualTo("Courier");
        assertThat(fonts.get(0).category()).isEqualTo(FontCategory.BUILT_IN);
        assertThat(fonts.get(1).name()).isEqualTo("Helvetica");
        assertThat(fonts.get(1).category()).isEqualTo(FontCategory.BUILT_IN);
        assertThat(fonts.get(2).name()).isEqualTo("Times New Roman");
        assertThat(fonts.get(2).category()).isEqualTo(FontCategory.BUILT_IN);
        assertThat(fonts.get(3).name()).isEqualTo("Arial");
        assertThat(fonts.get(3).category()).isEqualTo(FontCategory.SYSTEM);
        assertThat(fonts.get(4).name()).isEqualTo("Zebra");
        assertThat(fonts.get(4).category()).isEqualTo(FontCategory.SYSTEM);
    }

    @Test
    void shouldBeEqualWhenSameNameAndCategory() {
        // Given
        FontInfo font1 = new FontInfo("Arial", FontCategory.SYSTEM);
        FontInfo font2 = new FontInfo("Arial", FontCategory.SYSTEM);

        // Then - records are equal when all fields match
        assertThat(font1).isEqualTo(font2);
        assertThat(font1.hashCode()).isEqualTo(font2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentCategory() {
        // Given
        FontInfo builtIn = new FontInfo("Arial", FontCategory.BUILT_IN);
        FontInfo system = new FontInfo("Arial", FontCategory.SYSTEM);

        // Then
        assertThat(builtIn).isNotEqualTo(system);
    }
}
