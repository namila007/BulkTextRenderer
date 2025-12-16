package me.namila.project.text_render.model;

/**
 * Represents a single entry from a CSV file containing name with optional prefix and postfix.
 * 
 * @param name the main name/text content (required)
 * @param prefix optional prefix to prepend (e.g., "Mr.", "Dr.")
 * @param postfix optional postfix to append (e.g., "Jr.", "PhD")
 */
public record CsvEntry(String name, String prefix, String postfix) {

    /**
     * Assembles the display text by combining prefix, name, and postfix.
     * Format: {@code <prefix> <name> <postfix>}
     * 
     * <p>Empty or blank prefix/postfix are omitted. All parts are trimmed.</p>
     * 
     * <p>Examples:</p>
     * <ul>
     *   <li>name="Adam Smith", prefix="Mr.", postfix=null → "Mr. Adam Smith"</li>
     *   <li>name="Jane Doe", prefix="Dr.", postfix="PhD" → "Dr. Jane Doe PhD"</li>
     *   <li>name="John Williams", prefix=null, postfix="Jr." → "John Williams Jr."</li>
     * </ul>
     * 
     * @return the assembled display text
     */
    public String getDisplayText() {
        StringBuilder sb = new StringBuilder();
        
        // Add prefix if present and not blank
        if (prefix != null && !prefix.isBlank()) {
            sb.append(prefix.trim()).append(" ");
        }
        
        // Add name (always present, trimmed)
        sb.append(name.trim());
        
        // Add postfix if present and not blank
        if (postfix != null && !postfix.isBlank()) {
            sb.append(" ").append(postfix.trim());
        }
        
        return sb.toString();
    }
}
