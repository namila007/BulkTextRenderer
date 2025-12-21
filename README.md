# BulkTextRenderer

[![GitHub Actions - Gradle](https://github.com/namila007/BulkTextRenderer/actions/workflows/build.yml/badge.svg)](https://github.com/namila007/BulkTextRenderer/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/java-24-blue.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-v8.14-green.svg)](https://gradle.org/)
[![GitHub License](https://img.shields.io/github/license/namila007/BulkTextRenderer)](https://github.com/namila007/BulkTextRenderer/blob/master/LICENSE)

A high-performance CLI tool for bulk rendering text onto PDF, PNG, and JPEG templates. Perfect for generating personalized invitations, certificates, name cards, and other documents from a CSV list of names or text entries.

Available as both a JAR (universal compatibility) and native executable (platform-specific, fast startup).

## Features

- **Multiple Format Support**: Render text on PDF, PNG, JPG, and JPEG template files
- **Bulk Processing**: Process multiple entries from a CSV file in one command
- **Multi-Column CSV**: Support for `name,prefix,postfix` format with automatic text assembly
- **Adaptive Threading**: Automatically uses sequential processing for small batches (<10 jobs), parallel for larger batches
- **Flexible Text Positioning**: Precise X/Y coordinate placement with pixel or millimeter units
- **Unified Coordinate System**: Top-left origin (0,0) for all formats
- **Text Alignment**: Left, center, or right alignment options
- **Custom Fonts**: Use system fonts or built-in PDF fonts
- **Customizable Output**: Add prefix and postfix to output filenames
- **Progress Tracking**: Real-time progress feedback during processing
- **Configurable Logging**: Verbose and debug modes for troubleshooting
- **Native Image Support**: Compile to platform-specific native executable with GraalVM

## Prerequisites

### For JAR
- **Java 24** or higher

### For Native Build
- **GraalVM 24** or higher (Oracle GraalVM recommended)
- **Gradle 8.x** (wrapper included)

## Installation

### Option 1: JAR (Recommended for PNG/JPEG)

Download the latest JAR from [Releases](https://github.com/namila007/BulkTextRenderer/releases):

```bash
java -jar BulkTextRenderer-{VERSION}.jar --help
```

**Advantages**:
- ✅ Universal - works on all platforms (macOS, Linux, Windows)
- ✅ Full format support (PDF, PNG, JPEG)
- ✅ No platform-specific dependencies

**Requirements**: Java 24+

### Option 2: Native Executable (Platform-Specific)

Download platform-specific native executable from [Releases](https://github.com/namila007/BulkTextRenderer/releases):

```bash
./BulkTextRenderer --help
```

**Advantages**:
- ✅ Fast startup time (~instant)
- ✅ Lower memory footprint
- ✅ No JRE required

**Limitations**:
- ⚠️ **macOS**: PDF only (PNG/JPEG not supported - see [#28](https://github.com/namila007/BulkTextRenderer/issues/28))
- ✅ **Linux**: Full support (PDF, PNG, JPEG)
- ✅ **Windows**: Full support (PDF, PNG, JPEG)

### Platform Compatibility Matrix

| Platform | JAR | Native Exec | PDF | PNG | JPEG |
|----------|-----|-------------|-----|-----|------|
| **macOS** | ✅ | ✅ | ✅ | ✅ (JAR) / ❌ (Native) | ✅ (JAR) / ❌ (Native) |
| **Linux** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Windows** | ✅ | ✅ | ✅ | ✅ | ✅ |

> **Note**: On macOS, use the JAR for PNG/JPEG rendering. The native executable only supports PDF due to [GraalVM AWT limitations](https://github.com/oracle/graal/issues/4124).

## Build Instructions

### Build the Project

```bash
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

### Build Fat JAR

```bash
./gradlew jar
```

The JAR file will be created at: `build/libs/BulkTextRenderer-{VERSION}.jar`

### Build Native Executable

Requires [GraalVM 24](https://www.graalvm.org/downloads/):

```bash
# Install GraalVM (via sdkman)
sdk install java 24.0.2-graal

# Build native image
./gradlew nativeCompile
```

The native executable will be at: `build/native/nativeCompile/BulkTextRenderer`

**Note**: On macOS, the native executable will only support PDF rendering (see [#28](https://github.com/namila007/BulkTextRenderer/issues/28)).


## Usage

### Basic Usage (JAR)

```bash
java -jar BulkTextRenderer-{VERSION}.jar \
  -t template.pdf \
  -c names.csv \
  -o output \
  --x 297 \
  --y 500
```

### Basic Usage (Native)

```bash
./BulkTextRenderer \
  -t template.pdf \
  -c names.csv \
  -o output \
  --x 297 \
  --y 500
```

### With All Options

```bash
java -jar BulkTextRenderer-{VERSION}.jar \
  -t template.pdf \
  -c names.csv \
  -o output \
  --x 297 \
  --y 500 \
  -u px \
  -a CENTER \
  -f Helvetica \
  -s 24 \
  -p 4 \
  --sequential-threshold 10 \
  --prefix wedding \
  --postfix final \
  --verbose
```

### List Available Fonts

```bash
java -jar BulkTextRenderer-{VERSION}.jar --list-fonts
```

### Display Help

```bash
java -jar BulkTextRenderer-{VERSION}.jar --help
```

## CLI Options Reference

| Option | Short | Description | Default |
|--------|-------|-------------|---------|
| `--template` | `-t` | Template file path (PDF, PNG, JPG, JPEG) | *required* |
| `--csv` | `-c` | CSV file path containing text entries | *required* |
| `--output` | `-o` | Output folder for generated files | `./output` |
| `--x` | | X coordinate for text placement | *required* |
| `--y` | | Y coordinate for text placement | *required* |
| `--unit` | `-u` | Measurement unit: PX (pixels) or MM (millimeters) | `PX` |
| `--align` | `-a` | Text alignment: LEFT, CENTER, RIGHT | `LEFT` |
| `--font` | `-f` | Font name | `Times New Roman` |
| `--font-size` | `-s` | Font size in points | `12` |
| `--threads` | `-p` | Number of parallel threads (for jobs ≥ threshold) | CPU cores |
| `--sequential-threshold` | | Jobs below this count use sequential processing | `10` |
| `--prefix` | | Output filename prefix | *none* |
| `--postfix` | | Output filename postfix | *none* |
| `--list-fonts` | | List available fonts and exit | |
| `--verbose` | `-v` | Enable verbose logging (INFO level) | |
| `--debug` | | Enable debug logging (DEBUG level) | |
| `--help` | `-h` | Display help message | |
| `--version` | `-V` | Display version information | |

## CSV File Format

### Simple Format (Single Column)

The CSV file can contain one text entry per line:

```csv
John Doe
Jane Smith
Robert Brown
```

### Multi-Column Format (Recommended)

For more control, use the multi-column format with `name,prefix,postfix`:

```csv
name,prefix,postfix
Adam Smith,Mr.,
Jane Doe,Dr.,PhD
John Williams,,Jr.
```

The display text is assembled as: `<prefix> <name> <postfix>`
- `Mr. Adam Smith`
- `Dr. Jane Doe PhD`
- `John Williams Jr.`

The filename uses only the `name` column (cleaner filenames without title prefixes).

**Note**: 
- Header row (`name,prefix,postfix`) is automatically detected and skipped
- Empty prefix/postfix columns are supported
- Single-column CSV still works for backward compatibility

## Coordinate System

Both PDF and PNG templates use a **unified coordinate system** with **top-left origin**:

- Origin (0,0) is at the **top-left** corner
- Y values increase **downward**
- X values increase **rightward**

### Measurement Units

| Unit | Description | Conversion |
|------|-------------|------------|
| `px` | Pixels (default) | 1px = 1px |
| `mm` | Millimeters | 1mm ≈ 2.835px (at 72 DPI) |

**Example with millimeters**:
```bash
--x 50 --y 100 --unit mm
```

### Common Page Dimensions

| Format | Width | Height |
|--------|-------|--------|
| A4 PDF | 595 pts (210mm) | 842 pts (297mm) |
| Letter PDF | 612 pts | 792 pts |
| PNG/JPEG | Varies by image |

**Example**: To place text 100 points from the left and 50 points from the top:
```bash
--x 100 --y 50
```

## Available Fonts

Use `--list-fonts` to see all available fonts categorized as:

### Built-in Fonts
Fonts that work without any system dependencies:
- Helvetica
- Courier
- Times New Roman

### System Fonts
Any font installed on your operating system (Arial, Verdana, etc.)

## Examples

### Wedding Invitations (PDF)

```bash
java -jar BulkTextRenderer-{VERSION}.jar \
  -t wedding-template.pdf \
  -c guests.csv \
  -o invitations \
  --x 297 \
  --y 500 \
  -a CENTER \
  -f Helvetica \
  -s 28 \
  --prefix invite \
  --postfix 2024
```

### Name Badges (PNG) - JAR Mode

```bash
java -jar BulkTextRenderer-{VERSION}.jar \
  -t badge-template.png \
  -c attendees.csv \
  -o badges \
  --x 200 \
  --y 150 \
  -a CENTER \
  -f SansSerif \
  -s 32
```

### Certificates (PDF) - Native Mode

```bash
./BulkTextRenderer \
  -t certificate-template.pdf \
  -c recipients.csv \
  -o certificates \
  --x 400 \
  --y 350 \
  -a CENTER \
  -f "Times New Roman" \
  -s 36
```

### Large Batch with Custom Threading

```bash
java -jar BulkTextRenderer-{VERSION}.jar \
  -t template.pdf \
  -c large-list.csv \
  -o output \
  --x 297 \
  --y 500 \
  --threads 8 \
  --sequential-threshold 20 \
  --verbose
```

## Output

Files are saved to the specified output folder with names derived from the text content:
- Spaces are replaced with underscores
- Special characters are removed
- Optional prefix and postfix are added

Example: `wedding-John_Doe-final.pdf`

## Error Handling

The tool provides clear error messages for:
- Missing template or CSV files
- Invalid file formats
- Missing required options
- File permission issues
- Platform-specific limitations (e.g., PNG/JPEG on macOS native-image)

Exit codes:
- `0` - Success
- `1` - Runtime error (file not found, processing error)
- `2` - Invalid arguments

## Troubleshooting

### PNG/JPEG Rendering Fails on macOS Native Executable

**Symptom**: Error message about missing AWT support

**Solution**: Use the JAR version instead:
```bash
java -jar BulkTextRenderer-{VERSION}.jar -t template.png ...
```

**Explanation**: GraalVM native-image on macOS doesn't bundle AWT native libraries required for PNG/JPEG rendering. See [#28](https://github.com/namila007/BulkTextRenderer/issues/28) for details.

**Alternatives**:
1. Use JAR mode (recommended)
2. Use Linux or Windows for native builds
3. Use Docker with Linux base image

### Performance Tips

- For small batches (<10 files), the tool automatically uses sequential processing (faster due to reduced overhead)
- For larger batches (≥10 files), parallel processing is enabled by default
- Adjust the threshold: `--sequential-threshold 20` (custom cutoff)
- Control thread count: `--threads 8` (for parallel jobs)

### Verbose Logging

Enable detailed logging to troubleshoot issues:

```bash
# INFO level logging
java -jar BulkTextRenderer-{VERSION}.jar ... --verbose

# DEBUG level logging (very detailed)
java -jar BulkTextRenderer-{VERSION}.jar ... --debug
```

## License

BulkTextRenderer is released under the MIT License. See [LICENSE](LICENSE) for the full text.

## Known Issues

See [GitHub Issues](https://github.com/namila007/BulkTextRenderer/issues) for known bugs and limitations.

Notable issues:
- [#28](https://github.com/namila007/BulkTextRenderer/issues/28) - AWT not supported in GraalVM native-image on macOS (PNG/JPEG limitation)
- [#29](https://github.com/namila007/BulkTextRenderer/issues/29) - Release strategy: Include Fat JAR alongside native executables
- [#30](https://github.com/namila007/BulkTextRenderer/issues/30) - Comprehensive error handling improvements

## Contributing

Contributions are welcome! Please ensure:
1. All tests pass (`./gradlew test`)
2. Code follows existing patterns
3. New features include tests
4. Update documentation as needed

See [IMPLEMENTATION_PROGRESS.md](IMPLEMENTATION_PROGRESS.md) for development notes and architectural decisions.

## Technical Details

### Threading Strategy

The application uses adaptive threading:
- **Sequential mode**: For small batches (<10 jobs by default)
  - Uses main thread only
  - Lower overhead for quick tasks
- **Parallel mode**: For larger batches (≥10 jobs)
  - Uses virtual threads (Java 21+)
  - Configurable thread count
  - Custom exception handler for visibility

Threshold is configurable via `--sequential-threshold`.

### GraalVM Native Image

The project supports compilation to native executables using GraalVM:
- Fast startup time (~instant vs ~1-2s for JAR)
- Lower memory footprint
- Platform-specific binaries
- **Limitation**: PNG/JPEG not supported on macOS (see [#28](https://github.com/namila007/BulkTextRenderer/issues/28))

Build with:
```bash
./gradlew nativeCompile
```

## Repo Activity
![Repository Activity](https://repobeats.axiom.co/api/embed/a986dc92e3b0ed3ff6d42e533fae5710f24e7d18.svg)
