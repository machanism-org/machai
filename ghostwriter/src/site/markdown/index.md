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

Ghostwriter is an advanced documentation and repository automation engine in the Machai ecosystem. It scans project files, detects embedded `@guidance` instructions, and uses configurable GenAI providers to generate or update content directly in the working tree. Its main benefit is that intent lives beside the files it governs, making AI-driven maintenance more transparent, reviewable, and repeatable across source code, documentation, site pages, configuration, and other project artifacts.

The conceptual foundation is [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html). Rather than treating AI prompts as one-off chat inputs, Ghostwriter applies guidance stored in the repository itself, enabling structured project-wide automation that fits both local development and CI/CD workflows.

## Overview

Ghostwriter is a command-line application that processes one or more scan targets such as directories, glob patterns, or regular-expression patterns. It initializes project and runtime configuration, determines the active AI model, applies excludes and optional global instructions, and then processes each eligible file through a guided pipeline.

During execution, the system traverses the selected content, reads embedded directives, builds prompts, invokes the configured GenAI backend, and writes the resulting updates back to the repository. In its default mode it focuses on guided file processing; in Act mode it runs reusable prompt templates and can expose tool functions for richer automated workflows.

### Architecture

![C4 Diagram](./images/c4-diagram.png)

The architecture centers on a command-line entry point that initializes configuration, resolves the working root, and selects the operating mode. A processing layer then scans the project, filters excluded content, and routes each supported artifact through a shared pipeline for reading, prompt construction, AI interaction, and write-back. Specialized review and processing components help adapt behavior to different content types, while provider integration manages model selection, request dispatch, and usage logging. In Act-oriented workflows, the system can also expose controlled tools for file access, web retrieval, command execution, and reusable prompt definitions.

## Machai Ghostwriter vs. Other Tools

The closest widely recognized tool to Machai Ghostwriter is **Claude Code**. Both operate beyond simple inline completion, can work across repositories, and fit automation-oriented workflows better than traditional IDE-only assistants. Claude Code is the nearest match because it supports agentic repository interaction and command-driven development tasks, but Ghostwriter is more explicitly designed for project-wide guided processing, repeatable content maintenance, and repository-embedded instructions.

### Key similarities

- Both can operate across multiple files instead of only a single editor buffer.
- Both can be integrated into scripted or semi-automated development workflows.
- Both depend on configurable LLM-backed execution rather than static templates alone.
- Both are useful for large-scale repository understanding and transformation tasks.

### Key differences

- **Guidance-first model:** Ghostwriter is built around persistent `@guidance` embedded in project files, while Claude Code is centered more on interactive agent behavior.
- **Repeatable batch processing:** Ghostwriter is optimized for scanning directories and patterns in a deterministic batch-style run, which aligns naturally with CI/CD and scheduled maintenance jobs.
- **Documentation and content assembly focus:** Ghostwriter is especially strong for updating documentation, project sites, structured text, and governed content across the repository.
- **Extensible Act workflow:** Ghostwriter supports reusable Act definitions and tool-enabled execution for predefined project automation patterns.

### Comparison with other popular tools

- **GitHub Copilot:** Excellent for in-editor completion and chat assistance, but not primarily oriented toward repository-wide guidance-driven automation.
- **Cursor:** Strong for interactive AI-assisted editing and multi-file context, but still more editor-centric than guidance-centric.
- **Tabnine:** Focused mainly on developer productivity through completion and assistance, with less emphasis on project-wide governed transformation.
- **Claude Code:** Closest in repository interaction and automation capability, but Ghostwriter places more control directly in the repository through embedded guidance and repeatable scanning behavior.

| Tool | Project-wide automation | Custom guidance in files | CI/CD integration | Documentation generation |
|---|---|---|---|---|
| Machai Ghostwriter | Yes | Yes | Yes | Yes |
| Claude Code | Yes | Partial | Yes | Partial |
| Cursor | Partial | No | Partial | Partial |
| GitHub Copilot | Partial | No | Partial | Partial |
| Tabnine | No | No | Partial | Limited |

