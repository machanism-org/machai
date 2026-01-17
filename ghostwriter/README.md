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

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

## Project Overview

Ghostwriter is an advanced documentation engine and CLI tool that scans a project tree, applies embedded guidance markers (e.g., `@guidance` comments), and uses a configured GenAI provider/model to synthesize and assemble consistent documentation across your repository.

## Introduction

Ghostwriter is an advanced documentation engine that automatically scans, analyzes, and assembles project documentation using embedded guidance tags and AI-powered synthesis. It helps teams keep documentation accurate and consistent by applying structured rules (guidance) across markdown and other documentation artifacts.

## Usage

### Prerequisites

- Java 17+ (recommended)
- Maven 3.9+ (for building)
- Network access to your configured GenAI provider (if required by the selected model)

### Environment Variables

The CLI accepts most configuration via command-line options. Environment variables may be required depending on the GenAI provider you use.

| Variable | Required | Description |
|---|---:|---|
| `OPENAI_API_KEY` | No* | API key used by OpenAI-backed models. Required only if you select an OpenAI model/provider. |

\* Provider-specific; set the variables required by your chosen GenAI service.

### Installation

- Build from source (example):

```bash
mvn -DskipTests package
```

- Download the CLI jar:

[Download Jar](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download jar")](https://sourceforge.net/projects/machanism/files/gw.jar/download)

### Basic Usage

```bash
java -jar gw.jar -d /path/to/project
```

### Command-Line Options

| Option | Description | Default |
|---|---|---|
| `-h`, `--help` | Displays help information for usage. | _n/a_ |
| `-t`, `--threads` | Enable multi-threaded processing. | `false` |
| `-d`, `--dir <path>` | Path to the project root directory. If omitted, uses the current user directory. | current user directory |
| `-g`, `--genai <provider:model>` | GenAI service provider and model (example: `OpenAI:gpt-5.1`). If omitted, the provider default is used (via configuration). | provider default |
| `-i`, `--instructions <text-or-file>` | Additional processing instructions; can be inline text or a path to a file whose contents will be used. | _none_ |

### Typical Workflow

1. Choose (or set) a GenAI provider/model.
2. Point Ghostwriter at a project root directory.
3. Optionally provide additional instructions (inline or from a file).
4. Run a scan against the root directory or specific subdirectories.
5. Review changes produced by the scan and commit updates.
