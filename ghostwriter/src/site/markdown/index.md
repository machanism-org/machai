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
2. **Quote**
   - Do not change this section. Leave it exactly as provided.
3. **Introduction**
   - Provide a comprehensive description of the project's purpose and main benefits.
   - Reference [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) as the conceptual foundation for Machai Ghostwriter.
4. **Overview**
   - Clearly explain the core functionality and value proposition of the project.
   Describe the project with diagrams bellow:
     - Create a project structure overview based on the `.puml` files below.
     - Describe the project without including file names in the description.
     - Use the project structure diagram by the path: `./images/c4-diagram.png` (`src/site/puml/c4-diagram.puml`).
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
canonical: https://machai.machanism.org/ghostwriter/index.html
---

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

> A guided file processing engine for generating and maintaining project-wide documentation and code improvements with AI.

## Introduction

Machai Ghostwriter is a guided, AI-assisted processing engine that runs across an entire repository—source code, tests, documentation, and other project assets—to generate and maintain project-wide documentation and code improvements.

Its conceptual foundation is [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): instead of treating files as isolated inputs, Ghostwriter treats a repository as a structured system where each file can carry its own embedded guidance and the tool orchestrates consistent processing across the project.

Main benefits:

- **Guidance-first prompting**: instructions live next to the content they govern via embedded `@guidance:` blocks.
- **Repository-scale consistency**: deterministic scanning, per-type reviewers, and injected project context make runs repeatable.
- **Automation-ready**: designed for non-interactive execution and integration into scripts and CI/CD pipelines.

## Overview

Ghostwriter is delivered as a Java CLI that scans a target directory or a matcher pattern, extracts embedded guidance directives from supported files, composes project-aware prompts, executes a configured GenAI provider, and writes results back into the workspace.

The following diagram illustrates the major responsibilities and how the components collaborate:

![Ghostwriter C4 diagram](./images/c4-diagram.png)

At a high level:

- A CLI run starts with a scan target (directory path, `glob:...`, or `regex:...`).
- A processor resolves project layout and configuration, then scans the workspace.
- Reviewers extract `@guidance:` directives and build a prompt with injected project context.
- A provider client executes the prompt against the configured GenAI backend.
- Outputs (and optionally input logs) are written under the project workspace.

## Machai Ghostwriter vs. Other Tools

The most similar tool in spirit is **Claude Code**, because both are designed for repository-scale work (beyond an editor buffer), can be used in automation contexts, and are suitable for scripted or CI/CD execution.

### Key similarities

- Repository-wide workflows spanning multiple files.
- Practical engineering focus (documentation, refactors, maintenance).
- Can be used non-interactively as part of an automated run.

### Key differences

- **Embedded guidance as the primary interface**: Ghostwriter is driven by file-local `@guidance:` directives (plus optional default guidance). Claude Code is typically driven by interactive task prompts.
- **Deterministic scanning and review**: Ghostwriter uses explicit scan targets (`dir`, `glob:`, `regex:`), extension-based reviewers, and configured excludes.
- **Extensibility model**: Ghostwriter integrates with the Machai ecosystem (project layout, GenAI client, function tools), and supports Act prompt bundles.
- **Distribution**: Ghostwriter is a Java CLI intended to run inside/alongside Java and Maven-based projects.

### Brief comparison to other assistants

- **GitHub Copilot / Cursor / Tabnine**: primarily editor-first (inline completions and interactive edits), with limited guidance-in-files concepts and limited batch, project-wide automation.
- **Claude Code**: closest for repo-scale actions, but Ghostwriter’s differentiator is turning embedded guidance into a repeatable scanning pipeline that can be rerun in CI.

### Capability summary

| Tool | Project-wide automation | Custom guidance in files (`@guidance:`) | CI/CD integration | Documentation generation |
|---|---:|---:|---:|---:|
| Machai Ghostwriter | Yes | Yes | Yes | Yes |
| Claude Code | Yes | No (prompt-driven) | Yes | Partial |
| GitHub Copilot | Limited | No | Limited | Limited |
| Cursor | Limited | No | Limited | Limited |
| Tabnine | Limited | No | Limited | Limited |

Machai Ghostwriter is unique in how it makes guidance part of the repository itself and turns it into a deterministic, repeatable, project-wide processing pipeline.

