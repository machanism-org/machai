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
   - Bindex Badge [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/ghostwriter/bindex.json)
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
7. **Machai Ghostwriter CLI Pack**  
     Add a download link for the Ghostwriter CLI delivery pack:  
     [![Download Ghostwriter](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download).
     [bindex-core](https://machai.machanism.org/bindex-core/index.html)
   - **Basic Usage:** Provide an example command to run the application.
   - **Typical Workflow:** Outline the step-by-step process for using the project artifacts.
   - **Java Version:** State the required Java version as defined in `pom.xml`, and clarify any additional functional requirements.
8. **Configuration**
   - **Command-Line Options:** Analyze `/java/org/machanism/machai/gw/processor/Ghostwriter.java` to extract and describe all available command-line options.
   - **Options Table:** Present a table listing each option, its description, and default value.
   - **Example:** Provide a command-line example showing how to configure and run the application with custom parameters. Include information from the `Ghostwriter.help()` method.
9. **Resources**
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
[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/ghostwriter/bindex.json)

## Introduction

Ghostwriter is an advanced repository-wide AI automation and documentation engine in the Machai ecosystem. It scans project content, detects embedded `@guidance` instructions, and applies GenAI-assisted processing to source code, documentation, project site content, configuration, diagrams, and other relevant artifacts. The project is designed to reduce documentation drift, automate repetitive maintenance, and keep human intent close to the files it governs.

Its conceptual foundation is [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html). Rather than relying only on one-off conversational prompts, Ghostwriter turns persistent repository guidance into repeatable, reviewable project automation that can run locally or in CI/CD pipelines.

## Overview

Ghostwriter is a Java command-line application that processes directories and pattern-based targets across a project. It loads runtime configuration, resolves the project layout, applies include and exclude rules, selects the configured GenAI provider and model, and performs guided AI-driven updates over eligible artifacts in the repository.

In guidance mode, Ghostwriter traverses project content, discovers embedded directives, prepares project-aware prompts, invokes the configured model, and writes generated updates back to the working tree. In Act mode, it executes reusable prompt workflows with controlled access to local files, command execution, web content, REST APIs, act definitions, and project-context variables.

### Architecture

![C4 Diagram](./images/c4-diagram.png)

The architecture is organized around a command-line runtime that initializes configuration and dispatches processing. A configuration layer supplies runtime settings, provider choices, paths, excludes, and optional instruction text. A project-layout layer provides repository metadata so processing can account for source, test, documentation, site, and configuration areas. The shared processing pipeline performs scanning, filtering, prompt preparation, GenAI interaction, and write-back. Guidance processing focuses on embedded repository instructions, while Act processing supports reusable workflow definitions and ad-hoc command execution. Supporting integrations provide controlled access to local and remote resources, and logging captures startup details, usage statistics, and optional LLM request inputs.

### Machai Ghostwriter vs. Other Tools

The closest widely known tool to Machai Ghostwriter is **Claude Code**. Both can operate across repositories, use tools, and assist with multi-file changes rather than only inline code completion. Claude Code is the nearest comparison because it supports agentic command-line workflows and project-level reasoning, but Ghostwriter is more specifically designed for repeatable project-wide automation driven by repository-embedded guidance, Maven/CLI execution, and reusable acts.

#### Key similarities

- Both support multi-file, repository-level work.
- Both go beyond autocomplete and can perform task-oriented engineering workflows.
- Both can be used from command-line-oriented environments.
- Both rely on configurable LLM-backed execution and tool-assisted context gathering.

#### Key differences

- **Persistent guidance:** Ghostwriter stores maintenance instructions in project files through `@guidance` directives; Claude Code is primarily session- and prompt-driven.
- **Repeatable batch execution:** Ghostwriter is optimized for scanning paths, glob patterns, and regex targets, making it suitable for scheduled repository maintenance and CI/CD jobs.
- **Documentation-first automation:** Ghostwriter explicitly targets documentation, project site content, diagrams, configuration, and source code as first-class project artifacts.
- **Reusable acts:** Ghostwriter supports predefined act workflows for repeatable automation with controlled tool access.
- **Build ecosystem fit:** Ghostwriter is distributed as a Java CLI and can be integrated into Maven-oriented delivery and automation flows.

#### Brief comparison with popular tools

Tabnine and GitHub Copilot focus primarily on editor-based completions and developer assistance. Cursor provides a richer AI-native IDE experience for interactive codebase edits. Claude Code provides the closest command-line, repository-aware agent workflow. Ghostwriter differs by treating project guidance and documentation maintenance as repeatable repository automation rather than an interactive coding session.

| Tool | Project-wide automation | Custom guidance | CI/CD integration | Documentation generation |
|---|---:|---:|---:|---:|
| Machai Ghostwriter | Yes | Yes, via embedded `@guidance` and acts | Yes, CLI/Maven-friendly | Yes, first-class focus |
| Claude Code | Yes | Yes, prompt/session and project instructions | Possible, command-line oriented | Yes, prompt-driven |
| GitHub Copilot | Limited | Limited to editor/chat context and repository instructions | Limited, mainly GitHub ecosystem features | Yes, interactive/prompt-driven |
| Cursor | Yes, interactive | Yes, IDE rules and prompts | Limited, IDE-centered | Yes, interactive/prompt-driven |
| Tabnine | Limited | Limited | Limited | Limited |

Machai Ghostwriter is unique because it combines repository-embedded guidance, broad project-file support, reusable act workflows, and CI/CD-friendly batch execution into a governed automation model for keeping code, documentation, diagrams, and configuration aligned.

## Key Features

- Scans directories, glob patterns, and regex-based targets.
- Detects embedded `@guidance` directives in repository files.
- Processes source code, documentation, project site content, configuration, diagrams, and other relevant artifacts.
- Integrates with configurable GenAI providers and models.
- Supports additional system instructions from plain text, URLs, files, or standard input.
- Applies exclusion rules for selective processing.
- Provides Act mode for reusable and ad-hoc prompt workflows.
- Supports configurable concurrency.
- Can log LLM request inputs for diagnostics and auditing.
- Records usage statistics at the end of processing.
- Fits both local development and CI/CD automation scenarios.

## Getting Started

### Prerequisites

- Java 8 or later, based on `maven.compiler.release` set to `8` in `pom.xml`.
- Access to a supported GenAI provider and any required credentials or network connectivity.
- A project or working directory containing files to scan and update.
- Optional `gw.properties` configuration in the Ghostwriter home directory, or a custom configuration file selected with the `gw.config` system property.
- Optional acts directory when using Act mode with predefined act definitions.
- Version control is strongly recommended so generated changes can be reviewed before commit.

## Machai Ghostwriter CLI Pack

Download the Machai Ghostwriter CLI Pack:

[![Download Ghostwriter](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)

The delivery pack provides `gw.jar` and the runtime dependencies needed to execute Ghostwriter from the command line. The pack also incorporates the [bindex-core](https://machai.machanism.org/bindex-core/index.html) library for Bindex-related functionality.

### Basic Usage

```bash
java -jar gw.jar <path> [options]
```

Examples:

```bash
java -jar gw.jar src
java -jar gw.jar "glob:**/*.java"
java -jar gw.jar "regex:^.*/[^/]+\\.java$"
```

### Typical Workflow

1. Add or update `@guidance` directives in the files Ghostwriter should maintain.
2. Configure provider/model, scan defaults, excludes, instructions, and optional acts in `gw.properties` or through command-line options.
3. Run Ghostwriter against a directory or pattern target.
4. Review generated changes in version control.
5. Re-run locally or in CI/CD to keep governed project artifacts current.

### Java Version

Ghostwriter requires **Java 8+**. Functional use also requires a valid GenAI provider/model configuration and any credentials or connectivity required by that provider.

## Configuration

Ghostwriter loads settings from `gw.properties` in the resolved home directory unless overridden with the `gw.config` system property. The home directory is resolved from `gw.home` when defined; otherwise it defaults to the current user directory. The project root is taken from `-d` / `--project.dir`, then from configuration, and otherwise falls back to the current user directory.

### Command-Line Options

The CLI options below are derived from `Ghostwriter.java` and the built-in help output.

| Option | Description | Default value |
|---|---|---|
| `-h`, `--help` | Show the help message and exit. | None |
| `-d`, `--project.dir <path>` | Specify the root directory for file processing. | `project.dir` from configuration, otherwise the current user directory |
| `-t`, `--threads <n>` | Set the degree of concurrency for processing. | `gw.threads` from configuration |
| `-m`, `--model <provider:model>` | Set the GenAI provider and model, for example `OpenAI:gpt-5.1`. | `gw.model` from configuration |
| `-i`, `--instructions [value]` | Specify system instructions as plain text, by URL, or by file path. Lines beginning with `http://` or `https://` are loaded from the URL, lines beginning with `file:` are loaded from the file path, blank lines are preserved, and other lines are used as-is. If the option is used without a value, Ghostwriter prompts for standard input. | `instructions` from configuration |
| `-e`, `--excludes <csv>` | Specify a comma-separated list of directories or patterns to exclude from processing. | `gw.excludes` from configuration |
| `-as`, `--acts <path>` | Specify the directory containing predefined act prompt files. | `gw.acts` from configuration |
| `-a`, `--act [value]` | Run Ghostwriter in Act mode for executing predefined or ad-hoc prompts. If used without a value, Ghostwriter prompts for act text interactively. | No act unless supplied on the command line or through `gw.act` when applicable |

The positional `<path>` argument defines the scan target. According to the built-in help, it may be a relative path with respect to the current project directory, an absolute path located within the root project directory, a raw directory name, a glob pattern such as `glob:**/*.java`, or a regex pattern such as `regex:^.*/[^/]+\\.java$`. If no scan target is supplied, Ghostwriter falls back to `gw.path` from configuration and then to `.`.

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
