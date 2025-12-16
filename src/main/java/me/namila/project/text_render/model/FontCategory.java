package me.namila.project.text_render.model;

/**
 * Enum representing the category/source of a font.
 */
public enum FontCategory {
    /**
     * Built-in fonts that are always available without system dependencies.
     * For PDF: Helvetica, Courier, Times New Roman
     */
    BUILT_IN,
    
    /**
     * System-installed fonts from the operating system.
     */
    SYSTEM
}
