# BulkTextRenderer Implementation Plan

> **Project:** Java PDF/PNG Text Insertion CLI Tool  
> **Approach:** TDD (Test-Driven Development)  
> **Principles:** SOLID, No Over-engineering, Human-readable code  
> **Tracking:** All progress recorded in `IMPLEMENTATION_PROGRESS.md`

---

## 📋 Overview

This plan breaks the implementation into 6 phases, each designed for subagent execution. Each phase follows TDD (write tests first, then implementation) and outputs progress to the tracking document.

---

## 📁 Project Structure (Target)

```
src/main/java/me/namila/project/text_render/
├── BulkTextRendererApp.java          # Main entry point
├── cli/
│   └── RenderCommand.java            # Picocli CLI command
├── config/
│   └── AppConfig.java                # Spring configuration
├── model/
│   ├── TextConfig.java               # Text position, alignment, font config
│   ├── RenderJob.java                # Single render job (text + config)
│   └── Alignment.java                # Enum: LEFT, CENTER, RIGHT
├── service/
│   ├── CsvReaderService.java         # CSV file parsing
│   ├── PdfRendererService.java       # PDF text insertion (OpenPDF)
│   ├── PngRendererService.java       # PNG text insertion (AWT)
│   ├── RendererService.java          # Interface for renderers
│   └── ParallelExecutorService.java  # Virtual thread executor
└── util/
    └── ProgressTracker.java          # Progress bar utility

src/test/java/me/namila/project/text_render/
├── service/
│   ├── CsvReaderServiceTest.java
│   ├── PdfRendererServiceTest.java
│   ├── PngRendererServiceTest.java
│   └── ParallelExecutorServiceTest.java
└── integration/
    └── EndToEndTest.java

src/test/resources/
├── sample.csv
├── template.pdf
└── template.png
```

---

## 🔧 Phase 0: Project Setup

**Subagent Task:** Configure build and create base structure

### Tasks
1. **Update `build.gradle.kts`** with:
   - Java 24 toolchain
   - Spring Context 6.1.x (DI only, no Spring Boot)
   - Picocli 4.7.x with annotation processor
   - OpenPDF 2.0.x
   - AssertJ and Mockito for testing
   - Application plugin with main class
   - slf4j for logging
   - Fat JAR configuration

2. **Create base package directories**

3. **Create `IMPLEMENTATION_PROGRESS.md`** tracking document

### Context7 MCP Usage
- **Use Context7 for latest Picocli 4.x** — search `picocli` for annotations and patterns
- **Use Context7 for OpenPDF 2.x** — search `openpdf` for API documentation

### Deliverables
- Updated `build.gradle.kts`
- Empty package structure created
- `IMPLEMENTATION_PROGRESS.md` initialized

---

## 🧪 Phase 1: Core Models & CSV Reader (TDD)

**Subagent Task:** Create domain models and CSV parsing

### Test First (Write These Tests)

```java
// CsvReaderServiceTest.java
@Test void shouldReadSingleLineFromCsv()
@Test void shouldReadMultipleLinesFromCsv()
@Test void shouldHandleEmptyFile()
@Test void shouldTrimWhitespaceFromLines()
@Test void shouldSkipEmptyLines()
```

### Implementation Tasks
1. **`Alignment.java`** — Enum with LEFT, CENTER, RIGHT
2. **`TextConfig.java`** — Record with x, y, alignment, fontName, fontSize
3. **`RenderJob.java`** — Record with text, textConfig, templatePath, outputPath
4. **`CsvReaderService.java`** — Reads CSV, returns `List<String>`

### SOLID Focus
- **S**ingle Responsibility: CsvReaderService only reads CSVs
- Keep models as simple records (immutable)

### Acceptance Criteria
- [ ] All tests pass
- [ ] Models are immutable Java records
- [ ] Update `IMPLEMENTATION_PROGRESS.md`

---

## 🖨️ Phase 2: PDF Renderer Service (TDD)

**Subagent Task:** Implement PDF text insertion using OpenPDF

### Test First

```java
// PdfRendererServiceTest.java
@Test void shouldInsertTextAtLeftAlignment()
@Test void shouldInsertTextAtCenterAlignment()
@Test void shouldInsertTextAtRightAlignment()
@Test void shouldUseSpecifiedFont()
@Test void shouldUseDefaultFontWhenNotSpecified()
@Test void shouldCreateOutputPdfFile()
```

### Implementation Tasks
1. **`RendererService.java`** — Interface with `void render(RenderJob job)`
2. **`PdfRendererService.java`** — Implements RendererService for PDFs
   - Load template PDF using OpenPDF PdfReader
   - Calculate text position based on alignment
   - Insert text using PdfContentByte
   - Save to output path with PdfStamper

### Context7 MCP Usage (CRITICAL)
- **Use Context7 to fetch OpenPDF documentation** — search `openpdf` for:
  - `PdfReader` and `PdfStamper` usage
  - `BaseFont` creation for custom fonts
  - `PdfContentByte.showTextAligned()` for alignment
  - Coordinate system (PDF origin is bottom-left)

