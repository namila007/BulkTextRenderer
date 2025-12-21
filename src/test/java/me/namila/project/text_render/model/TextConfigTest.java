package me.namila.project.text_render.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.Color;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TextConfig Tests")
class TextConfigTest {

    @Nested
    @DisplayName("Default Values")
    class DefaultValues {

        @Test
        @DisplayName("DEFAULT_FONT should be Times New Roman")
        void defaultFontShouldBeTimesNewRoman() {
            assertThat(TextConfig.DEFAULT_FONT).isEqualTo("Times New Roman");
        }

        @Test
        @DisplayName("DEFAULT_FONT_SIZE should be 12.0")
        void defaultFontSizeShouldBeTwelve() {
            assertThat(TextConfig.DEFAULT_FONT_SIZE).isEqualTo(12.0f);
        }

        @Test
        @DisplayName("DEFAULT_COLOR should be BLACK")
        void defaultColorShouldBeBlack() {
            assertThat(TextConfig.DEFAULT_COLOR).isEqualTo(Color.BLACK);
        }

        @Test
        @DisplayName("DEFAULT_FONT_STYLE should be NORMAL")
        void defaultFontStyleShouldBeNormal() {
            assertThat(TextConfig.DEFAULT_FONT_STYLE).isEqualTo(FontStyle.NORMAL);
        }
    }

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        @DisplayName("Minimal constructor should use default font settings")
        void minimalConstructorShouldUseDefaults() {
            TextConfig config = new TextConfig(100f, 200f, Alignment.LEFT);

            assertThat(config.x()).isEqualTo(100f);
            assertThat(config.y()).isEqualTo(200f);
            assertThat(config.alignment()).isEqualTo(Alignment.LEFT);
            assertThat(config.fontName()).isEqualTo(TextConfig.DEFAULT_FONT);
            assertThat(config.fontSize()).isEqualTo(TextConfig.DEFAULT_FONT_SIZE);
            assertThat(config.color()).isEqualTo(TextConfig.DEFAULT_COLOR);
            assertThat(config.fontStyle()).isEqualTo(TextConfig.DEFAULT_FONT_STYLE);
        }

        @Test
        @DisplayName("Font constructor should use default color and style")
        void fontConstructorShouldUseDefaultColorAndStyle() {
            TextConfig config = new TextConfig(100f, 200f, Alignment.CENTER, "Arial", 24f);

            assertThat(config.fontName()).isEqualTo("Arial");
            assertThat(config.fontSize()).isEqualTo(24f);
            assertThat(config.color()).isEqualTo(TextConfig.DEFAULT_COLOR);
            assertThat(config.fontStyle()).isEqualTo(TextConfig.DEFAULT_FONT_STYLE);
        }