## Key Features

- Scans directories and patterns (`glob:` / `regex:`) and processes matching files.
- Extracts embedded `@guidance:` directives using per-file-type reviewers.
- Injects project layout and structure context into prompts.
- Supports additional system instructions and default guidance from plain text, URLs, or `file:` references.
- Supports excludes (exact paths or `glob:` / `regex:` patterns).
- Optional multi-threaded processing.
- Optional logging of provider input payloads per processed file.
- Act mode for running predefined prompts (`--act`) and custom act bundles (`--acts`).

## Getting Started

### Prerequisites

- Java **8** (per `maven.compiler.release` in `pom.xml`).
- A configured GenAI provider/model (set `gw.model` in `gw.properties` or pass `-m/--model`).
- (Optional) `gw.properties` to persist configuration.

### Download

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```bash
java -jar gw.jar src -m OpenAI:gpt-5.1
```

### Typical Workflow

1. Add `@guidance:` blocks to the files you want Ghostwriter to improve or document (code, docs, configs, site pages, etc.).
2. Create `gw.properties` (optional) and configure values such as:
   - `project.dir` (project root)
   - `gw.model` (provider:model)
   - `instructions` (optional system instructions)
   - `gw.excludes` (optional excludes)
3. Run Ghostwriter against a directory or pattern (e.g., `src`, `glob:**/*.md`, `regex:...`).
4. Review the resulting changes and iterate.

### Java Version

Ghostwriter requires **Java 8**. In addition, you must configure an accessible GenAI provider/model (for example via `gw.model` or `-m/--model`), otherwise the CLI fails fast.

## Configuration

Ghostwriter CLI options are defined in `org.machanism.machai.gw.processor.Ghostwriter`.

### Command-Line Options

- `-h, --help` — Show help message and exit.
- `-d, --projectDir <path>` — Root directory for file processing.
- `-t, --threads <count>` — Degree of concurrency for processing.
- `-m, --model <provider:model>` — Set the GenAI provider and model (e.g., `OpenAI:gpt-5.1`).
- `-i, --instructions[=<text|url|file:...>]` — Additional system instructions. If used without a value, reads multi-line text from stdin until a line does not end with `\`.
- `-e, --excludes <csv>` — Comma-separated list of directories/patterns to exclude.
- `-l, --logInputs` — Log LLM request inputs to dedicated log files.
- `-as, --acts <path>` — Directory containing predefined Act prompt files.
- `-a, --act[=<prompt>]` — Run in Act mode. If used without a value, reads multi-line text from stdin until a line does not end with `\`.

### Options Table

| Option | Description | Default |
|---|---|---|
| `-h, --help` | Show this help message and exit. | `false` |
| `-d, --projectDir <path>` | Root directory for file processing. | `project.dir` (if set) or current working directory |
| `-t, --threads <count>` | Degree of concurrency for processing. | `gw.threads` or unset |
| `-m, --model <provider:model>` | GenAI provider and model. | `gw.model` |
| `-i, --instructions[=<text\|url\|file:...>]` | System instructions (plain text/URL/`file:`). If no value: multi-line stdin. | `instructions` |
| `-e, --excludes <csv>` | Comma-separated excludes (exact paths or `glob:` / `regex:` patterns). | `gw.excludes` |
| `-l, --logInputs` | Log provider input payloads to log files. | `false` |
| `-as, --acts <path>` | Directory containing act prompt files. | `acts.location` |
| `-a, --act[=<prompt>]` | Act mode prompt. If no value: multi-line stdin. | `gw.act` |

### Example

This example runs on a pattern, provides default guidance via stdin, and enables input logging:

```bash
java -jar gw.jar "glob:**/*.md" -m OpenAI:gpt-5.1 -g -l
```

From the built-in help:

- `<scanDir>` may be a relative path (from the current project directory) or a `glob:` / `regex:` matcher.
- If an absolute scan path is provided, it must be located within the root project directory.
- If an option with an optional value (`-i/--instructions`, `-a/--act`) is used without a value, Ghostwriter reads multi-line input from stdin.
- When entering multi-line input interactively, end input when a line does not end with `\` (a trailing backslash continues the next line).

## Resources

- Project site: https://machai.machanism.org/ghostwriter/index.html
- GitHub repository (Machai mono-repo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