### Key Logic: Alignment Calculation
```java
// LEFT: text starts at x
// CENTER: text centered around x
// RIGHT: text ends at x
float adjustedX = switch (alignment) {
    case LEFT -> x;
    case CENTER -> x; // use Element.ALIGN_CENTER
    case RIGHT -> x;  // use Element.ALIGN_RIGHT
};
```

### Acceptance Criteria
- [ ] All tests pass with sample PDF
- [ ] Alignment works correctly
- [ ] Custom fonts supported
- [ ] Update `IMPLEMENTATION_PROGRESS.md`

---

## 🖼️ Phase 3: PNG Renderer Service (TDD)

**Subagent Task:** Implement PNG text insertion using Java AWT

### Test First

```java
// PngRendererServiceTest.java
@Test void shouldInsertTextAtLeftAlignment()
@Test void shouldInsertTextAtCenterAlignment()
@Test void shouldInsertTextAtRightAlignment()
@Test void shouldUseSpecifiedFont()
@Test void shouldCreateOutputPngFile()
@Test void shouldPreserveTemplateImageQuality()
```

### Implementation Tasks
1. **`PngRendererService.java`** — Implements RendererService for PNGs
   - Load template using `ImageIO.read()`
   - Create `Graphics2D` from BufferedImage
   - Apply font and calculate alignment
   - Draw text using `drawString()`
   - Save using `ImageIO.write()`

### Context7 MCP Usage
- **Use Context7 for Java Graphics2D** — search `java graphics2d` for:
  - Font metrics for text width calculation
  - Anti-aliasing settings for quality
  - RenderingHints best practices

### Key Logic: Text Width Calculation
```java
FontMetrics fm = g2d.getFontMetrics();
int textWidth = fm.stringWidth(text);

int adjustedX = switch (alignment) {
    case LEFT -> x;
    case CENTER -> x - (textWidth / 2);
    case RIGHT -> x - textWidth;
};
```

### Acceptance Criteria
- [ ] All tests pass with sample PNG
- [ ] Image quality preserved
- [ ] Update `IMPLEMENTATION_PROGRESS.md`

---

## ⚡ Phase 4: Parallel Execution Engine (TDD)

**Subagent Task:** Build virtual thread executor with progress tracking

### Test First

```java
// ParallelExecutorServiceTest.java
@Test void shouldExecuteJobsInParallel()
@Test void shouldRespectMaxThreadLimit()
@Test void shouldReportProgressAccurately()
@Test void shouldUseVirtualThreads()
@Test void shouldHandleFailedJobsGracefully()
```

### Implementation Tasks
1. **`ProgressTracker.java`** — Thread-safe progress counter with console output
   - Atomic counter for completed jobs
   - Callback for progress updates
   - Console progress bar rendering

2. **`ParallelExecutorService.java`**
   - Accept list of `RenderJob` and appropriate `RendererService`
   - Use `Executors.newVirtualThreadPerTaskExecutor()` for unlimited parallelism
   - Or use Semaphore to limit concurrent tasks when user specifies thread count
   - Report progress via `ProgressTracker`

### Context7 MCP Usage (CRITICAL)
- **Use Context7 for Java 22 Virtual Threads** — search `java virtual threads` for:
  - `Thread.ofVirtual().factory()`
  - `ExecutorService` with virtual threads
  - Best practices for virtual thread executors

### Key Pattern: Virtual Thread Executor with Limit
```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
Semaphore semaphore = new Semaphore(maxParallelism);

for (RenderJob job : jobs) {
    semaphore.acquire();
    executor.submit(() -> {
        try {
            renderer.render(job);
            progressTracker.increment();
        } finally {
            semaphore.release();
        }
    });
}
```

### Progress Bar Output Format
```
Processing: [████████████████░░░░] 80% (80/100)
```

### Acceptance Criteria
- [ ] Parallel execution works
- [ ] Virtual threads confirmed (not platform threads)
- [ ] Progress tracking accurate
- [ ] Update `IMPLEMENTATION_PROGRESS.md`

---

## 🖥️ Phase 5: CLI Integration

**Subagent Task:** Wire everything with Picocli and Spring Context

### Test First

```java
// RenderCommandTest.java
@Test void shouldParseAllRequiredArguments()
@Test void shouldUseDefaultOutputFolder()
@Test void shouldUseDefaultParallelism()
@Test void shouldValidateTemplateFileExists()
@Test void shouldValidateCsvFileExists()
```

### Implementation Tasks

1. **`AppConfig.java`** — Spring @Configuration for bean definitions
   ```java
   @Configuration
   public class AppConfig {
       @Bean CsvReaderService csvReaderService() { ... }
       @Bean PdfRendererService pdfRendererService() { ... }
       @Bean PngRendererService pngRendererService() { ... }
       @Bean ParallelExecutorService parallelExecutorService() { ... }
   }
   ```