Machai Ghostwriter is unique because it combines repository-embedded guidance, batch-style project scanning, reusable acts, and documentation-oriented automation in a form that is especially well suited to maintainable, auditable AI-driven project workflows.

## Key Features

- Processes directories, glob patterns, and regex-based scan targets.
- Extracts embedded `@guidance` directives directly from project files.
- Supports project-wide automation across code, documentation, site content, and other relevant artifacts.
- Integrates with configurable GenAI providers and models.
- Accepts additional system instructions from inline text, URLs, or files.
- Supports exclusion rules for selective processing.
- Provides Act mode for reusable prompt-driven workflows.
- Supports configurable concurrency for faster runs.
- Can log request inputs for diagnostics and auditing.
- Fits local execution as well as CI/CD automation scenarios.

## Getting Started

### Prerequisites

- Java 8 or later runtime, based on `maven.compiler.release` set to `8` in `pom.xml`.
- Access to a supported GenAI provider and any required credentials, endpoints, or network connectivity.
- A project or working directory containing files to scan and update.
- Optional `gw.properties` configuration in the Ghostwriter home directory or a custom path provided via `-Dgw.config=...`.
- If using Act mode, an acts directory or configured acts location when custom acts are required.

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

1. Add or refine `@guidance` directives in the files you want Ghostwriter to manage.
2. Configure the default model, exclusions, instructions, and optional acts in `gw.properties`.
3. Run Ghostwriter against a directory or pattern to process eligible files.
4. Review the generated changes and commit them to version control.
5. Re-run Ghostwriter locally or in CI/CD to keep documentation and governed content synchronized over time.

### Java Version

Ghostwriter requires **Java 8+**. In addition to the Java runtime, successful execution also depends on a valid AI provider/model configuration and any external connectivity required by the configured provider.

## Configuration

Ghostwriter loads configuration from `gw.properties` in the resolved home directory unless overridden with the `gw.config` system property. The home directory resolution order is:

1. `gw.home`
2. the CLI-specified project directory (`-d` / `--project.dir`)
3. the current user directory

### Command-Line Options

The CLI exposes the following options based on `Ghostwriter.java` and its built-in help output.

| Option | Description | Default value |
|---|---|---|
| `-h`, `--help` | Show the help message and exit. | None |
| `-d`, `--project.dir <path>` | Specify the root directory for file processing. | `project.dir` from config, otherwise current user directory |
| `-t`, `--threads <n>` | Set the degree of concurrency to improve processing performance. | `gw.threads` from config |
| `-m`, `--model <provider:model>` | Set the GenAI provider and model, such as `OpenAI:gpt-5.1`. | `gw.model` from config |
| `-i`, `--instructions [value]` | Provide system instructions as plain text, a URL, or a `file:` reference. If used without a value, instructions are read from standard input. Multi-line input supports `\` line continuation. | `instructions` from config |
| `-e`, `--excludes <csv>` | Comma-separated list of directories to exclude from processing. | `gw.excludes` from config |
| `-l`, `--logInputs` | Log LLM request inputs to dedicated log files. | `false` unless enabled in configuration |
| `-as`, `--acts <path>` | Path to the directory containing predefined act prompt files. | `acts.location` from config |
| `-a`, `--act [value]` | Run in Act mode using a provided or prompted act value. | `gw.act` from config when applicable |

The scan target itself is provided as `<scanDir>`. According to the built-in help, it can be a relative path under the current project directory, an absolute path within the root project directory, a glob pattern such as `glob:**/*.java`, or a regex pattern such as `regex:^.*/[^/]+\.java$`. If no scan target is supplied, Ghostwriter falls back to `gw.scanDir` from configuration and then to the current user directory.

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
