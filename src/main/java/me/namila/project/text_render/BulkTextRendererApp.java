package me.namila.project.text_render;

import me.namila.project.text_render.cli.RenderCommand;
import me.namila.project.text_render.config.AppConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import picocli.CommandLine;

/**
 * Main entry point for the BulkTextRenderer application.
 */
public class BulkTextRendererApp {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        RenderCommand command = context.getBean(RenderCommand.class);
        int exitCode = new CommandLine(command).execute(args);
        System.exit(exitCode);
    }
}