2. **`RenderCommand.java`** — Picocli @Command class with options:
   ```
   --template, -t    : Template PDF/PNG path (required)
   --csv, -c         : CSV file path(s) (required, multiple allowed)
   --output, -o      : Output folder (default: ./output)
   --x               : X coordinate (required)
   --y               : Y coordinate (required)
   --align, -a       : Alignment LEFT|CENTER|RIGHT (default: LEFT)
   --font, -f        : Font name (default: Times New Roman)
   --font-size, -s   : Font size (default: 12)
   --threads, -p     : Parallelism count (default: CPU cores)
   ```

3. **`BulkTextRendererApp.java`** — Main class
   - Initialize Spring context
   - Create Picocli CommandLine
   - Execute and return exit code

### Context7 MCP Usage
- **Use Context7 for Picocli** — search `picocli` for:
  - `@Command`, `@Option`, `@Parameters` annotations
  - Type conversion for enums
  - Exit code handling
  - Help generation

### Acceptance Criteria
- [ ] CLI parses all arguments correctly
- [ ] Spring DI wires services correctly
- [ ] Validation errors show helpful messages
- [ ] Help command works (`--help`)
- [ ] Update `IMPLEMENTATION_PROGRESS.md`

---

## 🧪 Phase 6: Integration Tests & Packaging

**Subagent Task:** End-to-end tests and JAR packaging

### Integration Tests

```java
// EndToEndTest.java
@Test void shouldGeneratePdfsFromCsvUsingPdfTemplate()
@Test void shouldGeneratePngsFromCsvUsingPngTemplate()
@Test void shouldHandleLargeCsvFileInParallel()
@Test void shouldCreateOutputDirectoryIfNotExists()
```

### Packaging Tasks

1. **Fat JAR configuration** in `build.gradle.kts` (already done in Phase 0)

2. **Create test resources**:
   - `src/test/resources/sample.csv` — 5-10 test names
   - `src/test/resources/template.pdf` — Simple single-page PDF
   - `src/test/resources/template.png` — Simple PNG image

3. **Update README.md** with:
   - Project description
   - Build instructions
   - Usage examples
   - CLI options reference

### Final Validation Commands
```bash
./gradlew clean build
./gradlew test
java -jar build/libs/BulkTextRenderer-1.0-SNAPSHOT.jar --help
```

### Acceptance Criteria
- [ ] All integration tests pass
- [ ] `./gradlew build` produces runnable JAR
- [ ] CLI help displays correctly
- [ ] Sample execution works end-to-end
- [ ] Update `IMPLEMENTATION_PROGRESS.md` — mark COMPLETE

---

## 🎯 Subagent Prompt Template

When invoking a subagent for each phase, use this format:

```
You are implementing Phase X of the BulkTextRenderer project.

**Approach:** TDD (write tests FIRST, then implementation)
**Principles:** SOLID, no over-engineering, human-readable code
**Project Root:** /Users/namila007/namz/projects/wedding-pdf-editor/BulkTextRenderer

**Context:**
- Read `IMPLEMENTATION_PROGRESS.md` for current state
- Read `IMPLEMENTATION_PLAN.md` for phase details
- Read previous phase files if dependencies exist

**Your Tasks:**
1. [List specific tasks from phase]

**Context7 MCP Usage:**
- [List specific docs to fetch via Context7 - use resolve-library-id first, then get-library-docs]

**Output Requirements:**
1. Create all test files first
2. Implement production code to pass tests
3. Run tests with ./gradlew test
4. Update `IMPLEMENTATION_PROGRESS.md` with:
   - Status changes (⬜ → ✅)
   - File paths created
   - Any blockers or notes
   - Timestamp in execution log

**Quality Checklist:**
- [ ] All tests pass
- [ ] Code follows package structure
- [ ] No over-engineering
- [ ] Methods are focused and readable
```

---

## 📝 Notes for Main Agent

1. **Execute phases sequentially** — Each phase depends on previous
2. **Check `IMPLEMENTATION_PROGRESS.md`** before each subagent call
3. **Validate tests pass** after each phase with `./gradlew test`
4. **Context7 is critical** for Phases 2, 3, 4, 5 — always resolve library ID first
5. **Final validation**: `./gradlew build` should produce working JAR

---

## 🔗 Context7 Library References

| Library | Search Term | Usage Phase |
|---------|-------------|-------------|
| OpenPDF | `openpdf` | Phase 2 |
| Picocli | `picocli` | Phase 0, 5 |
| Java Virtual Threads | `java virtual threads` or `java 21 concurrency` | Phase 4 |
| Java Graphics2D | Standard JDK - no Context7 needed | Phase 3 |

---

## ⚠️ Potential Blockers & Mitigations

| Risk | Mitigation |
|------|------------|
| OpenPDF API changes | Use Context7 for latest docs |
| Font not found on system | Fallback to default, log warning |
| Large CSV memory issues | Process line-by-line, don't load all |
| Virtual threads not available | Fallback to platform thread pool |
