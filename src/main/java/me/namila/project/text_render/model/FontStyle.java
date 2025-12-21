package me.namila.project.text_render.model;

import java.awt.Font;

/**
 * Represents font style options for text rendering.
 */
public enum FontStyle {
    NORMAL(Font.PLAIN),
    BOLD(Font.BOLD),
    ITALIC(Font.ITALIC),
    BOLD_ITALIC(Font.BOLD | Font.ITALIC);

    private final int awtStyle;

    FontStyle(int awtStyle) {
        this.awtStyle = awtStyle;
    }

    /**
     * Gets the AWT Font style constant for this font style.
     * Used for PNG/JPEG rendering with java.awt.Font.
     *
     * @return the AWT Font style constant
     */
    public int getAwtStyle() {
        return awtStyle;
    }

    /**
     * Determines the font style based on bold and italic flags.
     *
     * @param bold   true if bold style is requested
     * @param italic true if italic style is requested
     * @return the corresponding FontStyle
     */
    public static FontStyle fromFlags(boolean bold, boolean italic) {
        if (bold && italic) {
            return BOLD_ITALIC;
        } else if (bold) {
            return BOLD;
        } else if (italic) {
            return ITALIC;
        } else {
            return NORMAL;
        }
    }

    /**
     * Checks if this style includes bold.
     *
     * @return true if bold
     */
    public boolean isBold() {
        return this == BOLD || this == BOLD_ITALIC;
    }

    /**
     * Checks if this style includes italic.
     *
     * @return true if italic
     */
    public boolean isItalic() {
        return this == ITALIC || this == BOLD_ITALIC;
    }
}