        @Test
        @DisplayName("Full constructor should set all fields")
        void fullConstructorShouldSetAllFields() {
            Color customColor = new Color(255, 0, 0);
            TextConfig config = new TextConfig(50f, 150f, Alignment.RIGHT, "Helvetica", 18f, customColor, FontStyle.BOLD);

            assertThat(config.x()).isEqualTo(50f);
            assertThat(config.y()).isEqualTo(150f);
            assertThat(config.alignment()).isEqualTo(Alignment.RIGHT);
            assertThat(config.fontName()).isEqualTo("Helvetica");
            assertThat(config.fontSize()).isEqualTo(18f);
            assertThat(config.color()).isEqualTo(customColor);
            assertThat(config.fontStyle()).isEqualTo(FontStyle.BOLD);
        }
    }

    @Nested
    @DisplayName("parseHexColor Method")
    class ParseHexColorMethod {

        @Nested
        @DisplayName("Valid Hex Colors")
        class ValidHexColors {

            @ParameterizedTest
            @CsvSource({
                "#FF0000, 255, 0, 0",
                "#00FF00, 0, 255, 0",
                "#0000FF, 0, 0, 255",
                "#FFFFFF, 255, 255, 255",
                "#000000, 0, 0, 0",
                "#123456, 18, 52, 86"
            })
            @DisplayName("Should parse 6-digit hex colors with hash")
            void shouldParseSixDigitHexWithHash(String hex, int r, int g, int b) {
                Color color = TextConfig.parseHexColor(hex);
                assertThat(color.getRed()).isEqualTo(r);
                assertThat(color.getGreen()).isEqualTo(g);
                assertThat(color.getBlue()).isEqualTo(b);
            }

            @ParameterizedTest
            @CsvSource({
                "FF0000, 255, 0, 0",
                "00FF00, 0, 255, 0",
                "0000FF, 0, 0, 255"
            })
            @DisplayName("Should parse 6-digit hex colors without hash")
            void shouldParseSixDigitHexWithoutHash(String hex, int r, int g, int b) {
                Color color = TextConfig.parseHexColor(hex);
                assertThat(color.getRed()).isEqualTo(r);
                assertThat(color.getGreen()).isEqualTo(g);
                assertThat(color.getBlue()).isEqualTo(b);
            }

            @ParameterizedTest
            @CsvSource({
                "#F00, 255, 0, 0",
                "#0F0, 0, 255, 0",
                "#00F, 0, 0, 255",
                "#FFF, 255, 255, 255",
                "#000, 0, 0, 0",
                "#ABC, 170, 187, 204"
            })
            @DisplayName("Should parse 3-digit shorthand hex colors")
            void shouldParseThreeDigitShorthand(String hex, int r, int g, int b) {
                Color color = TextConfig.parseHexColor(hex);
                assertThat(color.getRed()).isEqualTo(r);
                assertThat(color.getGreen()).isEqualTo(g);
                assertThat(color.getBlue()).isEqualTo(b);
            }

            @ParameterizedTest
            @CsvSource({
                "#ff0000, 255, 0, 0",
                "#Ff0000, 255, 0, 0",
                "aabbcc, 170, 187, 204"
            })
            @DisplayName("Should handle case insensitive hex values")
            void shouldHandleCaseInsensitive(String hex, int r, int g, int b) {
                Color color = TextConfig.parseHexColor(hex);
                assertThat(color.getRed()).isEqualTo(r);
                assertThat(color.getGreen()).isEqualTo(g);
                assertThat(color.getBlue()).isEqualTo(b);
            }
        }

        @Nested
        @DisplayName("Default Color for Empty/Null")
        class DefaultColorForEmptyNull {

            @ParameterizedTest
            @NullAndEmptySource
            @DisplayName("Should return default color for null or empty input")
            void shouldReturnDefaultForNullOrEmpty(String input) {
                Color color = TextConfig.parseHexColor(input);
                assertThat(color).isEqualTo(TextConfig.DEFAULT_COLOR);
            }
        }

        @Nested
        @DisplayName("Invalid Hex Colors")
        class InvalidHexColors {

            @ParameterizedTest
            @ValueSource(strings = {
                "#GG0000",      // Invalid character
                "#GGGGGG",      // All invalid characters
                "#12345",       // 5 digits
                "#1234567",     // 7 digits
                "#12",          // 2 digits
                "#1",           // 1 digit
                "red",          // Named color
                "rgb(255,0,0)", // RGB format
                "##FF0000",     // Double hash
                "#FF 0000"      // Space in middle
            })
            @DisplayName("Should throw exception for invalid hex format")
            void shouldThrowForInvalidFormat(String invalidHex) {
                assertThatThrownBy(() -> TextConfig.parseHexColor(invalidHex))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid hex color format");
            }
        }
    }

    @Nested
    @DisplayName("Record Equality")
    class RecordEquality {

        @Test
        @DisplayName("Equal configs should be equal")
        void equalConfigsShouldBeEqual() {
            TextConfig config1 = new TextConfig(100f, 200f, Alignment.LEFT, "Arial", 12f, Color.RED, FontStyle.BOLD);
            TextConfig config2 = new TextConfig(100f, 200f, Alignment.LEFT, "Arial", 12f, Color.RED, FontStyle.BOLD);

            assertThat(config1).isEqualTo(config2);
            assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        }

        @Test
        @DisplayName("Different color configs should not be equal")
        void differentColorConfigsShouldNotBeEqual() {
            TextConfig config1 = new TextConfig(100f, 200f, Alignment.LEFT, "Arial", 12f, Color.RED, FontStyle.NORMAL);
            TextConfig config2 = new TextConfig(100f, 200f, Alignment.LEFT, "Arial", 12f, Color.BLUE, FontStyle.NORMAL);

            assertThat(config1).isNotEqualTo(config2);
        }

        @Test
        @DisplayName("Different style configs should not be equal")
        void differentStyleConfigsShouldNotBeEqual() {
            TextConfig config1 = new TextConfig(100f, 200f, Alignment.LEFT, "Arial", 12f, Color.BLACK, FontStyle.NORMAL);
            TextConfig config2 = new TextConfig(100f, 200f, Alignment.LEFT, "Arial", 12f, Color.BLACK, FontStyle.BOLD);

            assertThat(config1).isNotEqualTo(config2);
        }
    }
}
