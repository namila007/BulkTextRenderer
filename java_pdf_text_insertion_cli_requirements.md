# Java-Based PDF Text Insertion CLI Tool


## 1. Overview
The system is a **Java-based CLI application** that generates PDFs or PNGs by inserting text from CSV files into predefined positions within a PDF or PNG template.

Each line of text in the CSV produces a **separate output file** (PDF or PNG) with text rendered at user-defined coordinates, alignment, and font.

**Note:** Currently, the tool supports PNG as both input and output in addition to PDF. In the future, cross-type conversions (e.g., PDF to PNG, PNG to PDF) and other formats can be implemented.

---

## 2. Input

### 2.1 CSV Files
- User provides **one or more CSV files**
- Each CSV contains **text values separated by new lines**
- Each line represents **one PDF output**
- Example:
  - 10 lines → 10 generated PDFs

### 2.2 Template File (PDF or PNG)
- A base PDF or PNG is used as the template
- Text is inserted into this template at specified positions


### 2.3 Text Position Configuration
User defines:
- **X coordinate**
- **Y coordinate**
- **Horizontal alignment**
  - `LEFT`
  - `CENTER`
  - `RIGHT` *(new)*

#### Alignment Behavior
- **LEFT**  
  Text starts exactly at the given `(x, y)` position

- **CENTER**  
  Text is horizontally centered around the given `x` coordinate

- **RIGHT**  
  Text ends at the given `(x, y)` position (right-aligned)

### 2.4 Font Configuration
- By default, the font is **Times New Roman**
- User can provide a **custom font name installed on the local PC**
- The generated PDF/PNG must render text using the specified font

---

## 3. Output
- One output file (PDF or PNG) is generated **per CSV line**
- Output files are created using:
  - The same base template (PDF or PNG)
  - The same position, alignment, and font configuration
  - Different text values from the CSV

---

## 4. Processing Logic
- CSV lines are processed **independently**
- Multiple PDFs can be generated **in parallel**
- Parallelism is based on:
  - Current PC core count (default)
  - User can specify custom parallelism (number of threads/CPUs) via CLI argument
  - Java virtual threads

---

## 5. Application Type
- Command Line Interface (CLI) application
- No graphical user interface

---

## 6. Technology Stack
- **Language**: Java 24
- **Build Tool**: Gradle
- **Dependency Injection**: Spring Context
- **CLI Framework**: Picocli
- **PDF Library**: OpenPDF
- **Concurrency**: Java virtual threads

---

## 7. Execution Flow (High Level)
1. User runs the CLI command with required parameters
  - User can specify output folder (default: `./output` in current directory)
  - User can specify number of parallel CPUs/threads (default: system core count)
2. Application reads CSV file(s)
3. For each line in the CSV:
  - Text is placed at the defined `(x, y)` position
  - Alignment rules are applied, including new `RIGHT` alignment (text ends at `(x, y)`)
  - Specified font is used (default: Times New Roman)
4. A new output file (PDF or PNG) is generated for each CSV line in the specified output folder
5. Output files are generated in parallel
6. User can view the current progress of output creation as a progress bar (0-100%)



----
new requirements phase 5.1
1. application able to register os fonts https://librepdf.github.io/OpenPDF/docs-1-2-7/com/lowagie/text/FontFactory.html for pdf, for png use relavant java inbuild? libraries, use context7 for explore and user can provide font exact name, then this custom font is used for text generation

2. user and give -prifix and -postfix for the output file name, by default  text from each(snake case max 15 chars) .so final name will be <prefix>-<base-template-name>-<csv_text_first_part>-<postfix>.<format>
default of pre and posfix will be null. show default available fontlist on picocli help





