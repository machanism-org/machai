---
canonical: https://machai.machanism.org/ghostwriter/index.html
---

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

> A guided file processing engine for generating and maintaining project-wide documentation and code improvements with AI.

## Introduction

Machai Ghostwriter is an AI-assisted documentation and review engine that scans an entire project—source code, tests, documentation, and other relevant assets—extracts embedded `@guidance:` directives, and turns them into actionable prompts for a configured GenAI provider.

Its conceptual foundation is [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): instead of treating files as isolated inputs, Ghostwriter treats a repository as a structured system, where each file can carry local guidance and the tool orchestrates processing across the project consistently.

## Overview

Ghostwriter is delivered as a Java CLI entry point (`org.machanism.machai.gw.processor.Ghostwriter`) that:

- Loads configuration from `gw.properties` (or an override via `-Dgw.config=...`).
- Sets the project root directory and scan targets (directory paths or `glob:` / `regex:` matchers).
- Discovers supported file types and uses per-type reviewers to extract embedded `@guidance:` directives.
- Composes provider inputs including project structure context and optional system instructions.
- Executes the configured GenAI provider across matching files (or in Act mode).

In addition to per-file guidance, Ghostwriter can apply *default guidance* when a file has no embedded `@guidance:` and can also perform a folder-level step (processor-dependent).

## Machai Ghostwriter vs. Other Tools

The closest tool in spirit is **Claude Code**: both operate across a repository (not just a single editor buffer), can be used non-interactively, and are suitable for scripted or CI/CD execution.

### Similarities (Ghostwriter vs. Claude Code)

- Repository-aware workflows (operate on more than one file).
- Designed for practical engineering tasks, not just chat.
- Can be used in automation contexts.

### Key differences

- **Guidance-first**: Ghostwriter’s primary input mechanism is embedded `@guidance:` directives in project files (plus optional default guidance). Claude Code is typically driven by interactive task prompts.
- **Deterministic scanning**: Ghostwriter has a defined scan/match model (directory, `glob:` or `regex:`) and a reviewer registry keyed by file extension.
- **Project-structure context**: Ghostwriter injects project layout information into prompts during processing.
- **Distribution model**: Ghostwriter is packaged as a Java CLI (and designed to integrate with Maven-based projects), whereas Claude Code is a separate agent toolchain.

### Brief comparison to other assistants

- **GitHub Copilot / Cursor / Tabnine**: primarily editor-first, focusing on inline completion and local code assistance rather than guided, project-wide batch processing.
- **Claude Code**: most similar for repo-scale actions, but Ghostwriter is distinguished by embedded guidance directives and a scanning pipeline.

### Capability summary

| Tool | Project-wide automation | Custom guidance in files (`@guidance:`) | CI/CD-friendly batch runs | Documentation generation |
|---|---:|---:|---:|---:|
| Machai Ghostwriter | Yes | Yes | Yes | Yes |
| Claude Code | Yes | No (prompt-driven) | Yes | Partial |
| GitHub Copilot | Limited | No | Limited | Limited |
| Cursor | Limited | No | Limited | Limited |
| Tabnine | Limited | No | Limited | Limited |

Machai Ghostwriter is unique in how it turns *file-local guidance* into a repeatable, repository-scale processing pipeline.

## Key Features

- Scans project directories and patterns (`glob:` / `regex:`) and processes matching files.
- Extracts embedded `@guidance:` directives using per-file-type reviewers.
- Adds project structure context to prompts.
- Supports system instructions and default guidance from plain text, URLs, or `file:` references.
- Supports excludes (exact paths or `glob:` / `regex:` patterns).
- Optional multi-threaded processing.
- Optional logging of provider inputs per processed file.
- “Act mode” for executing predefined prompts (`--act`).

## Getting Started

### Prerequisites

- Java **8** (as configured by `maven.compiler.release` in `pom.xml`).
- A configured GenAI provider/model setting (`gw.model`) or CLI override (`-m/--model`).
- (Optional) A `gw.properties` file to persist configuration.

