package me.namila.project.text_render.cli;

import me.namila.project.text_render.model.MeasurementUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MeasurementUnitConverter Tests")
class MeasurementUnitConverterTest {

    private MeasurementUnitConverter converter;

    @BeforeEach
    void setUp() {
        converter = new MeasurementUnitConverter();
    }

    @ParameterizedTest
    @ValueSource(strings = {"px", "PX", "Px", "pX"})
    @DisplayName("Should parse pixel unit case-insensitively")
    void shouldParsePixelUnitCaseInsensitively(String input) throws Exception {
        assertThat(converter.convert(input)).isEqualTo(MeasurementUnit.PX);
    }

    @ParameterizedTest
    @ValueSource(strings = {"mm", "MM", "Mm", "mM"})
    @DisplayName("Should parse millimeter unit case-insensitively")
    void shouldParseMillimeterUnitCaseInsensitively(String input) throws Exception {
        assertThat(converter.convert(input)).isEqualTo(MeasurementUnit.MM);
    }

    @Test
    @DisplayName("Should default to PX for null input")
    void shouldDefaultToPxForNull() throws Exception {
        assertThat(converter.convert(null)).isEqualTo(MeasurementUnit.PX);
    }

    @Test
    @DisplayName("Should default to PX for blank input")
    void shouldDefaultToPxForBlank() throws Exception {
        assertThat(converter.convert("   ")).isEqualTo(MeasurementUnit.PX);
        assertThat(converter.convert("")).isEqualTo(MeasurementUnit.PX);
    }

    @Test
    @DisplayName("Should handle whitespace around input")
    void shouldHandleWhitespaceAroundInput() throws Exception {
        assertThat(converter.convert("  px  ")).isEqualTo(MeasurementUnit.PX);
        assertThat(converter.convert("\tmm\n")).isEqualTo(MeasurementUnit.MM);
    }

    @Test
    @DisplayName("Should throw exception for invalid unit")
    void shouldThrowExceptionForInvalidUnit() {
        assertThatThrownBy(() -> converter.convert("inches"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid measurement unit")
            .hasMessageContaining("inches");
    }

    @ParameterizedTest
    @ValueSource(strings = {"cm", "pt", "in", "em", "rem"})
    @DisplayName("Should throw exception for unsupported units")
    void shouldThrowExceptionForUnsupportedUnits(String unit) {
        assertThatThrownBy(() -> converter.convert(unit))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid measurement unit");
    }
}
