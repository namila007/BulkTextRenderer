package me.namila.project.text_render;

import me.namila.project.text_render.cli.RenderCommand;
import me.namila.project.text_render.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import picocli.CommandLine;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Main entry point for the BulkTextRenderer application.
 */
public class BulkTextRendererApp {
    private static Logger logger;

    public static void main(String[] args) {
        // Configure logging level BEFORE anything else so logger picks it up
        configureLoggingFromArgs(args);
        logger = LoggerFactory.getLogger(BulkTextRendererApp.class);
        
        // Configure java.home for native-image font support
        initializeNativeImageSupport();
        
        // Ensure AWT runs in non-headless mode to match native-image configuration
        System.setProperty("java.awt.headless", "false");
        
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        RenderCommand command = context.getBean(RenderCommand.class);
        int exitCode = new CommandLine(command).execute(args);
        System.exit(exitCode);
    }
    
    /**
     * Initializes support for GraalVM native-image execution.
     * Sets java.home to locate fontconfig files for FontConfiguration.
     * FontConfiguration needs java.home to find fontconfig.bfc which contains font mappings.
     * 
     * Priority order:
     * 1. Already set java.home (user override)
     * 2. Bundled fontconfig in executable's lib directory (self-contained)
     * 3. JAVA_HOME environment variable (fallback to external JDK)
     */
    private static void initializeNativeImageSupport() {
        logger.debug("Initializing native image support...");
        
        // Skip if already set
        if (System.getProperty("java.home") != null) {
            logger.debug("java.home already set to: {}", System.getProperty("java.home"));
            return;
        }
        
        // Try to find bundled fontconfig relative to executable
        String executablePath = findExecutablePath();
        logger.debug("Detected executable path: {}", executablePath);
        
        if (executablePath != null) {
            File execDir = new File(executablePath).getParentFile();
            File bundledFontConfig = new File(execDir, "lib/fontconfig.bfc");
            
            logger.debug("Checking for bundled fontconfig at: {}", bundledFontConfig.getAbsolutePath());
            
            if (bundledFontConfig.exists()) {
                // Set java.home to executable directory (fontconfig.bfc is in lib subdirectory)
                System.setProperty("java.home", execDir.getAbsolutePath());
                logger.debug("Set java.home to bundled location: {}", execDir.getAbsolutePath());
                return;
            } else {
                logger.debug("Bundled fontconfig not found at: {}", bundledFontConfig.getAbsolutePath());
            }
        }
        
        // Fallback: Try JAVA_HOME environment variable
        String javaHome = System.getenv("JAVA_HOME");
        logger.debug("Falling back to JAVA_HOME environment variable: {}", javaHome);
        
        if (javaHome != null && !javaHome.isEmpty()) {
            File fontConfig = new File(javaHome, "lib/fontconfig.bfc");
            logger.debug("Checking for fontconfig in JAVA_HOME at: {}", fontConfig.getAbsolutePath());
            if (fontConfig.exists()) {
                System.setProperty("java.home", javaHome);
                logger.debug("Set java.home to JAVA_HOME: {}", javaHome);
            } else {
                logger.debug("fontconfig.bfc not found in JAVA_HOME/lib");
            }
        } else {
            logger.debug("JAVA_HOME environment variable is not set");
        }
    }
    
    /**
     * Attempts to find the path to the currently running executable.
     * Works for both native-image executables and JAR files.
     */
    private static String findExecutablePath() {
        try {
            // Get the location of this class
            Path classPath = Path.of(BulkTextRendererApp.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
            String path = classPath.toAbsolutePath().toString();
            logger.debug("Code source location: {}", path);
            return path;
        } catch (URISyntaxException | SecurityException | NullPointerException e) {
            logger.debug("Failed to get code source location: {}", e.getMessage());
        }
        
        // Alternative: Use system property that GraalVM might set
        String processPath = System.getProperty("sun.java.command");
        logger.debug("sun.java.command: {}", processPath);
        
        if (processPath != null && !processPath.isEmpty()) {
            // For native-image, this might be the executable path
            String[] parts = processPath.split(" ");
            if (parts.length > 0) {
                File f = new File(parts[0]);
                if (f.exists()) {
                    return f.getAbsolutePath();
                }
            }
        }
        
        return null;
    }
    
    /**
     * Pre-configures logging level based on CLI arguments before Spring context starts.
     * This is necessary because SLF4J Simple Logger reads properties only at initialization.
     */
    private static void configureLoggingFromArgs(String[] args) {
        boolean hasDebug = Arrays.asList(args).contains("--debug");
        boolean hasVerbose = Arrays.stream(args).anyMatch(arg -> 
            arg.equals("-v") || arg.equals("--verbose"));
        
        if (hasDebug) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
            // Also set package-specific level to override simplelogger.properties
            System.setProperty("org.slf4j.simpleLogger.log.me.namila.project.text_render", "debug");
        } else if (hasVerbose) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
            System.setProperty("org.slf4j.simpleLogger.log.me.namila.project.text_render", "info");
        }
    }
}
