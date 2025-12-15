package me.namila.project.text_render.util;

import java.nio.file.Path;

/**
 * Utility class for generating output filenames with configurable prefix and postfix.
 * 
 * Pattern: <prefix>-<base-template-name>-<csv_text_first_part>-<postfix>.<format>
 */
public class OutputFileNameGenerator {

    private static final int MAX_TEXT_LENGTH = 50;

    private OutputFileNameGenerator() {
        // Utility class - prevent instantiation
    }

    /**
     * Generates an output filename based on template, text content, and optional prefix/postfix.
     *
     * @param templatePath path to the template file
     * @param text         text content from CSV (first part will be extracted)
     * @param prefix       optional prefix (null to skip)
     * @param postfix      optional postfix (null to skip)
     * @param format       output format (pdf, png)
     * @return generated filename
     */
    public static String generate(String templatePath, String text, String prefix, String postfix, String format) {
        StringBuilder sb = new StringBuilder();

        // Add prefix if not null/empty
        if (prefix != null && !prefix.isBlank()) {
            sb.append(prefix).append("-");
        }

        // Add base template name
        String baseName = extractBaseName(templatePath);
        sb.append(baseName).append("-");

        // Add sanitized first part of text
        String firstPart = extractFirstPart(text);
        String sanitizedText = sanitizeText(firstPart);
        sb.append(sanitizedText);

        // Add postfix if not null/empty
        if (postfix != null && !postfix.isBlank()) {
            sb.append("-").append(postfix);
        }

        // Add format extension
        sb.append(".").append(format.toLowerCase());

        return sb.toString();
    }

    /**
     * Extracts the base name from a template path (filename without extension).
     */
    private static String extractBaseName(String templatePath) {
        String fileName = Path.of(templatePath).getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    /**
     * Extracts the first word/part from the text (splits by whitespace, comma, semicolon).
     */
    private static String extractFirstPart(String text) {
        if (text == null || text.isBlank()) {
            return "unnamed";
        }
        // Get first word (split by space, comma, semicolon, etc.)
        String firstPart = text.split("[\\s,;]+")[0];
        if (firstPart.length() > MAX_TEXT_LENGTH) {
            return firstPart.substring(0, MAX_TEXT_LENGTH);
        }
        return firstPart;
    }

    /**
     * Sanitizes text by replacing invalid filename characters with underscores.
     */
    private static String sanitizeText(String text) {
        // Replace invalid filename characters with underscore
        return text.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
