package me.namila.project.text_render;

import me.namila.project.text_render.cli.RenderCommand;
import me.namila.project.text_render.config.AppConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import picocli.CommandLine;

import java.util.Arrays;

/**
 * Main entry point for the BulkTextRenderer application.
 */
public class BulkTextRendererApp {

    public static void main(String[] args) {
        // Configure logging level BEFORE Spring context initialization
        // SLF4J Simple Logger reads properties only once at startup
        configureLoggingFromArgs(args);
        
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        RenderCommand command = context.getBean(RenderCommand.class);
        int exitCode = new CommandLine(command).execute(args);
        System.exit(exitCode);
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
