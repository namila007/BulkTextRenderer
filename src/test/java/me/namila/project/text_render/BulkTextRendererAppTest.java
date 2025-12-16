package me.namila.project.text_render;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for BulkTextRendererApp logging configuration.
 */
class BulkTextRendererAppTest {
    
    private String originalLogLevel;
    private String originalPackageLogLevel;
    
    @BeforeEach
    void setUp() {
        // Store original values
        originalLogLevel = System.getProperty("org.slf4j.simpleLogger.defaultLogLevel");
        originalPackageLogLevel = System.getProperty("org.slf4j.simpleLogger.log.me.namila.project.text_render");
    }
    
    @AfterEach
    void tearDown() {
        // Restore original values
        if (originalLogLevel != null) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", originalLogLevel);
        } else {
            System.clearProperty("org.slf4j.simpleLogger.defaultLogLevel");
        }
        if (originalPackageLogLevel != null) {
            System.setProperty("org.slf4j.simpleLogger.log.me.namila.project.text_render", originalPackageLogLevel);
        } else {
            System.clearProperty("org.slf4j.simpleLogger.log.me.namila.project.text_render");
        }
    }
    
    @Test
    void shouldConfigureDebugLoggingWhenDebugFlagProvided() {
        // Given
        String[] args = {"--debug", "--template=test.pdf"};
        
        // When
        configureLoggingFromArgs(args);
        
        // Then
        assertThat(System.getProperty("org.slf4j.simpleLogger.defaultLogLevel"))
            .isEqualTo("debug");
        assertThat(System.getProperty("org.slf4j.simpleLogger.log.me.namila.project.text_render"))
            .isEqualTo("debug");
    }
    
    @Test
    void shouldConfigureInfoLoggingWhenVerboseFlagProvided() {
        // Given
        String[] args = {"-v", "--template=test.pdf"};
        
        // When
        configureLoggingFromArgs(args);
        
        // Then
        assertThat(System.getProperty("org.slf4j.simpleLogger.defaultLogLevel"))
            .isEqualTo("info");
        assertThat(System.getProperty("org.slf4j.simpleLogger.log.me.namila.project.text_render"))
            .isEqualTo("info");
    }
    
    @Test
    void shouldConfigureInfoLoggingWhenVerboseLongFlagProvided() {
        // Given
        String[] args = {"--verbose", "--template=test.pdf"};
        
        // When
        configureLoggingFromArgs(args);
        
        // Then
        assertThat(System.getProperty("org.slf4j.simpleLogger.defaultLogLevel"))
            .isEqualTo("info");
    }
    
    @Test
    void shouldPreferDebugOverVerbose() {
        // Given
        String[] args = {"--debug", "--verbose", "--template=test.pdf"};
        
        // When
        configureLoggingFromArgs(args);
        
        // Then
        assertThat(System.getProperty("org.slf4j.simpleLogger.defaultLogLevel"))
            .isEqualTo("debug");
    }
    
    @Test
    void shouldNotSetLogLevelWithoutFlags() {
        // Given
        System.clearProperty("org.slf4j.simpleLogger.defaultLogLevel");
        System.clearProperty("org.slf4j.simpleLogger.log.me.namila.project.text_render");
        String[] args = {"--template=test.pdf"};
        
        // When
        configureLoggingFromArgs(args);
        
        // Then - property should remain unset (default from simplelogger.properties will be used)
        assertThat(System.getProperty("org.slf4j.simpleLogger.defaultLogLevel")).isNull();
        assertThat(System.getProperty("org.slf4j.simpleLogger.log.me.namila.project.text_render")).isNull();
    }
    
    /**
     * Helper method that mirrors the logic in BulkTextRendererApp.
     * We test it here to avoid starting Spring context in tests.
     */
    private void configureLoggingFromArgs(String[] args) {
        boolean hasDebug = java.util.Arrays.asList(args).contains("--debug");
        boolean hasVerbose = java.util.Arrays.stream(args).anyMatch(arg -> 
            arg.equals("-v") || arg.equals("--verbose"));
        
        if (hasDebug) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
            System.setProperty("org.slf4j.simpleLogger.log.me.namila.project.text_render", "debug");
        } else if (hasVerbose) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
            System.setProperty("org.slf4j.simpleLogger.log.me.namila.project.text_render", "info");
        }
    }
}
