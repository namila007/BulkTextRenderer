package me.namila.project.text_render.model;

import java.nio.file.Path;

public record RenderJob(
    String text,
    TextConfig textConfig,
    Path templatePath,
    Path outputPath
) {}
