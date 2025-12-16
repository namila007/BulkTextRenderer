package me.namila.project.text_render.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("MeasurementUnit Tests")
class MeasurementUnitTest {

    @Test
    @DisplayName("PX should have multiplier of 1.0")
    void pxShouldHaveMultiplierOfOne() {
        assertThat(MeasurementUnit.PX.getPixelMultiplier()).isEqualTo(1.0f);
    }

    @Test
    @DisplayName("MM should have multiplier of 2.835")
    void mmShouldHaveCorrectMultiplier() {
        assertThat(MeasurementUnit.MM.getPixelMultiplier()).isCloseTo(2.835f, within(0.001f));
    }

    @Test
    @DisplayName("PX toPixels should return same value")
    void pxToPixelsShouldReturnSameValue() {
        float value = 100.0f;
        assertThat(MeasurementUnit.PX.toPixels(value)).isEqualTo(100.0f);
    }

    @ParameterizedTest
    @CsvSource({
        "1.0, 2.835",
        "10.0, 28.35",
        "100.0, 283.5",
        "25.4, 71.97"  // 1 inch = 25.4mm = ~72 points
    })
    @DisplayName("MM toPixels should convert correctly")
    void mmToPixelsShouldConvertCorrectly(float mm, float expectedPixels) {
        assertThat(MeasurementUnit.MM.toPixels(mm)).isCloseTo(expectedPixels, within(0.1f));
    }

    @Test
    @DisplayName("Zero value should remain zero for both units")
    void zeroValueShouldRemainZero() {
        assertThat(MeasurementUnit.PX.toPixels(0.0f)).isEqualTo(0.0f);
        assertThat(MeasurementUnit.MM.toPixels(0.0f)).isEqualTo(0.0f);
    }

    @Test
    @DisplayName("Negative values should convert correctly")
    void negativeValuesShouldConvert() {
        assertThat(MeasurementUnit.PX.toPixels(-50.0f)).isEqualTo(-50.0f);
        assertThat(MeasurementUnit.MM.toPixels(-10.0f)).isCloseTo(-28.35f, within(0.1f));
    }
}
