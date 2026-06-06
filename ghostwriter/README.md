<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src\\site\\markdown\\index.md` content summary.
   - Add `![](src/site/resources/images/machai-ghostwriter-logo.png)` before the title.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)` after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
   - Add the Ghostwriter CLI application jar download link: [![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download) to the installation section.
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

![](src/site/resources/images/machai-ghostwriter-logo.png)

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

> A repository-wide AI automation and documentation engine that applies embedded guidance across source code, documentation, project site content, configuration, diagrams, and other project artifacts.

## Introduction

Ghostwriter is an advanced repository-wide AI automation and documentation engine in the Machai ecosystem. It scans project content, detects embedded `@guidance` instructions, and applies GenAI-assisted processing to source code, documentation, project site content, configuration, diagrams, and other relevant artifacts. The main benefit is that maintenance intent lives inside the repository itself, close to the files it governs, so updates become more repeatable, reviewable, and suitable for both local execution and CI/CD pipelines.

Its conceptual foundation is [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html). Instead of relying only on one-off prompts, Ghostwriter turns persistent repository guidance into structured project automation, enabling governed updates across the full range of project file types.

## Usage

### Getting Started

#### Prerequisites

- Java 8 or later, based on `maven.compiler.release` set to `8` in `pom.xml`.
- Access to a supported GenAI provider and any required credentials or network connectivity.
- A project or working directory containing files to scan and update.
- Optional `gw.properties` configuration in the Ghostwriter home directory, or a custom configuration path supplied with `-Dgw.config=...`.
- Optional acts directory when using Act mode with predefined act definitions.

#### Installation

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)

#### Basic Usage

```bash
java -jar gw.jar <paths> [options]
```

Examples:

```bash
java -jar gw.jar src
java -jar gw.jar "glob:**/*.java"
java -jar gw.jar "regex:^.*/[^/]+\\.java$"
```

#### Typical Workflow

1. Add or update `@guidance` directives in the files Ghostwriter should maintain.
2. Configure model, scan defaults, excludes, instructions, and optional acts in `gw.properties`.
3. Run Ghostwriter against a directory or pattern target.
4. Review generated changes in version control.
5. Re-run locally or in CI/CD to keep governed project artifacts current.

#### Java Version

Ghostwriter requires **Java 8+**. Practical use also requires a valid GenAI provider and model configuration, plus any connectivity needed by the selected provider.

## Configuration

### Command-Line Options

| Option | Description | Default value |
|---|---|---|
| `-h`, `--help` | Show the help message and exit. | None |
| `-d`, `--project.dir <path>` | Specify the path to the root directory for file processing. | `project.dir` from configuration, otherwise the current user directory |
| `-t`, `--threads <n>` | Set the degree of concurrency for processing to improve performance. | `gw.threads` from configuration |
| `-m`, `--model <provider:model>` | Set the GenAI provider and model, for example `OpenAI:gpt-5.1`. | `gw.model` from configuration |
| `-i`, `--instructions [value]` | Specify system instructions as plain text, by URL, or by file path. If used without a value, Ghostwriter reads instructions from standard input. | `instructions` from configuration |
| `-e`, `--excludes <csv>` | Specify a comma-separated list of directories to exclude from processing. | `gw.excludes` from configuration |
| `-l`, `--logInputs` | Log LLM request inputs to dedicated log files. | `false` unless enabled in configuration |
| `-as`, `--acts <path>` | Specify the path to the directory containing predefined act prompt files for processing. | `gw.acts` from configuration |
| `-a`, `--act [value]` | Run Ghostwriter in Act mode. If used without a value, Ghostwriter prompts for the act text interactively. | `gw.act` from configuration when applicable |

## Resources

- Official platform: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
