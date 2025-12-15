package me.namila.project.text_render.cli;

import me.namila.project.text_render.model.Alignment;
import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.model.TextConfig;
import me.namila.project.text_render.service.CsvReaderService;
import me.namila.project.text_render.service.ParallelExecutorService;
import me.namila.project.text_render.service.PdfRendererService;
import me.namila.project.text_render.service.PngRendererService;
import me.namila.project.text_render.service.RendererService;
import me.namila.project.text_render.util.ProgressTracker;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * CLI command for bulk text rendering on PDF/PNG templates.
 */
@Command(
    name = "bulk-render",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Bulk render text onto PDF or PNG templates using data from a CSV file."
)
public class RenderCommand implements Callable<Integer> {

    private final CsvReaderService csvReaderService;
    private final PdfRendererService pdfRendererService;
    private final PngRendererService pngRendererService;
    private final ParallelExecutorService parallelExecutorService;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Option(names = {"-t", "--template"}, required = true, 
            description = "Template file path (PDF or PNG)")
    private Path templatePath;

    @Option(names = {"-c", "--csv"}, required = true, 
            description = "CSV file path containing text entries")
    private Path csvPath;

    @Option(names = {"-o", "--output"}, defaultValue = "./output", 
            description = "Output folder (default: ${DEFAULT-VALUE})")
    private Path outputFolder;

    @Option(names = {"--x"}, required = true, 
            description = "X coordinate for text placement")
    private float x;

    @Option(names = {"--y"}, required = true, 
            description = "Y coordinate for text placement")
    private float y;

    @Option(names = {"-a", "--align"}, defaultValue = "LEFT", 
            description = "Text alignment: LEFT, CENTER, RIGHT (default: ${DEFAULT-VALUE})")
    private Alignment alignment;

    @Option(names = {"-f", "--font"}, defaultValue = "Times New Roman", 
            description = "Font name (default: ${DEFAULT-VALUE})")
    private String fontName;

    @Option(names = {"-s", "--font-size"}, defaultValue = "12", 
            description = "Font size (default: ${DEFAULT-VALUE})")
    private float fontSize;

    @Option(names = {"-p", "--threads"}, 
            description = "Number of parallel threads (default: available processors)")
    private Integer parallelism;

    public RenderCommand(CsvReaderService csvReaderService,
                        PdfRendererService pdfRendererService,
                        PngRendererService pngRendererService,
                        ParallelExecutorService parallelExecutorService) {
        this.csvReaderService = csvReaderService;
        this.pdfRendererService = pdfRendererService;
        this.pngRendererService = pngRendererService;
        this.parallelExecutorService = parallelExecutorService;
    }

    @Override
    public Integer call() {
        try {
            // Validate files exist
            if (!validateFiles()) {
                return 1;
            }

            // Create output directory if it doesn't exist
            Files.createDirectories(outputFolder);

            // Read CSV lines
            List<String> lines = csvReaderService.readLines(csvPath);
            if (lines.isEmpty()) {
                System.out.println("No entries found in CSV file.");
                return 0;
            }

            // Determine renderer based on template extension
            RendererService renderer = selectRenderer();

            // Build render jobs
            TextConfig textConfig = new TextConfig(x, y, alignment, fontName, fontSize);
            String extension = getFileExtension(templatePath);
            List<RenderJob> jobs = lines.stream()
                .map(text -> createRenderJob(text, textConfig, extension))
                .toList();

            // Execute jobs in parallel
            System.out.printf("Processing %d entries with %d threads...%n", 
                             jobs.size(), getParallelism());
            
            ProgressTracker tracker = new ProgressTracker(jobs.size());
            parallelExecutorService.executeAll(jobs, renderer, getParallelism(), tracker);

            System.out.printf("Completed! Output files saved to: %s%n", outputFolder.toAbsolutePath());
            return 0;

        } catch (Exception e) {
            System.err.printf("Error: %s%n", e.getMessage());
            return 1;
        }
    }

    private boolean validateFiles() {
        PrintWriter err = spec.commandLine().getErr();
        
        if (!Files.exists(templatePath)) {
            err.println("Template file does not exist: " + templatePath);
            return false;
        }

        if (!Files.exists(csvPath)) {
            err.println("CSV file does not exist: " + csvPath);
            return false;
        }

        String extension = getFileExtension(templatePath).toLowerCase();
        if (!extension.equals("pdf") && !extension.equals("png")) {
            err.println("Unsupported template format. Use PDF or PNG: " + templatePath);
            return false;
        }

        return true;
    }

    private RendererService selectRenderer() {
        String extension = getFileExtension(templatePath).toLowerCase();
        return switch (extension) {
            case "pdf" -> pdfRendererService;
            case "png" -> pngRendererService;
            default -> throw new IllegalArgumentException("Unsupported format: " + extension);
        };
    }

    private RenderJob createRenderJob(String text, TextConfig textConfig, String extension) {
        String sanitizedText = sanitizeFilename(text);
        Path outputPath = outputFolder.resolve(sanitizedText + "." + extension);
        return new RenderJob(text, textConfig, templatePath, outputPath);
    }

    private String getFileExtension(Path path) {
        String filename = path.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "";
    }

    private String sanitizeFilename(String text) {
        return text.replaceAll("[^a-zA-Z0-9.-]", "_")
                  .replaceAll("_+", "_")
                  .substring(0, Math.min(text.length(), 100));
    }

    // Getters for testing
    public Path getTemplatePath() {
        return templatePath;
    }

    public Path getCsvPath() {
        return csvPath;
    }

    public Path getOutputFolder() {
        return outputFolder;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public String getFontName() {
        return fontName;
    }

    public float getFontSize() {
        return fontSize;
    }

    public int getParallelism() {
        return parallelism != null ? parallelism : Runtime.getRuntime().availableProcessors();
    }
}
