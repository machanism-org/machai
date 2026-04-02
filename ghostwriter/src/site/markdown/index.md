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

Ghostwriter is an advanced documentation engine for the Machai ecosystem. It scans a project’s files, extracts embedded `@guidance` directives, and uses a configured GenAI provider to synthesize updates—turning your repository into a self-describing, continuously maintained knowledge base.

The conceptual foundation is Machanism’s **Guided File Processing**: guidance is stored with the files it governs, and the processor applies that guidance consistently across the project (source code, documentation, project site content, configuration, and other relevant artifacts).

See: https://www.machanism.org/guided-file-processing/index.html

## Overview

Ghostwriter runs as a CLI. You point it at one or more scan roots or patterns. For each discovered file that it supports, it:

- loads project and runtime configuration
- detects and aggregates embedded guidance instructions
- optionally applies additional system instructions
- sends a prompt to the selected GenAI provider/model
- writes back generated or updated content according to the guidance rules

### Architecture (C4)

![C4 Diagram](./images/c4-diagram.png)

At a high level, the system consists of a command-line entry point that bootstraps configuration and selects a processing mode. A processing pipeline walks the project tree (or matches patterns), filters excluded paths, and for each eligible file builds a prompt from embedded guidance and configured instructions. Provider management routes requests to the selected GenAI backend and tracks usage, while processors handle concurrency, I/O, and applying results back into the working tree.

## Machai Ghostwriter vs. Other Tools

The closest commonly known tool in spirit is **Claude Code** (agentic coding in a repository context). Both can operate across multiple files and can be used in automation workflows; however, Ghostwriter is purpose-built around *guided file processing* and repository-embedded directives, making it especially strong for repeatable documentation and project-wide transformations.

### Key similarities (Ghostwriter vs. Claude Code)

- Can work across many files in a repository, not just a single snippet.
- Can be used as part of scripted/automated workflows.
- Uses an LLM backend and can be configured to different models/providers.

### Key differences

- **Guidance-in-repo first:** Ghostwriter’s primary control plane is `@guidance` embedded directly in files; this creates deterministic, reviewable intent living next to the content.
- **Batch scanning model:** Ghostwriter is designed to scan and process directories/patterns as a batch job (good fit for CI/CD and scheduled maintenance).
- **Documentation engine focus:** While agentic tools optimize for interactive development, Ghostwriter centers on continuously assembling and updating documentation and project site content.

### Brief comparison to other popular tools

- **GitHub Copilot / Cursor / Tabnine:** primarily interactive IDE assistants; great for in-editor completion and chat, but not typically designed for repository-wide, guidance-driven documentation runs.
- **Claude Code:** closest in repository-wide capability, but Ghostwriter emphasizes embedded guidance, repeatability, and documentation assembly.

| Tool | Project-wide automation (batch scanning) | Custom guidance embedded in files | CI/CD friendly CLI execution | Documentation generation/refresh |
|---|---:|---:|---:|---:|
| Machai Ghostwriter | Yes | Yes | Yes | Yes |
| Claude Code | Partial | Partial | Yes | Partial |
| GitHub Copilot | No | No | Partial | Partial |
| Cursor | Partial | No | Partial | Partial |
| Tabnine | No | No | Partial | Partial |

Machai Ghostwriter is unique in combining **repository-embedded guidance** with a **scanner-driven batch processor**, enabling consistent, repeatable, project-wide documentation and content maintenance.

## Key Features

- Scans directories or patterns (raw paths, `glob:` patterns, `regex:` patterns).
- Extracts embedded `@guidance` directives from project files and uses them as the source of truth.
- Supports system instructions from inline text, URL content, or `file:` references.
- Excludes paths via configuration or CLI.
- Optional Act mode for executing predefined prompts.
- Configurable degree of concurrency for faster processing.
- Optional request input logging to dedicated log files.

## Getting Started

### Prerequisites

- Java 8 (as configured by `maven.compiler.release=8`).
- Network access to your selected GenAI provider endpoint (depending on provider).
- A Ghostwriter configuration file (defaults to `gw.properties` in the resolved Ghostwriter home directory).

### Download

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```bash
java -jar gw.jar <scanDir> [options]
```

Examples from the built-in help:

```bash
java -jar gw.jar C:\\projects\\project
java -jar gw.jar src\\project
java -jar gw.jar "glob:**/*.java"
java -jar gw.jar "regex:^.*/[^/]+\\.java$"
```

### Typical Workflow

1. Add or refine `@guidance` directives in the files you want maintained (docs, site pages, source headers, configs, etc.).
2. Configure defaults in `gw.properties` (provider/model, excludes, threads, optional default act/prompt settings).
3. Run Ghostwriter locally against a directory or pattern to validate changes.
4. Commit results and optionally run Ghostwriter in CI to keep documentation and generated content synchronized.

### Java Version

Ghostwriter targets **Java 8**. Ensure your runtime is Java 8+ and that your chosen GenAI provider configuration is available at runtime (credentials, endpoints, etc., as required by the provider implementation).

## Configuration

Ghostwriter reads configuration primarily from `gw.properties` located in the resolved home directory. The home directory is determined in this order:

1. `gw.home` (if set)
2. the CLI-provided project directory (`-d/--project.dir`)
3. the current user directory

The configuration file path can be overridden with the system property `gw.config`.

### Command-Line Options

Options are parsed by `org.machanism.machai.gw.processor.Ghostwriter`.

| Option | Description | Default |
|---|---|---|
| `-h`, `--help` | Show help message and exit. | n/a |
| `-d`, `--project.dir <path>` | Root directory for file processing. | `project.dir` from config; otherwise current user directory |
| `-t`, `--threads <n>` | Degree of concurrency to improve processing performance. | `gw.threads` from config (unset means implementation default) |
| `-m`, `--model <provider:model>` | GenAI provider and model (e.g., `OpenAI:gpt-5.1`). | `gw.model` from config (required overall) |
| `-i`, `--instructions [value]` | System instructions as text, URL (`http(s)://...`), or file (`file:...`). If used without a value, prompts via stdin; supports multi-line input using `\` line continuation. | `instructions` from config |
| `-e`, `--excludes <csv>` | Comma-separated list of directories to exclude. | `gw.excludes` from config |
| `-l`, `--logInputs` | Log LLM request inputs to dedicated log files. | `false` (or `Genai.LOG_INPUTS_PROP_NAME` from config) |
| `-as`, `--acts <path>` | Directory containing predefined act prompt files. | `acts.location` from config |
| `-a`, `--act [value]` | Run in Act mode (interactive execution of predefined prompts). If used without a value, prompts via stdin. | `gw.act` from config (when Act mode is used) |

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
java -jar gw.jar src -a "Summarize repository" -as ./acts
```

## Resources

- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- GitHub (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
- Project site canonical URL: https://machai.machanism.org/ghostwriter/index.html