### Download

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```bash
java -jar gw.jar <scanDir> -m OpenAI:gpt-5.1
```

### Typical Workflow

1. Add `@guidance:` blocks to the files you want Ghostwriter to improve or document.
2. Create `gw.properties` (optional) and configure:
   - `gw.rootDir` (project root)
   - `gw.model` (provider:model)
   - `gw.instructions` (optional)
   - `gw.excludes` (optional)
   - `gw.guidance` (optional default guidance)
3. Run Ghostwriter against a directory or pattern (e.g., `src`, `glob:**/*.md`, `regex:...`).
4. Review the resulting changes and iterate.

### Java Version

Ghostwriter requires **Java 8**. In addition, you must configure an accessible GenAI provider/model (e.g., via `gw.model` or `-m/--model`), otherwise the CLI will fail fast.

## Configuration

Ghostwriter CLI options are defined in `org.machanism.machai.gw.processor.Ghostwriter`.

### Command-Line Options

- `-h, --help` — Show help message and exit.
- `-r, --root <path>` — Root directory for file processing.
- `-t, --threads <count>` — Degree of concurrency for processing.
- `-m, --model <provider:model>` — Set the GenAI provider and model (e.g., `OpenAI:gpt-5.1`).
- `-i, --instructions[=<text|url|file:...>]` — System instructions (plain text, URL, or `file:`). If no value: stdin using `\\` as a line-continuation marker.
- `-g, --guidance[=<text|url|file:...>]` — Default guidance (plain text, URL, or `file:`). If no value: stdin using `\\` as a line-continuation marker.
- `-e, --excludes <csv>` — Comma-separated list of directories to exclude.
- `-l, --logInputs` — Log LLM request inputs to dedicated log files.
- `-as, --acts <path>` — Directory containing predefined act prompt files.
- `-a, --act[=<name and prompt>]` — Run in Act mode (interactive execution of predefined prompts). If no value: stdin using `\\` as a line-continuation marker.

### Options Table

| Option | Description | Default |
|---|---|---|
| `-h, --help` | Show this help message and exit. | `false` |
| `-r, --root <path>` | Specify the path to the root directory for file processing. | `gw.rootDir` or current working directory |
| `-t, --threads <count>` | Degree of concurrency for processing. | `gw.threads` or unset |
| `-m, --model <provider:model>` | Set the GenAI provider and model. | `gw.model` |
| `-i, --instructions[=<text\|url\|file:...>]` | System instructions (plain text, URL, or `file:`). If no value: stdin until a line does not end with `\\`. | `gw.instructions` |
| `-g, --guidance[=<text\|url\|file:...>]` | Default guidance (plain text, URL, or `file:`). If no value: stdin until a line does not end with `\\`. | `gw.guidance` |
| `-e, --excludes <csv>` | Comma-separated list of directories to exclude from processing. | `gw.excludes` |
| `-l, --logInputs` | Log LLM request inputs to dedicated log files. | `false` (`gw.logInputs`) |
| `-as, --acts <path>` | Directory containing predefined act prompt files. | not set |
| `-a, --act[=<...>]` | Act mode (interactive execution of predefined prompts). | disabled |

### Example

This example combines a scan pattern, explicit provider selection, default guidance from stdin, and input logging:

```bash
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g -l
```

From the built-in help:

- `<scanDir>` may be a relative path (from the current project directory) or a `glob:` / `regex:` matcher.
- If an option that accepts optional text (`-g/--guidance`, `-i/--instructions`, or `-a/--act`) is used without a value, Ghostwriter reads multi-line input from stdin.
- When entering multi-line input interactively, end input when a line does not end with `\\` (a trailing backslash continues the next line).

## Default Guidance

Ghostwriter supports *default guidance* via `AIFileProcessor.setDefaultPrompt(String)` (referred to in configuration/CLI as `gw.guidance`). Default guidance is used when a file contains no embedded `@guidance:` directives, and it can also be used for an optional folder-level processing step.

### Purpose

