package me.namila.project.text_render.cli;

import me.namila.project.text_render.model.Alignment;
import picocli.CommandLine.ITypeConverter;

/**
 * Picocli type converter for {@link Alignment} enum.
 * Converts string input to Alignment in a case-insensitive manner.
 * 
 * <p>Examples:
 * <ul>
 *   <li>"left" or "LEFT" or "Left" → Alignment.LEFT</li>
 *   <li>"center" or "CENTER" → Alignment.CENTER</li>
 *   <li>"right" or "RIGHT" → Alignment.RIGHT</li>
 * </ul>
 * </p>
 * 
 * <p>Resolves issue #38: Make alignment option case-insensitive</p>
 */
public class AlignmentConverter implements ITypeConverter<Alignment> {
    
    @Override
    public Alignment convert(String value) throws Exception {
        if (value == null || value.isBlank()) {
            return Alignment.LEFT; // Default to left alignment
        }
        
        try {
            return Alignment.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Invalid alignment: '%s'. Valid values: left, center, right", value));
        }
    }
}
