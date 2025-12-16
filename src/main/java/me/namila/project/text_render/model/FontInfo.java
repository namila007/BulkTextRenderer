package me.namila.project.text_render.model;

/**
 * Record representing font information for display in font listing.
 *
 * @param name     the name of the font
 * @param category the category of the font (BUILT_IN or SYSTEM)
 */
public record FontInfo(String name, FontCategory category) implements Comparable<FontInfo> {
    
    @Override
    public int compareTo(FontInfo other) {
        // Sort by category first (BUILT_IN before SYSTEM), then by name
        int categoryComparison = this.category.compareTo(other.category);
        if (categoryComparison != 0) {
            return categoryComparison;
        }
        return this.name.compareToIgnoreCase(other.name);
    }
}
