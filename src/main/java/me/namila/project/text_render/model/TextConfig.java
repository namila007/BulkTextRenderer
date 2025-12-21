package me.namila.project.text_render.model;

import java.awt.Color;

/**
 * Configuration for text rendering including position, font, and styling options.
 */
public record TextConfig(
    float x,
    float y,
    Alignment alignment,
    String fontName,
    float fontSize,
    Color color,
    FontStyle fontStyle
) {
    public static final String DEFAULT_FONT = "Times New Roman";
    public static final float DEFAULT_FONT_SIZE = 12.0f;
    public static final Color DEFAULT_COLOR = Color.BLACK;
    public static final FontStyle DEFAULT_FONT_STYLE = FontStyle.NORMAL;

    /**
     * Creates a TextConfig with default font settings.
     */
    public TextConfig(float x, float y, Alignment alignment) {
        this(x, y, alignment, DEFAULT_FONT, DEFAULT_FONT_SIZE, DEFAULT_COLOR, DEFAULT_FONT_STYLE);
    }

    /**
     * Creates a TextConfig with specified font but default color and style.
     */
    public TextConfig(float x, float y, Alignment alignment, String fontName, float fontSize) {
        this(x, y, alignment, fontName, fontSize, DEFAULT_COLOR, DEFAULT_FONT_STYLE);
    }

    /**
     * Parses a hex color string to a Color object.
     * Supports formats: #RGB, #RRGGBB (with or without # prefix)
     *
     * @param hexColor the hex color string
     * @return the parsed Color
     * @throws IllegalArgumentException if the format is invalid
     */
    public static Color parseHexColor(String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) {
            return DEFAULT_COLOR;
        }

        String hex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;

        if (hex.length() == 3) {
            // Expand shorthand #RGB to #RRGGBB
            hex = String.valueOf(hex.charAt(0)) + hex.charAt(0) +
                  hex.charAt(1) + hex.charAt(1) +
                  hex.charAt(2) + hex.charAt(2);
        }

        if (hex.length() != 6 || !hex.matches("[0-9A-Fa-f]+")) {
            throw new IllegalArgumentException(
                "Invalid hex color format: '" + hexColor + "'. Expected format: #RRGGBB or #RGB");
        }

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        return new Color(r, g, b);
    }
}
