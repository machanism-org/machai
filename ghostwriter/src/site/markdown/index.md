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

Ghostwriter is an AI-powered command-line automation and documentation engine in the Machai ecosystem. It scans repository content, detects embedded `@guidance` instructions, and applies GenAI-assisted updates directly to project files. Its main benefit is that automation intent lives inside the repository itself, close to the source code, documentation, site pages, configuration, diagrams, and other artifacts it governs, making updates easier to review, repeat, and integrate into everyday delivery workflows.

Its conceptual foundation is [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html). Rather than depending only on ad hoc prompting, Ghostwriter uses repository-embedded instructions to make project maintenance structured, auditable, and reusable across the full range of project files.

## Overview

Ghostwriter processes scan targets such as directories, glob patterns, and regular-expression patterns. It initializes runtime settings, resolves the project root, loads configuration, selects the active model and execution mode, applies excludes and optional instructions, and then runs guided AI processing across eligible project artifacts.

In guidance-driven execution, it traverses the repository, extracts embedded directives, prepares project-aware prompts, invokes the configured GenAI backend, and writes generated results back to the working tree. In Act mode, it executes reusable prompt templates and can expose controlled tools for file access, web retrieval, REST calls, command execution, and act discovery.

### Architecture

![C4 Diagram](./images/c4-diagram.png)

The diagram shows a layered command-line architecture centered on a runtime entry point that initializes execution and dispatches work. A configuration layer supplies processing settings, while project layout resolution contributes metadata about modules and relevant source, test, and documentation areas. A shared scanning and AI-processing pipeline handles repository traversal, filtering, prompt construction, provider interaction, and write-back across supported file types. Two main execution paths sit on top of that pipeline: one focused on embedded guidance in repository artifacts and another focused on reusable act-driven workflows. Supporting integrations provide controlled access to local files, remote resources, and provider tooling, while logging and usage tracking capture operational activity.

## Machai Ghostwriter vs. Other Tools

The closest widely known tool to Machai Ghostwriter is **Claude Code**. Both operate across an entire repository rather than focusing only on inline completion, and both support agentic, automation-oriented workflows. Claude Code is the nearest comparison because it can perform multi-file work with tool access, but Ghostwriter is more explicitly designed for repeatable project-wide processing driven by repository-embedded guidance and command-line automation.

### Key similarities

- Both can work across multiple files in a repository.
- Both support AI-assisted automation beyond editor autocomplete.
- Both can participate in scripted engineering and CI/CD-oriented workflows.
- Both rely on configurable LLM-backed execution for repository tasks.

### Key differences

- **Guidance-first workflow:** Ghostwriter is built around persistent `@guidance` directives stored in project files, while Claude Code is primarily centered on interactive agent execution.
- **Batch processing model:** Ghostwriter is optimized for deterministic scanning of directories and patterns, which fits scheduled maintenance and repeatable automation jobs.
- **Repository-governed updates:** Ghostwriter is especially strong for maintaining documentation, site pages, diagrams, configuration, and other governed project artifacts in addition to source files.
- **Reusable acts:** Ghostwriter supports reusable act definitions and act-specific tooling for repeatable prompt workflows.

### Comparison with other popular tools

- **GitHub Copilot:** Strong for editor-centric assistance and chat, but less focused on repository-embedded guidance and governed project-wide automation.
- **Cursor:** Effective for interactive multi-file editing, but primarily IDE-driven rather than guidance-driven.
- **Tabnine:** Primarily aimed at completion and coding productivity, with limited support for repository-wide guided transformation.
- **Claude Code:** The nearest match for agentic multi-file execution, but Ghostwriter places more durable control inside the repository through embedded guidance and repeatable batch execution.

| Tool | Project-wide automation | Custom guidance | CI/CD integration | Documentation generation |
|---|---|---|---|---|
| Machai Ghostwriter | Yes | Yes, embedded in files | Yes | Yes |
| Claude Code | Yes | Partial | Yes | Partial |
| Cursor | Partial | No | Partial | Partial |
| GitHub Copilot | Partial | No | Partial | Partial |
| Tabnine | Limited | No | Partial | Limited |

