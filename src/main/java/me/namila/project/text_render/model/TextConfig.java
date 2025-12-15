package me.namila.project.text_render.model;

public record TextConfig(
    float x,
    float y,
    Alignment alignment,
    String fontName,
    float fontSize
) {
    public static final String DEFAULT_FONT = "Times New Roman";
    public static final float DEFAULT_FONT_SIZE = 12.0f;

    public TextConfig(float x, float y, Alignment alignment) {
        this(x, y, alignment, DEFAULT_FONT, DEFAULT_FONT_SIZE);
    }
}
