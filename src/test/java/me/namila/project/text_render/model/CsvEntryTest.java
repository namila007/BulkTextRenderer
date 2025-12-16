package me.namila.project.text_render.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CsvEntryTest {

    @Test
    void shouldCreateEntryWithAllFields() {
        // When
        CsvEntry entry = new CsvEntry("Adam Smith", "Mr.", "Jr.");

        // Then
        assertThat(entry.name()).isEqualTo("Adam Smith");
        assertThat(entry.prefix()).isEqualTo("Mr.");
        assertThat(entry.postfix()).isEqualTo("Jr.");
    }

    @Test
    void shouldCreateEntryWithNameOnly() {
        // When
        CsvEntry entry = new CsvEntry("Adam Smith", null, null);

        // Then
        assertThat(entry.name()).isEqualTo("Adam Smith");
        assertThat(entry.prefix()).isNull();
        assertThat(entry.postfix()).isNull();
    }

    @Test
    void shouldGetDisplayTextWithPrefixOnly() {
        // Given
        CsvEntry entry = new CsvEntry("Adam Smith", "Mr.", null);

        // When
        String displayText = entry.getDisplayText();

        // Then
        assertThat(displayText).isEqualTo("Mr. Adam Smith");
    }

    @Test
    void shouldGetDisplayTextWithPostfixOnly() {
        // Given
        CsvEntry entry = new CsvEntry("John Williams", null, "Jr.");

        // When
        String displayText = entry.getDisplayText();

        // Then
        assertThat(displayText).isEqualTo("John Williams Jr.");
    }

    @Test
    void shouldGetDisplayTextWithBothPrefixAndPostfix() {
        // Given
        CsvEntry entry = new CsvEntry("Jane Doe", "Dr.", "PhD");

        // When
        String displayText = entry.getDisplayText();

        // Then
        assertThat(displayText).isEqualTo("Dr. Jane Doe PhD");
    }

    @Test
    void shouldGetDisplayTextWithNameOnly() {
        // Given
        CsvEntry entry = new CsvEntry("Adam Smith", null, null);

        // When
        String displayText = entry.getDisplayText();

        // Then
        assertThat(displayText).isEqualTo("Adam Smith");
    }

    @Test
    void shouldHandleEmptyPrefix() {
        // Given
        CsvEntry entry = new CsvEntry("Adam Smith", "", null);

        // When
        String displayText = entry.getDisplayText();

        // Then
        assertThat(displayText).isEqualTo("Adam Smith");
    }

    @Test
    void shouldHandleEmptyPostfix() {
        // Given
        CsvEntry entry = new CsvEntry("Adam Smith", "Mr.", "");

        // When
        String displayText = entry.getDisplayText();

        // Then
        assertThat(displayText).isEqualTo("Mr. Adam Smith");
    }

    @Test
    void shouldHandleBlankPrefix() {
        // Given
        CsvEntry entry = new CsvEntry("Adam Smith", "   ", null);

        // When
        String displayText = entry.getDisplayText();

        // Then
        assertThat(displayText).isEqualTo("Adam Smith");
    }

    @Test
    void shouldHandleBlankPostfix() {
        // Given
        CsvEntry entry = new CsvEntry("Adam Smith", "Mr.", "   ");

        // When
        String displayText = entry.getDisplayText();

        // Then
        assertThat(displayText).isEqualTo("Mr. Adam Smith");
    }

    @Test
    void shouldTrimWhitespaceInDisplayText() {
        // Given
        CsvEntry entry = new CsvEntry("  Adam Smith  ", "  Mr.  ", "  Jr.  ");

        // When
        String displayText = entry.getDisplayText();

        // Then
        assertThat(displayText).isEqualTo("Mr. Adam Smith Jr.");
    }

    @ParameterizedTest
    @CsvSource({
        "Adam Smith, Mr., '', Mr. Adam Smith",
        "Jane Doe, Dr., PhD, Dr. Jane Doe PhD",
        "John Williams, '', Jr., John Williams Jr.",
        "Simple Name, '', '', Simple Name"
    })
    void shouldAssembleDisplayTextCorrectly(String name, String prefix, String postfix, String expected) {
        // Given
        String actualPrefix = prefix.isEmpty() ? null : prefix;
        String actualPostfix = postfix.isEmpty() ? null : postfix;
        CsvEntry entry = new CsvEntry(name, actualPrefix, actualPostfix);

        // When
        String displayText = entry.getDisplayText();

        // Then
        assertThat(displayText).isEqualTo(expected);
    }
}
