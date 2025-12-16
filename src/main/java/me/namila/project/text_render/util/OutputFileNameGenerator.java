package me.namila.project.text_render.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Utility class for generating output filenames with configurable prefix and postfix.
 * 
 * Pattern: <prefix>-<base-template-name>-<csv_text(max 20chars)>-<postfix>.<format>
 * 
 * Text processing:
 * - Removes name prefixes (Mr., Mrs., Miss, Ms., Dr., Prof., etc.)
 * - Replaces spaces with underscores
 * - Removes non-alphanumeric characters (except underscore)
 * - Truncates to max 20 characters
 */
public class OutputFileNameGenerator {

    private static final Logger logger = LoggerFactory.getLogger(OutputFileNameGenerator.class);
    
    private static final int MAX_TEXT_LENGTH = 20;
    
    /**
     * Pattern to match common name prefixes.
     * Matches: Mr., Mrs., Miss, Ms., Dr., Prof. (case-insensitive, with optional period)
     */
    private static final Pattern NAME_PREFIX_PATTERN = Pattern.compile(
        "^(mr\\.?|mrs\\.?|miss\\.?|ms\\.?|dr\\.?|prof\\.?)\\s+",
        Pattern.CASE_INSENSITIVE
    );

    private OutputFileNameGenerator() {
        // Utility class - prevent instantiation
    }

    /**
     * Generates an output filename based on template, text content, and optional prefix/postfix.
     *
     * @param templatePath path to the template file
     * @param text         text content from CSV
     * @param prefix       optional prefix (null to skip)
     * @param postfix      optional postfix (null to skip)
     * @param format       output format (pdf, png, jpg, jpeg)
     * @return generated filename
     */
    public static String generate(String templatePath, String text, String prefix, String postfix, String format) {
        logger.debug("Generating filename for text: '{}' with prefix='{}', postfix='{}'", text, prefix, postfix);
        
        StringBuilder sb = new StringBuilder();

        // Add prefix if not null/empty
        if (prefix != null && !prefix.isBlank()) {
            sb.append(prefix).append("-");
        }

        // Add base template name
        String baseName = extractBaseName(templatePath);
        sb.append(baseName).append("-");

        // Process and add sanitized text
        String processedText = processText(text);
        sb.append(processedText);

        // Add postfix if not null/empty
        if (postfix != null && !postfix.isBlank()) {
            sb.append("-").append(postfix);
        }

        // Add format extension
        sb.append(".").append(format.toLowerCase());

        String result = sb.toString();
        logger.debug("Generated filename: '{}'", result);
        return result;
    }

    /**
     * Processes text for use in filename:
     * 1. Removes name prefixes (Mr., Mrs., etc.)
     * 2. Replaces spaces with underscores
     * 3. Removes non-alphanumeric characters (except underscore)
     * 4. Truncates to max 20 characters
     *
     * @param text the input text
     * @return processed text suitable for filename
     */
    static String processText(String text) {
        if (text == null || text.isBlank()) {
            return "unnamed";
        }
        
        String processed = text.trim();
        
        // Step 1: Remove name prefixes
        processed = removeNamePrefixes(processed);
        
        // Step 2: Replace spaces with underscores
        processed = processed.replaceAll("\\s+", "_");
        
        // Step 3: Remove non-alphanumeric characters (except underscore)
        processed = processed.replaceAll("[^a-zA-Z0-9_]", "");
        
        // Step 4: Truncate to max length
        if (processed.length() > MAX_TEXT_LENGTH) {
            processed = processed.substring(0, MAX_TEXT_LENGTH);
        }
        
        // Handle edge case where all characters were removed
        if (processed.isEmpty()) {
            return "unnamed";
        }
        
        return processed;
    }
    
    /**
     * Removes common name prefixes from text.
     * Supports: Mr., Mrs., Miss, Ms., Dr., Prof. (case-insensitive)
     *
     * @param text the input text
     * @return text with name prefix removed
     */
    static String removeNamePrefixes(String text) {
        return NAME_PREFIX_PATTERN.matcher(text).replaceFirst("");
    }

    /**
     * Extracts the base name from a template path (filename without extension).
     */
    private static String extractBaseName(String templatePath) {
        String fileName = Path.of(templatePath).getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
}
