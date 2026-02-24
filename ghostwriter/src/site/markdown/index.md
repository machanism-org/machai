---
canonical: https://machai.machanism.org/ghostwriter/index.html
---

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
- Use Markdown syntax for headings, lists, tables, code blocks, and links.
-->

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

> No mainstream tool offers the full range of features provided by Machai Ghostwriter out of the box.
>
>  ― <cite>&copy; OpenAI</cite>

## Introduction

Ghostwriter is Machai’s guidance-driven, repository-scale documentation and transformation engine.

It scans your repository (source code, docs, project-site Markdown, build metadata, and other artifacts), extracts embedded `@guidance:` directives, and uses a configured GenAI provider to apply consistent improvements across many files in a repeatable way. This makes it practical to keep documentation, conventions, and refactors aligned across an entire project—especially when changes must be deterministic, reviewable, and CI-friendly.

Ghostwriter is built on **[Guided File Processing](https://www.machanism.org/guided-file-processing/index.html)**: guidance lives next to the content it controls, and the processor composes those local directives—plus any configured defaults—into a structured prompt per file. The result is automation that remains explicit and version-controlled inside the repository.

## Overview

At a high level, Ghostwriter runs as a CLI that:

1. Resolves the project root and scan target (directory, `glob:...`, or `regex:...`).
2. Traverses the project (Maven multi-module aware).
3. For each supported file type, extracts embedded `@guidance:` directives using pluggable reviewers.
4. Composes an LLM request input that can include:
   - environment constraints (OS, project layout, etc.),
   - per-file guidance (or fallback default guidance),
   - optional global instruction blocks.
5. Sends the request to the configured provider/model and applies the resulting updates.

The core value proposition is **documentation and refactoring at repository scale**, while keeping intent explicit via embedded guidance and preserving auditability through version control (and optional input logging).

## Machai Ghostwriter vs. Other Tools

The closest mainstream tool conceptually is **[Claude Code](https://www.anthropic.com/claude-code)**: it can operate across multiple files and can be used in automated workflows. Ghostwriter, however, is purpose-built for **repeatable, guidance-driven batch processing** as a CLI (and Maven-friendly artifact), rather than an interactive agent primarily optimized for ad-hoc developer sessions.

### Key similarities

- **Multi-file changes:** both can apply edits across multiple files in a repository.
- **Automation potential:** both can be used in scripted or CI workflows (Ghostwriter directly; Claude Code via your integration).
- **Repository context:** both can use broader project context to produce coherent changes.

### Key differences

- **Guidance-first operation:** Ghostwriter extracts embedded `@guidance:` directives from many file types (source, docs, site content, build files) and composes them into a prompt per file.
- **Deterministic batch processing:** Ghostwriter’s primary workflow is scanning a target (directory/`glob:`/`regex:`) and applying updates systematically, including Maven multi-module traversal.
- **Extensibility via reviewers and tools:** file-type handling is pluggable via reviewer implementations; function tools can be attached via a loader mechanism.
- **Auditability:** Ghostwriter can persist composed request inputs per file when input logging is enabled.

### Brief comparison to other popular tools

- **GitHub Copilot / Tabnine / Cursor:** primarily IDE/editor copilots designed for interactive completion and chat; they do not center on repository-wide, `@guidance:`-driven enforcement across documentation and project-site content.
- **Claude Code:** closer to Ghostwriter in multi-file capabilities, but typically driven by interactive sessions rather than guidance embedded directly in the files being processed.

### Summary table

| Tool | Project-wide automation | Custom guidance embedded in files | CI/CD integration | Documentation generation |
|---|---:|---:|---:|---:|
| **Machai Ghostwriter** | Yes | Yes (`@guidance:`) | Yes | Yes |
| **Claude Code** | Yes | Partial (prompting/conventions) | Possible | Possible |
| **GitHub Copilot** | Limited | No | Limited | Partial |
| **Cursor** | Limited | Partial (workspace rules) | Limited | Partial |
| **Tabnine** | Limited | No | Limited | Limited |

Machai Ghostwriter is unique because it makes **version-controlled, per-file guidance** the primary interface for reliable, repeatable repository-wide improvements.

## Key Features

- Processes many project file types (not just Java), including documentation and project-site Markdown.
- Extracts embedded `@guidance:` directives via pluggable, file-type-aware reviewers.
- Supports scan targets as a directory, `glob:` matcher, or `regex:` matcher.
- Maven multi-module traversal (child modules first).
- Optional multi-threaded module processing (when the provider is thread-safe).
- Optional logging of composed LLM request inputs.
- Supports global instructions and default guidance loaded from plain text, URLs, or local files.

## Getting Started

### Prerequisites

- **Java**
  - **Build target:** Java **8** (from `pom.xml`: `maven.compiler.release=8`).
  - **Runtime:** depends on your selected GenAI provider/client; you can run with a newer JRE if required by the provider SDK while still building at the configured release level.
- **GenAI provider access and credentials** as required by your provider (for example via `GW_HOME\\gw.properties`, environment variables, or provider-specific configuration).
- **Network access** to the provider endpoint (if applicable).

### Download

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/gw.zip/download)

### Basic Usage

```text
java -jar gw.jar <scanTarget> [options]
```

Example (scan a folder on Windows):

```text
java -jar gw.jar src\\main\\java
```

### Typical Workflow

1. Add `@guidance:` directives to the files you want Ghostwriter to update (Markdown under `src\\site`, Java sources, templates, etc.).
2. Choose a scan target:
   - directory path (relative to the project), or
   - `glob:` matcher (example: `glob:**/*.java`), or
   - `regex:` matcher.
3. Configure your GenAI provider/model and credentials.
4. Optionally add global instructions and/or default guidance.
5. Run Ghostwriter, then review and commit the results.

### Java Version

Ghostwriter is compiled with **Java 8** (`maven.compiler.release=8`). You can usually run it on a newer JVM, but any selected provider/client libraries may impose additional runtime requirements.

## Configuration

### Command-Line Options

The CLI options are defined in `org.machanism.machai.gw.processor.Ghostwriter`:

- `-h`, `--help` — Show help and exit.
- `-r`, `--root <path>` — Specify the root directory used as the base for relative paths.
- `-t`, `--threads[=<true|false>]` — Enable multi-threaded module processing; if specified without a value, it enables it.
- `-a`, `--genai <provider:model>` — Set the GenAI provider and model (example: `OpenAI:gpt-5.1`).
- `-i`, `--instructions[=<text|url|file:...>]` — Provide global system instructions. When used without a value, you are prompted to enter multi-line text via stdin.
- `-g`, `--guidance[=<text|url|file:...>]` — Provide default guidance (fallback). When used without a value, you are prompted to enter multi-line text via stdin.
- `-e`, `--excludes <csv>` — Comma-separated list of directories to exclude from processing.
- `-l`, `--logInputs` — Log composed LLM request inputs to dedicated log files.

Notes on `--instructions` and `--guidance` values:

- blank lines are preserved,
- lines beginning with `http://` or `https://` are fetched and included,
- lines beginning with `file:` are read from the referenced file and included,
- other lines are included as-is.

### Options Table

| Option | Argument | Description | Default |
|---|---|---|---|
| `-h`, `--help` | none | Show help message and exit. | n/a |
| `-r`, `--root` | `path` | Root directory used as the base for relative scan targets and `file:` includes. | From config key `gw.rootDir`; otherwise current working directory. |
| `-t`, `--threads` | `true`/`false` (optional) | Enable multi-threaded module processing; if used without a value, it enables it. | From config key `gw.threads` (default `false`). |
| `-a`, `--genai` | `provider:model` | GenAI provider/model identifier. | From config key `gw.genai`; otherwise must be provided. |
| `-i`, `--instructions` | text/url/file (optional) | Global system instructions appended to every prompt; supports `http(s)://...` and `file:...`; prompts via stdin if no value. | From config key `gw.instructions`; otherwise none. |
| `-g`, `--guidance` | text/url/file (optional) | Fallback guidance used when files have no embedded `@guidance:`; supports `http(s)://...` and `file:...`; prompts via stdin if no value. | From config key `gw.guidance`; otherwise none. |
| `-e`, `--excludes` | csv | Comma-separated exclude list. | From config key `gw.excludes`; otherwise none. |
| `-l`, `--logInputs` | none | Log composed LLM inputs to per-file log files. | From config key `gw.logInputs` (default `false`). |

### Example

The built-in help text documents supported scan targets:

- raw directory names,
- `glob:` patterns (for example `glob:**/*.java`),
- `regex:` patterns.

Example (Windows): scan Java sources via glob, enable threads, set provider/model, add instructions and default guidance, exclude common folders, and log inputs:

```text
java -jar gw.jar "glob:**/*.java" -t -a OpenAI:gpt-5.1 -i file:project-instructions.txt -g file:default-guidance.txt -e target,.git -l
```

## Default Guidance

`defaultGuidance` is a fallback instruction block used when a file does not contain embedded `@guidance:` directives.

### Purpose

When Ghostwriter processes a file, it first asks the reviewer for that file type to extract embedded `@guidance:` directives. If no guidance is found, `defaultGuidance` provides a project-wide baseline so the file can still be updated in a consistent way.

### How it’s set

- CLI: `-g` / `--guidance` (plain text, `http(s)://...`, or `file:...`; supports stdin when provided without a value)
- API: `FileProcessor#setDefaultGuidance(String)`

### How it’s interpreted

The value is parsed line-by-line:

- blank lines are preserved,
- lines beginning with `http://` or `https://` are fetched and included,
- lines beginning with `file:` are read from the referenced file and included,
- other lines are included as-is.

This makes it easy to keep shared guidance in version-controlled files (or hosted documents) while still allowing simple inline defaults.

## Resources

- Official platform: https://machai.machanism.org/ghostwriter/
- GitHub (SCM): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter
