package me.namila.project.text_render.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.Font;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FontStyle Tests")
class FontStyleTest {

    @Nested
    @DisplayName("AWT Style Mapping")
    class AwtStyleMapping {

        @Test
        @DisplayName("NORMAL should map to Font.PLAIN")
        void normalShouldMapToPlain() {
            assertThat(FontStyle.NORMAL.getAwtStyle()).isEqualTo(Font.PLAIN);
        }

        @Test
        @DisplayName("BOLD should map to Font.BOLD")
        void boldShouldMapToBold() {
            assertThat(FontStyle.BOLD.getAwtStyle()).isEqualTo(Font.BOLD);
        }

        @Test
        @DisplayName("ITALIC should map to Font.ITALIC")
        void italicShouldMapToItalic() {
            assertThat(FontStyle.ITALIC.getAwtStyle()).isEqualTo(Font.ITALIC);
        }

        @Test
        @DisplayName("BOLD_ITALIC should map to Font.BOLD | Font.ITALIC")
        void boldItalicShouldMapToBoldOrItalic() {
            assertThat(FontStyle.BOLD_ITALIC.getAwtStyle()).isEqualTo(Font.BOLD | Font.ITALIC);
        }
    }

    @Nested
    @DisplayName("fromFlags Factory Method")
    class FromFlagsMethod {

        @ParameterizedTest
        @CsvSource({
            "false, false, NORMAL",
            "true, false, BOLD",
            "false, true, ITALIC",
            "true, true, BOLD_ITALIC"
        })
        @DisplayName("fromFlags should return correct style")
        void fromFlagsShouldReturnCorrectStyle(boolean bold, boolean italic, FontStyle expected) {
            assertThat(FontStyle.fromFlags(bold, italic)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Enum Values")
    class EnumValues {

        @Test
        @DisplayName("Should have exactly 4 values")
        void shouldHaveFourValues() {
            assertThat(FontStyle.values()).hasSize(4);
        }

        @Test
        @DisplayName("Should contain all expected values")
        void shouldContainAllExpectedValues() {
            assertThat(FontStyle.values())
                .containsExactly(FontStyle.NORMAL, FontStyle.BOLD, FontStyle.ITALIC, FontStyle.BOLD_ITALIC);
        }
    }

    @Nested
    @DisplayName("AWT Style Values")
    class AwtStyleValues {

        @Test
        @DisplayName("AWT style values should be distinct except BOLD_ITALIC")
        void awtStyleValuesShouldBeDistinct() {
            assertThat(FontStyle.NORMAL.getAwtStyle()).isNotEqualTo(FontStyle.BOLD.getAwtStyle());
            assertThat(FontStyle.NORMAL.getAwtStyle()).isNotEqualTo(FontStyle.ITALIC.getAwtStyle());
            assertThat(FontStyle.BOLD.getAwtStyle()).isNotEqualTo(FontStyle.ITALIC.getAwtStyle());
        }

        @Test
        @DisplayName("BOLD_ITALIC AWT style should be combination of BOLD and ITALIC")
        void boldItalicShouldBeCombination() {
            int expected = FontStyle.BOLD.getAwtStyle() | FontStyle.ITALIC.getAwtStyle();
            assertThat(FontStyle.BOLD_ITALIC.getAwtStyle()).isEqualTo(expected);
        }
    }
}