- Provide a baseline instruction set for files that do not carry their own embedded `@guidance:` blocks.
- Allow centralized, repeatable prompting for a scan run (for example: “summarize all Markdown files”, “modernize headers”, “add missing Javadoc”).
- Optionally run a folder-level prompt against the scanned folder/project context (via `AIFileProcessor.processFolder(ProjectLayout)`), depending on processor behavior.

### How it works

Default guidance is stored on the processor as `defaultPrompt` and is passed to the GenAI provider when:

- a processed file has no embedded `@guidance:`; and/or
- the processor performs a folder-level step.

When the guidance value is supplied, it is expanded line-by-line using `AIFileProcessor.parseLines(String)`:

- Blank lines are preserved.
- Lines starting with `http://` or `https://` are fetched and included.
- Lines starting with `file:` are loaded from the referenced file.
  - If the file path is relative, it is resolved from the configured root directory.
- Other lines are included as-is.

### Configuring default guidance

You can set default guidance via:

- Property: `gw.guidance`
- CLI: `-g, --guidance[=<text|url|file:...>]`

If `-g/--guidance` is provided without a value, Ghostwriter reads multi-line text from stdin; a trailing `\\` continues input on the next line.

## Resources

- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub repository (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter

<!-- @guidance:
**IMPORTANT:**  
The Ghostwriter Maven plugin is designed to work with **all types of project files**—including source code, documentation, project site content, and any other relevant files.  
**When generating content or documentation, always consider the full range of file types present in the project.**
# Page Structure
1. **Header**
   - **Project Title:** Extract automatically from `pom.xml`.
   - **Maven Central Badge:**  
     Use the following Markdown, replacing `[groupId]` and `[artifactId]` with values from `pom.xml`:  
     `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`
2. **Quote**
   - Do not change this section. Leave it exactly as provided.
3. **Introduction**
   - Provide a comprehensive description of the project's purpose and main benefits.
   - Reference [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) as the conceptual foundation for Machai Ghostwriter.
4. **Overview**
   - Clearly explain the core functionality and value proposition of the project.
5. **Machai Ghostwriter vs. Other Tools.** 
   - Identify the AI code assistant tool most similar to Machai Ghostwriter and explain why, focusing on project-wide automation, CI/CD integration, and extensibility.
   - List key similarities and key differences between Machai Ghostwriter and the closest tool.
   - Briefly compare Machai Ghostwriter to other popular tools (e.g., Tabnine, GitHub Copilot, Claude Code, Cursor) in terms of project-wide automation, guidance, and documentation features.
   - Summarize the comparison in a Markdown table showing which tools support project-wide automation, custom guidance, CI/CD integration, and documentation generation.
   - Conclude with a short statement on what makes Machai Ghostwriter unique.
Let me know if you want it even shorter or tailored for a specific toolset!
6. **Key Features**
   - Present a concise, bulleted list of the primary capabilities and features.
7. **Getting Started**
   - **Prerequisites:** List all required software, services, and environment settings.
   - **Download:**  
     Add a download link for the Ghostwriter CLI application jar:  
     `[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)`
   - **Basic Usage:** Provide an example command to run the application.
   - **Typical Workflow:** Outline the step-by-step process for using the project artifacts.
   - **Java Version:** State the required Java version as defined in `pom.xml`, and clarify any additional functional requirements.
8. **Configuration**
   - **Command-Line Options:** Analyze `/java/org/machanism/machai/gw/processor/Ghostwriter.java` to extract and describe all available command-line options.
   - **Options Table:** Present a table listing each option, its description, and default value.
   - **Example:** Provide a command-line example showing how to configure and run the application with custom parameters. Include information from the `Ghostwriter.help()` method.
9. **Default Guidance**
   - Use the documentation for the `FileProcessor.setDefaultGuidance(String defaultGuidance)` method to generate a section detailing the purpose and usage of `defaultGuidance`.
10. **Resources**
   - List relevant links, including the official platform, GitHub repository, and Maven Central page.
# General Instructions
- Ensure clarity, completeness, and accuracy in each section.
- Use information from project files and source code as specified.
- Structure the documentation for easy navigation and practical use.
-->
