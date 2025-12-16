# BulkTextRenderer

[![GitHub Actions - Gradle](https://github.com/namila007/BulkTextRenderer/actions/workflows/build.yml/badge.svg)](https://github.com/namila007/BulkTextRenderer/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/java-24-blue.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-v8.14-green.svg)](https://gradle.org/)
[![GitHub License](https://img.shields.io/github/license/namila007/BulkTextRenderer)](https://github.com/namila007/BulkTextRenderer/blob/master/LICENSE)

A high-performance CLI tool for bulk rendering text onto PDF, PNG, and JPEG templates. Perfect for generating personalized invitations, certificates, name cards, and other documents from a CSV list of names or text entries.

## Features

- **Multiple Format Support**: Render text on PDF, PNG, JPG, and JPEG template files
- **Bulk Processing**: Process multiple entries from a CSV file in one command
- **Multi-Column CSV**: Support for `name,prefix,postfix` format with automatic text assembly
- **Parallel Execution**: Configurable multi-threaded processing for faster rendering
- **Flexible Text Positioning**: Precise X/Y coordinate placement with pixel or millimeter units
- **Unified Coordinate System**: Top-left origin (0,0) for all formats
- **Text Alignment**: Left, center, or right alignment options
- **Custom Fonts**: Use system fonts or built-in PDF fonts
- **Customizable Output**: Add prefix and postfix to output filenames
- **Progress Tracking**: Real-time progress feedback during processing
- **Configurable Logging**: Verbose and debug modes for troubleshooting

## Prerequisites

- **Java 24** or higher
- **Gradle 8.x** (wrapper included)

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

The build automatically creates a fat JAR with all dependencies:

```bash
./gradlew jar
```

The JAR file will be created at: `build/libs/BulkTextRenderer-1.0-SNAPSHOT.jar`

## Usage

### Basic Usage

```bash
java -jar build/libs/BulkTextRenderer-1.0-SNAPSHOT.jar \
  -t template.pdf \
  -c names.csv \
  -o output \
  --x 297 \
  --y 500
```

### With All Options

```bash
java -jar build/libs/BulkTextRenderer-1.0-SNAPSHOT.jar \
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
  --prefix wedding \
  --postfix final \
  --verbose
```

### List Available Fonts

```bash
java -jar build/libs/BulkTextRenderer-1.0-SNAPSHOT.jar --list-fonts
```

### Display Help

```bash
java -jar build/libs/BulkTextRenderer-1.0-SNAPSHOT.jar --help
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
| `--threads` | `-p` | Number of parallel threads | CPU cores |
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
java -jar build/libs/BulkTextRenderer-1.0-SNAPSHOT.jar \
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

### Name Badges (PNG)

```bash
java -jar build/libs/BulkTextRenderer-1.0-SNAPSHOT.jar \
  -t badge-template.png \
  -c attendees.csv \
  -o badges \
  --x 200 \
  --y 150 \
  -a CENTER \
  -f SansSerif \
  -s 32
```

### Certificates (PDF, Parallel Processing)

```bash
java -jar build/libs/BulkTextRenderer-1.0-SNAPSHOT.jar \
  -t certificate-template.pdf \
  -c recipients.csv \
  -o certificates \
  --x 400 \
  --y 350 \
  -a CENTER \
  -f "Times New Roman" \
  -s 36 \
  -p 8
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

Exit codes:
- `0` - Success
- `1` - Runtime error (file not found, processing error)
- `2` - Invalid arguments

## License

BulkTextRenderer is released under the MIT License. See [LICENSE](LICENSE) for the full text.

## Contributing

Contributions are welcome! Please ensure:
1. All tests pass (`./gradlew test`)
2. Code follows existing patterns
3. New features include tests
