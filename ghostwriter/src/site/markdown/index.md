---
<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.

**IMPORTANT:**  
The Ghostwriter Maven plugin is designed to work with **all types of project files**—including source code, documentation, project site content, and any other relevant files.  
**When generating content or documentation, always consider the full range of file types present in the project.**
# Page Structure
1. **Header**
   - **Project Title:** Extract automatically from `pom.xml`.
   - **Maven Central Badge:**  
     Use the following Markdown, replacing `[groupId]` and `[artifactId]` with values from `pom.xml`:  
     `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`
2. **Introduction**
   - Provide a comprehensive description of the project's purpose and main benefits.
   - Reference [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) as the conceptual foundation for Machai Ghostwriter.
3. **Overview**
   - Clearly explain the core functionality and value proposition of the project.
   Describe the project with diagrams bellow:
     - Create a project structure overview based on the `.puml` files below.
     - Describe the project without including file names in the description.
     - Use the project structure diagram by the path: `./images/c4-diagram.png` (`src/site/puml/c4-diagram.puml`).
4. **Machai Ghostwriter vs. Other Tools.** 
   - Identify the AI code assistant tool most similar to Machai Ghostwriter and explain why, focusing on project-wide automation, CI/CD integration, and extensibility.
   - List key similarities and key differences between Machai Ghostwriter and the closest tool.
   - Briefly compare Machai Ghostwriter to other popular tools (e.g., Tabnine, GitHub Copilot, Claude Code, Cursor) in terms of project-wide automation, guidance, and documentation features.
   - Summarize the comparison in a Markdown table showing which tools support project-wide automation, custom guidance, CI/CD integration, and documentation generation.
   - Conclude with a short statement on what makes Machai Ghostwriter unique.
Let me know if you want it even shorter or tailored for a specific toolset!
5. **Key Features**
   - Present a concise, bulleted list of the primary capabilities and features.
6. **Getting Started**
   - **Prerequisites:** List all required software, services, and environment settings.
   - **Download:**  
     Add a download link for the Ghostwriter CLI application jar:  
     `[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)`
   - **Basic Usage:** Provide an example command to run the application.
   - **Typical Workflow:** Outline the step-by-step process for using the project artifacts.
   - **Java Version:** State the required Java version as defined in `pom.xml`, and clarify any additional functional requirements.
7. **Configuration**
   - **Command-Line Options:** Analyze `/java/org/machanism/machai/gw/processor/Ghostwriter.java` to extract and describe all available command-line options.
   - **Options Table:** Present a table listing each option, its description, and default value.
   - **Example:** Provide a command-line example showing how to configure and run the application with custom parameters. Include information from the `Ghostwriter.help()` method.
8. **Resources**
   - List relevant links, including the official platform, GitHub repository, and Maven Central page.
# General Instructions
- Ensure clarity, completeness, and accuracy in each section.
- Use information from project files and source code as specified.
- Structure the documentation for easy navigation and practical use.
-->
canonical: https://machai.machanism.org/ghostwriter/index.html
---

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

## Introduction

Ghostwriter is a command-line AI automation and documentation engine in the Machai ecosystem. It scans project content, detects embedded `@guidance` instructions, and applies GenAI-driven updates directly to repository files. Its value comes from keeping automation intent close to the artifacts it governs, making changes easier to review, repeat, and integrate into regular development workflows.

Its conceptual foundation is [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html). Instead of relying only on ad hoc prompts, Ghostwriter uses instructions stored in the repository itself so teams can automate maintenance of source code, documentation, project site content, configuration, diagrams, and other relevant files in a structured, auditable way.

## Overview

Ghostwriter processes one or more scan targets such as directories, glob patterns, or regular-expression patterns. It loads configuration, resolves the project root, determines the active model and operating mode, applies excludes and optional instructions, and then processes each eligible artifact through a guided pipeline.

In its standard mode, the application scans repository content, extracts embedded directives, prepares prompts, calls the configured GenAI backend, and writes results back to the working tree. In Act mode, it executes reusable prompt templates and can expose controlled tools for file access, web retrieval, command execution, and related automation tasks.

### Architecture

![C4 Diagram](./images/c4-diagram.png)

The diagram shows a layered architecture centered on a command-line entry point that initializes runtime settings and dispatches work. A configuration layer provides project and execution settings, while processing components handle guided repository scanning and reusable act-based execution. A shared pipeline manages filtering, reading, reviewer selection, AI interaction, and write-back across supported file types. Tool integrations can be enabled for act execution, and provider management handles communication with external LLM services together with usage and operational logging.

## Machai Ghostwriter vs. Other Tools

The closest widely known tool to Machai Ghostwriter is **Claude Code**. Both support repository-level work rather than only inline code completion, and both fit automation-oriented development workflows. Claude Code is the nearest comparison because it can act across multiple files and supports agentic execution, but Ghostwriter is more explicitly designed for repeatable project-wide processing driven by repository-embedded guidance and automation-friendly CLI behavior.

### Key similarities

- Both can operate across multiple files in a repository.
- Both support AI-assisted automation beyond simple editor autocomplete.
- Both can participate in scripted development and CI/CD-oriented workflows.
- Both rely on configurable LLM-backed execution for repository tasks.

### Key differences

