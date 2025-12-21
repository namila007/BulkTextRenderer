package me.namila.project.text_render.cli;

import me.namila.project.text_render.model.Alignment;
import me.namila.project.text_render.model.CsvEntry;
import me.namila.project.text_render.model.FontStyle;
import me.namila.project.text_render.model.MeasurementUnit;
import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.model.TextConfig;
import me.namila.project.text_render.service.CsvReaderService;
import me.namila.project.text_render.service.FontService;
import me.namila.project.text_render.service.JpegRendererService;
import me.namila.project.text_render.service.ParallelExecutorService;
import me.namila.project.text_render.service.PdfRendererService;
import me.namila.project.text_render.service.PngRendererService;
import me.namila.project.text_render.service.RendererService;
import me.namila.project.text_render.util.OutputFileNameGenerator;
import me.namila.project.text_render.util.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.awt.Color;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * CLI command for bulk text rendering on PDF/PNG/JPEG templates.
 */
@Command(
    name = "bulk-render",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Bulk render text onto PDF, PNG, or JPEG templates using data from a CSV file."
)
public class RenderCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(RenderCommand.class);

    private final CsvReaderService csvReaderService;
    private final PdfRendererService pdfRendererService;
    private final PngRendererService pngRendererService;
    private final JpegRendererService jpegRendererService;
    private final ParallelExecutorService parallelExecutorService;
    private final FontService fontService;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Option(names = {"-t", "--template"}, 
            description = "Template file path (PDF, PNG, JPG, or JPEG)")
    private Path templatePath;

    @Option(names = {"-c", "--csv"}, 
            description = "CSV file path containing text entries")
    private Path csvPath;

    @Option(names = {"-o", "--output"}, defaultValue = "./output", 
            description = "Output folder (default: ${DEFAULT-VALUE})")
    private Path outputFolder;

    @Option(names = {"--x"}, 
            description = "X coordinate for text placement")
    private Float x;

    @Option(names = {"--y"}, 
            description = "Y coordinate for text placement")
    private Float y;

    @Option(names = {"-u", "--unit"}, 
            defaultValue = "PX",
            converter = MeasurementUnitConverter.class,
            description = "Measurement unit for coordinates: PX (pixels), MM (millimeters). Default: ${DEFAULT-VALUE}")
    private MeasurementUnit unit;

    @Option(names = {"-a", "--align"}, defaultValue = "LEFT", 
            description = "Text alignment: LEFT, CENTER, RIGHT (default: ${DEFAULT-VALUE})")
    private Alignment alignment;

    @Option(names = {"-f", "--font"}, defaultValue = "Times New Roman", 
            description = "Font name (default: ${DEFAULT-VALUE})")
    private String fontName;

    @Option(names = {"-s", "--font-size"}, defaultValue = "12", 
            description = "Font size (default: ${DEFAULT-VALUE})")
    private float fontSize;

    @Option(names = {"-C", "--color"}, defaultValue = "#000000",
            description = "Font color in hex format (e.g., #FF0000, #000). Default: ${DEFAULT-VALUE}")
    private String fontColor;

    @Option(names = {"-b", "--bold"},
            description = "Use bold font style")
    private boolean bold;

    @Option(names = {"-i", "--italic"},
            description = "Use italic font style")
    private boolean italic;

    @Option(names = {"-p", "--threads"}, 
            description = "Number of parallel threads (default: available processors)")
    private Integer parallelism;

    @Option(names = {"--sequential-threshold"},
            description = "Jobs below this count are processed sequentially on main thread " +
                         "(default: 10). Set to 0 to always use parallel processing.")
    private Integer sequentialThreshold;

    @Option(names = {"--prefix"}, 
            description = "Output filename prefix")
    private String prefix;

    @Option(names = {"--postfix"}, 
            description = "Output filename postfix")
    private String postfix;

    @Option(names = {"--list-fonts"}, 
            description = "List available fonts for PDF, PNG, and JPEG rendering and exit")
    private boolean listFonts;

    @Option(names = {"-v", "--verbose"}, 
            description = "Enable verbose logging (INFO level)")
    private boolean verbose;

    @Option(names = {"--debug"}, 
            description = "Enable debug logging (DEBUG level)")
    private boolean debug;

    public RenderCommand(CsvReaderService csvReaderService,
                        PdfRendererService pdfRendererService,
                        PngRendererService pngRendererService,
                        JpegRendererService jpegRendererService,
                        ParallelExecutorService parallelExecutorService,
                        FontService fontService) {
        this.csvReaderService = csvReaderService;
        this.pdfRendererService = pdfRendererService;
        this.pngRendererService = pngRendererService;
        this.jpegRendererService = jpegRendererService;
        this.parallelExecutorService = parallelExecutorService;
        this.fontService = fontService;
    }

    @Override
    public Integer call() {
        try {
            // Log startup message if verbose/debug enabled
            // Note: Actual log level configuration is done in BulkTextRendererApp.main()
            // before Spring context initialization
            if (debug) {
                logger.debug("Debug logging enabled");
            } else if (verbose) {
                logger.info("Verbose logging enabled");
            }
            
            // Handle --list-fonts option
            if (listFonts) {
                listAvailableFonts();
                return 0;
            }

            // Validate required options for rendering
            if (!validateRequiredOptions()) {
                return 2;
            }

            // Validate files exist
            if (!validateFiles()) {
                return 1;
            }

            // Create output directory if it doesn't exist
            Files.createDirectories(outputFolder);
            logger.debug("Output directory created/verified: {}", outputFolder.toAbsolutePath());

            // Read CSV entries (supports multi-column: name,prefix,postfix)
            List<CsvEntry> entries = csvReaderService.readEntries(csvPath);
            if (entries.isEmpty()) {
                logger.warn("No entries found in CSV file.");
                spec.commandLine().getOut().println("No entries found in CSV file.");
                return 0;
            }

            // Determine renderer based on template extension
            RendererService renderer = selectRenderer();
            logger.debug("Selected renderer: {}", renderer.getClass().getSimpleName());

            // Convert coordinates from specified unit to pixels
            float xPixels = unit.toPixels(x);
            float yPixels = unit.toPixels(y);
            logger.debug("Coordinates converted: ({}, {}) {} -> ({}, {}) px", x, y, unit, xPixels, yPixels);

            // Parse font color and style
            Color color = TextConfig.parseHexColor(fontColor);
            FontStyle fontStyle = FontStyle.fromFlags(bold, italic);
            logger.debug("Font styling: color={}, style={}", fontColor, fontStyle);

            // Build render jobs
            TextConfig textConfig = new TextConfig(xPixels, yPixels, alignment, fontName, fontSize, color, fontStyle);
            String extension = getFileExtension(templatePath);
            List<RenderJob> jobs = entries.stream()
                .map(entry -> createRenderJob(entry, textConfig, extension))
                .toList();

            // Determine threading strategy
            int threshold = getSequentialThreshold();
            String threadingMode = jobs.size() < threshold ? "sequential" : "parallel";
            
            logger.info("Processing {} entries ({} mode, {} threads)", 
                       jobs.size(), threadingMode, getParallelism());
            spec.commandLine().getOut().printf("Processing %d entries (%s mode)...%n", 
                             jobs.size(), threadingMode);
            
            ProgressTracker tracker = new ProgressTracker(jobs.size());
            parallelExecutorService.executeAll(jobs, renderer, getParallelism(), tracker, threshold);

            logger.info("Completed! Output saved to: {}", outputFolder.toAbsolutePath());
            spec.commandLine().getOut().printf("Completed! Output files saved to: %s%n", outputFolder.toAbsolutePath());
            return 0;

        } catch (Exception e) {
            logger.error("Error during rendering: {}", e.getMessage(), e);
            spec.commandLine().getErr().printf("Error: %s%n", e.getMessage());
            return 1;
        }
    }

    private boolean validateRequiredOptions() {
        PrintWriter err = spec.commandLine().getErr();
        boolean valid = true;

        if (templatePath == null) {
            err.println("Missing required option: '--template=<templatePath>'");
            valid = false;
        }
        if (csvPath == null) {
            err.println("Missing required option: '--csv=<csvPath>'");
            valid = false;
        }
        if (x == null) {
            err.println("Missing required option: '--x=<x>'");
            valid = false;
        }
        if (y == null) {
            err.println("Missing required option: '--y=<y>'");
            valid = false;
        }

        return valid;
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
        if (!extension.equals("pdf") && !extension.equals("png") && 
            !extension.equals("jpg") && !extension.equals("jpeg")) {
            err.println("Unsupported template format. Use PDF, PNG, JPG, or JPEG: " + templatePath);
            return false;
        }

        return true;
    }

    private RendererService selectRenderer() {
        String extension = getFileExtension(templatePath).toLowerCase();
        return switch (extension) {
            case "pdf" -> pdfRendererService;
            case "png" -> pngRendererService;
            case "jpg", "jpeg" -> jpegRendererService;
            default -> throw new IllegalArgumentException("Unsupported format: " + extension);
        };
    }

    private RenderJob createRenderJob(CsvEntry entry, TextConfig textConfig, String extension) {
        // Use clean name for filename (no prefix stripping needed)
        String outputFilename = OutputFileNameGenerator.generateFromCleanName(
            templatePath.toString(), entry.name(), prefix, postfix, extension);
        Path outputPath = outputFolder.resolve(outputFilename);
        // Display text combines prefix + name + postfix
        String displayText = entry.getDisplayText();
        return new RenderJob(displayText, textConfig, templatePath, outputPath);
    }

    private String getFileExtension(Path path) {
        String filename = path.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1) : "";
    }

    private void listAvailableFonts() {
        fontService.registerSystemFonts();
        PrintWriter out = spec.commandLine().getOut();
        
        out.println("=== Available Fonts ===");
        out.println();
        
        var fonts = fontService.getUnifiedAvailableFonts();
        Set<String> pdfRegistered = fontService.getAvailablePdfFonts();
        
        // Built-in fonts (work for all formats)
        out.println("[Built-in - PDF, PNG, JPEG]");
        fonts.stream()
            .filter(f -> f.category() == me.namila.project.text_render.model.FontCategory.BUILT_IN)
            .forEach(f -> out.println("  " + f.name()));
        
        // PDF-registered system fonts
        out.println();
        out.println("[System Fonts - PDF, PNG, JPEG]");
        fonts.stream()
            .filter(f -> f.category() == me.namila.project.text_render.model.FontCategory.SYSTEM)
            .filter(f -> pdfRegistered.stream().anyMatch(p -> p.equalsIgnoreCase(f.name())))
            .forEach(f -> out.println("  " + f.name()));
        
        // PNG/JPEG-only fonts
        out.println();
        out.println("[System Fonts - PNG, JPEG only]");
        out.println("(These fonts are not registered for PDF embedding and will fall back to Times Roman)");
        fonts.stream()
            .filter(f -> f.category() == me.namila.project.text_render.model.FontCategory.SYSTEM)
            .filter(f -> pdfRegistered.stream().noneMatch(p -> p.equalsIgnoreCase(f.name())))
            .forEach(f -> out.println("  " + f.name()));
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
        return x != null ? x : 0f;
    }

    public float getY() {
        return y != null ? y : 0f;
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

    public int getSequentialThreshold() {
        return sequentialThreshold != null ? sequentialThreshold : ParallelExecutorService.DEFAULT_SEQUENTIAL_THRESHOLD;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPostfix() {
        return postfix;
    }

    public boolean isListFonts() {
        return listFonts;
    }
}
