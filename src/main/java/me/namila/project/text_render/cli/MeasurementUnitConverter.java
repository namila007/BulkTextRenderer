package me.namila.project.text_render.cli;

import me.namila.project.text_render.model.MeasurementUnit;
import picocli.CommandLine.ITypeConverter;

/**
 * Picocli type converter for {@link MeasurementUnit} enum.
 * Converts string input to MeasurementUnit in a case-insensitive manner.
 * 
 * <p>Examples:
 * <ul>
 *   <li>"px" or "PX" → MeasurementUnit.PX</li>
 *   <li>"mm" or "MM" → MeasurementUnit.MM</li>
 * </ul>
 * </p>
 */
public class MeasurementUnitConverter implements ITypeConverter<MeasurementUnit> {
    
    @Override
    public MeasurementUnit convert(String value) throws Exception {
        if (value == null || value.isBlank()) {
            return MeasurementUnit.PX; // Default to pixels
        }
        
        try {
            return MeasurementUnit.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Invalid measurement unit: '%s'. Valid values: px, mm", value));
        }
    }
}
