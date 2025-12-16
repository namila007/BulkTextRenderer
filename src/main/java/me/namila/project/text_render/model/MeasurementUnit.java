package me.namila.project.text_render.model;

/**
 * Enum representing measurement units for coordinate values.
 * 
 * <p>Supported units:
 * <ul>
 *   <li>{@link #PX} - Pixels (default)</li>
 *   <li>{@link #MM} - Millimeters (converted at 72 DPI)</li>
 * </ul>
 * </p>
 */
public enum MeasurementUnit {
    /**
     * Pixel measurement (default).
     * No conversion applied - values are used as-is.
     */
    PX(1.0f),
    
    /**
     * Millimeter measurement.
     * Converted to pixels at 72 DPI (1mm = 2.835 pixels).
     * Formula: 72 points/inch ÷ 25.4 mm/inch = 2.835 points/mm
     */
    MM(2.835f);
    
    private final float pixelMultiplier;
    
    MeasurementUnit(float pixelMultiplier) {
        this.pixelMultiplier = pixelMultiplier;
    }
    
    /**
     * Converts a value in this unit to pixels.
     *
     * @param value the value in this measurement unit
     * @return the equivalent value in pixels
     */
    public float toPixels(float value) {
        return value * pixelMultiplier;
    }
    
    /**
     * Gets the pixel multiplier for this unit.
     *
     * @return the multiplier to convert to pixels
     */
    public float getPixelMultiplier() {
        return pixelMultiplier;
    }
}
