package me.namila.project.text_render.cli;

import me.namila.project.text_render.model.Alignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link AlignmentConverter}.
 */
class AlignmentConverterTest {

    private AlignmentConverter converter;

    @BeforeEach
    void setUp() {
        converter = new AlignmentConverter();
    }

    @ParameterizedTest
    @CsvSource({
        "left, LEFT",
        "LEFT, LEFT",
        "Left, LEFT",
        "lEfT, LEFT",
        "center, CENTER",
        "CENTER, CENTER",
        "Center, CENTER",
        "right, RIGHT",
        "RIGHT, RIGHT",
        "Right, RIGHT"
    })
    @DisplayName("Should convert alignment case-insensitively")
    void shouldConvertCaseInsensitively(String input, Alignment expected) throws Exception {
        assertThat(converter.convert(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return LEFT for null input")
    void shouldReturnLeftForNull() throws Exception {
        assertThat(converter.convert(null)).isEqualTo(Alignment.LEFT);
    }

    @Test
    @DisplayName("Should return LEFT for blank input")
    void shouldReturnLeftForBlank() throws Exception {
        assertThat(converter.convert("")).isEqualTo(Alignment.LEFT);
        assertThat(converter.convert("   ")).isEqualTo(Alignment.LEFT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "middle", "top", "bottom", "justify"})
    @DisplayName("Should throw exception for invalid alignment")
    void shouldThrowForInvalidAlignment(String invalidValue) {
        assertThatThrownBy(() -> converter.convert(invalidValue))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid alignment")
            .hasMessageContaining(invalidValue);
    }

    @Test
    @DisplayName("Should handle whitespace around valid values")
    void shouldHandleWhitespace() throws Exception {
        assertThat(converter.convert("  left  ")).isEqualTo(Alignment.LEFT);
        assertThat(converter.convert("\tcenter\t")).isEqualTo(Alignment.CENTER);
        assertThat(converter.convert("\nright\n")).isEqualTo(Alignment.RIGHT);
    }
}
