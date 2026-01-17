<!--
@guidance:
**ADD FOLLOWING SECTIONS TO THIS README FILE:**
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src\site\markdown\index.md` content summary.
   - Add `![](src/site/resources/images/machai-ghostwriter-logo.png)` before the title.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)` after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

![](src/site/resources/images/machai-ghostwriter-logo.png)

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

## Project Overview

Ghostwriter is an AI-assisted documentation engine for Maven/Java projects. It scans your project sources and documentation, interprets embedded guidance tags, and generates or updates documentation artifacts in a consistent, repeatable way.

## Introduction

Ghostwriter is an AI-assisted documentation engine for Maven/Java projects. It scans your project sources and documentation, interprets embedded guidance tags, and generates or updates documentation artifacts in a consistent, repeatable way.

It is designed for use in local workflows and CI pipelines to keep documentation accurate as the code evolves.

## Usage

### Prerequisites

- Java 9+ runtime.
- Network access to your selected GenAI provider (if applicable).

### Installation

Download the CLI JAR:

[![Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/gw.jar/download)

### Basic Usage

```bash
java -jar gw.jar
```

To scan specific directories (relative to the working directory), pass them as trailing arguments:

```bash
java -jar gw.jar src/site src/main
```

### Typical Workflow

1. Build or download `gw.jar`.
2. Choose a working directory (the project root) and optionally the directories to scan.
3. (Optional) Provide additional instructions (inline or as a file path).
4. Run Ghostwriter.
5. Review and commit generated/updated documentation.

### Configuration (command-line options)

| Option | Long | Argument | Description | Default |
|---|---|---:|---|---|
| `-h` | `--help` | No | Displays help information for usage. | Off |
| `-t` | `--threads` | No | Enable multi-threaded processing. | Off |
| `-d` | `--dir` | Yes | Path to the project root directory. | Current user directory (working dir) |
| `-g` | `--genai` | Yes | GenAI service provider and model (e.g. `OpenAI:gpt-5.1`). | Provider/model resolved by `Config.getChatModel(...)` |
| `-i` | `--instructions` | Yes | Additional file-processing instructions. Provide either the instruction text directly or a path to a file containing the instructions. | None |

**Positional arguments**: one or more directories to scan. If none are provided, Ghostwriter scans the resolved root directory.

Example:

```bash
java -jar gw.jar \
  --threads \
  --genai OpenAI:gpt-5.1 \
  --instructions ./ghostwriter-instructions.txt \
  .
```

## Resources

- GitHub: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Download CLI JAR: https://sourceforge.net/projects/machanism/files/gw.jar/download
