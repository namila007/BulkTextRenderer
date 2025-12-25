# BulkTextRenderer - AI Coding Instructions

## Project Overview

A Java 24 CLI tool for bulk rendering text onto PDF/PNG/JPEG templates from CSV data. Uses **Spring Context for DI** (not Spring Boot), **Picocli for CLI**, and **OpenPDF for PDF manipulation**. Supports both JAR execution and GraalVM native-image compilation.

## Architecture

### Package Structure (`me.namila.project.text_render`)
```
├── BulkTextRendererApp.java   # Entry point, native-image initialization
├── cli/                       # Picocli commands and converters
│   └── RenderCommand.java     # Main CLI command with all options
├── config/
│   └── AppConfig.java         # Spring @Configuration (manual bean definitions)
├── model/                     # Records: RenderJob, TextConfig, CsvEntry, enums
├── service/                   # Business logic
│   ├── RendererService.java   # Interface for all renderers
│   ├── PdfRendererService     # OpenPDF-based PDF rendering
│   ├── AbstractImageRendererService  # Template Method for PNG/JPEG
│   ├── PngRendererService / JpegRendererService  # Image renderers
│   ├── ParallelExecutorService       # Virtual threads + adaptive threading
│   └── CsvReaderService / FontService
└── util/                      # NativeImageUtil, ProgressTracker, OutputFileNameGenerator
```

### Key Patterns

1. **Template Method Pattern**: `AbstractImageRendererService` defines rendering workflow; `PngRendererService`/`JpegRendererService` override format-specific hooks.

2. **Adaptive Threading**: Jobs < 10 → sequential; ≥10 → virtual threads with semaphore-controlled parallelism. See `ParallelExecutorService`.

3. **Coordinate System**: Unified top-left origin (0,0) for all formats. PDF Y-coordinates are transformed internally from top-left to PDF's native bottom-left.

4. **Records for Data**: Use Java records for immutable data (`RenderJob`, `TextConfig`, `CsvEntry`, `FontInfo`).

## Build & Test Commands

```bash
./gradlew build              # Build + run tests
./gradlew test               # Tests only (JUnit 5)
./gradlew jar                # Fat JAR → build/libs/
./gradlew nativeCompile      # Native executable → build/native/nativeCompile/
./gradlew runWithAgent -PappArgs="-t,template.pdf,..." # Collect native-image metadata
```

### Running Locally
```bash
# JAR mode (full format support)
java -jar build/libs/BulkTextRenderer-*.jar -t template.pdf -c names.csv --x=100 --y=200

# Native executable
./build/native/nativeCompile/bulkTextRenderer -t template.pdf -c names.csv --x=100 --y=200
```

## GraalVM Native Image

### Configuration Files (`src/main/resources/META-INF/native-image/`)
- `reflect-config.json` – Reflection metadata for Spring, Picocli, OpenPDF
- `resource-config.json` – Bundled resources (fonts, configs)
- `jni-config.json` – JNI calls for AWT
- `reachability-metadata.json` – Library-specific metadata

### Platform Limitations
- **macOS Native**: PDF only (AWT not supported in native-image)
- **Linux/Windows Native**: Full support (PDF, PNG, JPEG)
- **JAR mode**: Full support on all platforms

### Collecting Metadata
When adding new reflection-dependent code:
```bash
./gradlew runWithAgent -PappArgs="<comma-separated-args>"
```
This merges metadata into `src/main/resources/META-INF/native-image/`.

## Testing Conventions

- **Unit tests**: `src/test/java/me/namila/.../` mirror main structure
- **Integration tests**: `EndToEndTest.java` – full CLI-to-output workflow
- **Test resources**: `src/test/resources/` – CSV files, template generators
- **Assertions**: Use AssertJ (`assertThat(...)`)
- **Mocking**: Mockito for service isolation
- **Coverage**: JaCoCo with 70% minimum threshold

### Test Template Generation
`TestResourceGenerator.java` creates PDF/PNG/JPEG templates on-demand for tests.

## Adding New Features

### New Renderer Format
1. Create `XxxRendererService extends AbstractImageRendererService`
2. Implement `getFormatName()` and `getImageFormat()`
3. Add bean to `AppConfig.java`
4. Add to `RenderCommand.getRendererForExtension()`

### New CLI Option
1. Add `@Option` field in `RenderCommand.java`
2. If converter needed, create in `cli/` package
3. Update `TextConfig` or `RenderJob` record if needed

### New Model
Use Java records with validation in constructors. Example:
```java
public record NewConfig(String value) {
    public NewConfig {
        Objects.requireNonNull(value, "value must not be null");
    }
}
```

## Code Style

- **Logging**: SLF4J with `logger.debug/info/warn/error`
- **Nullability**: Validate in constructors, fail fast
- **Exceptions**: Descriptive messages mentioning native-image workarounds where applicable
- **Javadoc**: Required on all public classes and methods (service interfaces especially)