Machai Ghostwriter is unique because it combines repository-embedded guidance, repeatable batch execution, reusable acts, and documentation-oriented automation in a governed CLI workflow.

## Key Features

- Scans directories, glob patterns, and regex-based targets.
- Detects embedded `@guidance` directives in repository files.
- Processes source code, documentation, project site content, configuration, diagrams, and other relevant artifacts.
- Integrates with configurable GenAI providers and models.
- Supports additional system instructions from plain text, URLs, files, or standard input.
- Applies exclusion rules for selective processing.
- Provides Act mode for reusable prompt workflows.
- Supports configurable concurrency.
- Can log LLM request inputs for diagnostics and auditing.
- Fits both local development and CI/CD automation scenarios.

## Getting Started

### Prerequisites

- Java 8 or later, based on `maven.compiler.release` set to `8` in `pom.xml`.
- Access to a supported GenAI provider and any required credentials or network connectivity.
- A project or working directory containing files to scan and update.
- Optional `gw.properties` configuration in the Ghostwriter home directory, or a custom configuration path supplied with `-Dgw.config=...`.
- Optional acts directory when using Act mode with predefined act definitions.

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
java -jar gw.jar "regex:^.*/[^/]+\\.java$"
```

### Typical Workflow

1. Add or update `@guidance` directives in the files Ghostwriter should maintain.
2. Configure model, scan defaults, excludes, instructions, and optional acts in `gw.properties`.
3. Run Ghostwriter against a directory or pattern.
4. Review the generated changes in version control.
5. Re-run locally or in CI/CD to keep governed content current.

### Java Version

Ghostwriter requires **Java 8+**. Practical use also requires a valid GenAI provider/model configuration and any connectivity needed by the selected provider.

## Configuration

Ghostwriter loads settings from `gw.properties` in the resolved home directory unless overridden with the `gw.config` system property. The home directory is resolved from `gw.home` when defined; otherwise it defaults to the current user directory. The project root is taken from `-d` / `--project.dir`, then from configuration, and otherwise falls back to the current user directory.

### Command-Line Options

The CLI options below are derived from `Ghostwriter.java` and the built-in help output.

| Option | Description | Default value |
|---|---|---|
| `-h`, `--help` | Show the help message and exit. | None |
| `-d`, `--project.dir <path>` | Specify the path to the root directory for file processing. | `project.dir` from configuration, otherwise the current user directory |
| `-t`, `--threads <n>` | Set the degree of concurrency for processing to improve performance. | `gw.threads` from configuration |
| `-m`, `--model <provider:model>` | Set the GenAI provider and model, for example `OpenAI:gpt-5.1`. | `gw.model` from configuration |
| `-i`, `--instructions [value]` | Specify system instructions as plain text, by URL, or by `file:` path. If used without a value, instructions are read from standard input. Each line is processed, preserving blank lines and resolving URL and file references. | `instructions` from configuration |
| `-e`, `--excludes <csv>` | Specify a comma-separated list of directories to exclude from processing. | `gw.excludes` from configuration |
| `-l`, `--logInputs` | Log LLM request inputs to dedicated log files. | `false` unless enabled in configuration |
| `-as`, `--acts <path>` | Specify the path to the directory containing predefined act prompt files for processing. | `gw.acts` from configuration |
| `-a`, `--act [value]` | Run Ghostwriter in Act mode. If used without a value, Ghostwriter prompts for the act text interactively. | `gw.act` from configuration when applicable |

The positional `<scanDir>` argument defines the scan target. According to the built-in help, it may be a relative path with respect to the current project directory, an absolute path located within the root project directory, a raw directory name, a glob pattern such as `glob:**/*.java`, or a regex pattern such as `regex:^.*/[^/]+\\.java$`. If no scan target is supplied, Ghostwriter falls back to `gw.scanDir` from configuration and then to `.`.

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