- **Guidance-first design:** Ghostwriter is built around persistent `@guidance` directives stored in project files, while Claude Code is primarily centered on interactive agent behavior.
- **Batch-oriented processing:** Ghostwriter is optimized for deterministic scanning of directories and patterns, which fits repeatable maintenance and scheduled automation jobs.
- **Documentation and governed content focus:** Ghostwriter is especially strong for updating documentation, site pages, diagrams, and other repository-managed content.
- **Reusable Act workflows:** Ghostwriter supports reusable act definitions and tool-enabled execution patterns for repeatable tasks.

### Comparison with other popular tools

- **GitHub Copilot:** Strong for editor-centric assistance and chat, but less focused on repository-embedded guidance and governed project-wide automation.
- **Cursor:** Good at interactive multi-file editing, but still more IDE-driven than guidance-driven.
- **Tabnine:** Primarily aimed at completion and local coding productivity rather than repository-wide guided transformation.
- **Claude Code:** The nearest match for multi-file agentic work, but Ghostwriter places more durable control in the repository through embedded guidance and batch execution.

| Tool | Project-wide automation | Custom guidance | CI/CD integration | Documentation generation |
|---|---|---|---|---|
| Machai Ghostwriter | Yes | Yes, embedded in files | Yes | Yes |
| Claude Code | Yes | Partial | Yes | Partial |
| Cursor | Partial | No | Partial | Partial |
| GitHub Copilot | Partial | No | Partial | Partial |
| Tabnine | Limited | No | Partial | Limited |

Machai Ghostwriter is unique because it combines repository-embedded guidance, repeatable batch processing, reusable acts, and documentation-oriented automation in a maintainable CLI workflow.

## Key Features

- Scans directories, glob patterns, and regex-based targets.
- Detects embedded `@guidance` directives in repository files.
- Processes source code, documentation, project site content, configuration, diagrams, and other relevant artifacts.
- Integrates with configurable GenAI providers and models.
- Supports extra system instructions from plain text, URLs, files, or standard input.
- Applies exclusion rules for selective processing.
- Provides Act mode for reusable prompt workflows.
- Supports configurable concurrency.
- Can log LLM request inputs for diagnostics and auditing.
- Works for local use as well as CI/CD automation.

## Getting Started

### Prerequisites

- Java 8 or later, based on `maven.compiler.release` set to `8` in `pom.xml`.
- Access to a supported GenAI provider and any required credentials or network connectivity.
- A working directory or project containing files to scan and update.
- Optional `gw.properties` configuration in the Ghostwriter home directory, or a custom configuration path supplied with `-Dgw.config=...`.
- Optional acts directory when using Act mode with predefined acts.

### Download

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```bash
java -jar gw.jar <scanDir> [options]
```

Examples:

```bash
java -jar gw.jar src
java -jar gw.jar "glob:**/*.java"
java -jar gw.jar "regex:^.*/[^/]+\.java$"
```

### Typical Workflow

1. Add or update `@guidance` directives in the files you want Ghostwriter to manage.
2. Configure model, scan defaults, excludes, instructions, and optional acts in `gw.properties`.
3. Run Ghostwriter against a directory or pattern.
4. Review generated changes in version control.
5. Re-run locally or in CI/CD to keep governed content current.

### Java Version

Ghostwriter requires **Java 8+**. Functional use also requires a valid GenAI provider/model configuration and any connectivity needed by the selected provider.

## Configuration

Ghostwriter loads settings from `gw.properties` in the resolved home directory unless overridden with the `gw.config` system property. The home directory resolution order is `gw.home`, then the configured project directory, then the current user directory.

### Command-Line Options

The CLI options below are derived from `Ghostwriter.java` and the built-in help output.

| Option | Description | Default value |
|---|---|---|
| `-h`, `--help` | Show the help message and exit. | None |
| `-d`, `--project.dir <path>` | Specify the root directory for file processing. | `project.dir` from configuration, otherwise the current user directory |
| `-t`, `--threads <n>` | Set the degree of concurrency for processing. | `gw.threads` from configuration |
| `-m`, `--model <provider:model>` | Set the GenAI provider and model, for example `OpenAI:gpt-5.1`. | `gw.model` from configuration |
| `-i`, `--instructions [value]` | Provide system instructions as plain text, a URL, or a `file:` path. If used without a value, instructions are read from standard input. Multi-line console input supports trailing `\` continuation. | `instructions` from configuration |
| `-e`, `--excludes <csv>` | Comma-separated list of directories to exclude from processing. | `gw.excludes` from configuration |
| `-l`, `--logInputs` | Log LLM request inputs to dedicated log files. | `false` unless enabled in configuration |
| `-as`, `--acts <path>` | Specify the directory containing predefined act prompt files. | `gw.acts` from configuration |
| `-a`, `--act [value]` | Run in Act mode using the provided value or, if omitted, prompt interactively for the act text. | `gw.act` from configuration when applicable |

The positional `<scanDir>` argument defines the scan target. According to the built-in help, it may be a relative path under the current project directory, an absolute path inside the root project directory, a raw directory name, a glob pattern such as `glob:**/*.java`, or a regex pattern such as `regex:^.*/[^/]+\.java$`. If no scan target is provided, Ghostwriter falls back to `gw.scanDir` from configuration and then to `.`.

### Example

```bash
java -Dgw.config=gw.properties -jar gw.jar src \
  -d . \
  -m OpenAI:gpt-5.1 \
  -t 4 \
  -e ".git,target" \
  -i "file:./instructions.txt" \
  -l
```

Act mode example:

```bash
java -jar gw.jar src -a "Summarize the repository" -as ./acts
```

## Resources

- Official platform: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
