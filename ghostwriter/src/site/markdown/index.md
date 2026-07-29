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

Ghostwriter is an advanced repository-wide AI automation and documentation engine in the Machai ecosystem. It scans project content, detects embedded `@guidance` instructions, and applies GenAI-assisted processing to source code, documentation, project site content, configuration, diagrams, and other relevant artifacts. The main benefit is that maintenance intent lives inside the repository itself, close to the files it governs, so updates become more repeatable, reviewable, and suitable for both local execution and CI/CD pipelines.

Its conceptual foundation is [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html). Instead of relying only on one-off prompts, Ghostwriter turns persistent repository guidance into structured project automation, enabling governed updates across the full range of project file types.

## Overview

Ghostwriter is a command-line application that processes directories and pattern-based targets across a project. It loads runtime configuration, resolves the active project layout, applies include and exclude rules, selects the configured GenAI provider and model, and then performs guided AI-driven updates over eligible artifacts in the repository.

In its default guidance-driven flow, it traverses the project, discovers embedded directives, prepares project-aware prompts, and writes generated updates back to the working tree. In Act mode, it executes reusable prompt workflows with controlled access to local files, command execution, web content, REST APIs, act definitions, and project-context variables.

### Architecture

![C4 Diagram](./images/c4-diagram.png)

The diagram shows a layered command-line system centered on a runtime entry point that initializes execution and dispatches work. A configuration layer provides runtime settings, while project-layout resolution supplies metadata about modules and important source, test, and documentation areas. A shared scanning and AI-processing pipeline handles repository traversal, filtering, prompt construction, provider interaction, and write-back across supported artifact types. On top of this pipeline, one execution path focuses on embedded guidance found in governed repository content, while another focuses on reusable act-driven workflows. Supporting integrations expose controlled access to local files, remote resources, and provider tools, and a logging and usage layer records operational activity.

### Machai Ghostwriter vs. Other Tools

The closest widely known tool to Machai Ghostwriter is **Claude Code**. Both support multi-file, repository-level work instead of limiting AI assistance to inline completion. Claude Code is the nearest match because it combines agentic execution, tool use, and automation-friendly workflows, but Ghostwriter is more explicitly designed for repeatable project-wide processing driven by repository-embedded guidance, CLI execution, and extensible reusable acts.

#### Key similarities

- Both can operate across multiple files in a repository.
- Both support AI-assisted workflows beyond editor autocomplete.
- Both can be used in automation-oriented engineering tasks and CI/CD scenarios.
- Both rely on configurable LLM-backed execution with tool-assisted capabilities.

#### Key differences

- **Guidance-first workflow:** Ghostwriter is built around persistent `@guidance` directives stored in project files, while Claude Code is primarily oriented around interactive agent sessions.
- **Repeatable batch execution:** Ghostwriter is optimized for deterministic scanning of directories and patterns, making it especially suitable for scheduled maintenance and governed automation jobs.
- **Broader governed artifact maintenance:** Ghostwriter is designed to maintain documentation, project site pages, diagrams, configuration, and other repository assets in addition to source code.
- **Reusable acts and extensibility:** Ghostwriter supports TOML-based acts, episode control, and explicit tool registration for reusable workflows.

**Machai Ghostwriter** is a specialized, repository-wide developer automation and documentation engine from the **Machanism** ecosystem. Rather than acting as a standard conversational chatbot, Ghostwriter acts as a headless "ghostwriter" built directly into local developer environments and CI/CD pipelines to programmatically align code, documentation, and diagrams.

To see how Machai Ghostwriter stacks up against industry giants, here is a detailed breakdown and comparison of its architecture, modes, and features against popular AI coding agents like **GitHub Copilot / Copilot Workspace**, **Cursor / Windsurf**, **Aider**, and **Devin / Sweep**.

#### 1. Architectural Philosophy of Machai Ghostwriter
Unlike tools that write code interactively, Machai Ghostwriter treats GenAI as an **interpreter for human language**. Its core philosophy relies on keeping the developer strictly "in the loop", refusing to execute unpredictable autonomous logic without explicit directions. 

It operates in two main modes:
1. **Guided Mode (`gw`):** Scans files for native comments prefixed with `@guidance`. It extracts these embedded natural-language commands and executes them on that specific file (overriding project-level fallbacks) to generate diagrams, sync documentation, or compile API signatures.
2. **Act Mode (`act`):** Used for ad-hoc, command-line commands across targeted path (e.g., `-Dgw.act=">Rewrite headings for clarity"`) without modifying source files beforehand.

It enforces strict scoping via the **Root Directory (`rootDir`)**—which maps workspace context and detects project types (Maven, Gradle, Node.js)—and the **Scanning Directory (`path`)**—which restricts the AI’s focus to save context-token costs.

#### 2. Comparison Matrix: Ghostwriter vs. Popular Agents

| Dimension | **Machai Ghostwriter** | **GitHub Copilot / Workspace** | **Cursor / Windsurf** | **Aider** | **Devin / Sweep** |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Primary Paradigm** | **Guided File Processing** (Deterministic, human-in-the-loop annotations) | **Interactive Co-Pilot** (Reactive code completion, inline generation) | **AI-Native IDE** (Interactive multi-file editing, chat, composer) | **CLI Pair Programmer** (Interactive chat, git-driven code edits) | **Autonomous Agent** (Goal-oriented task loops, execution sandbox) |
| **Control Model** | **High & Structured**: Operates strictly via embedded `@guidance` tags or CLI-defined `act` scopes. | **User-driven**: Triggered via tab completions, inline prompts, or chat windows. | **Interactive**: User approves edits block-by-block inside the editor workspace. | **Collaborative**: Edits local files in place; git commit checkpointing. | **Low/Delegated**: Operates in a sandbox; user reviews end results. |
| **Integration Point** | Build tools (**Maven plugin**, Java CLI) & native code comments. | **IDE Extensions** (VS Code, JetBrains, Xcode) & GitHub PRs. | **Standalone IDE** (Forked fork of VS Code) or tailored windows. | **Terminal/CLI** integrated directly with local Git repositories. | **Web Browser Sandbox** or remote GitHub Webhooks (for Sweep). |
| **Key Strengths** | Aligning docs with code, updating Mermaid/PlantUML diagrams, mass formatting. | Fast auto-completions, answering general questions in IDE, PR description generation. | Understanding whole-codebase context, multi-file edits, inline code refactoring. | Editing code logic natively via terminal, auto-committing, fast iteration. | Solving complex, end-to-end bugs autonomously; running tests. |
| **Target Audience** | Software Architects, Technical Writers, CI/CD Devops Engineers. | Individual generalist software developers. | Power-user developers seeking AI-first editing workflows. | Command-line power users, terminal advocates, script developers. | Product teams wanting to delegate entire features or bug backlogs. |

#### 3. Detailed Comparison: Where Ghostwriter Differs

##### A. Guided Automation vs. Autonomous Drift
* **The Ghostwriter Approach:** In standard agents (like Devin or Sweep), you give the AI a goal, and it executes a loop to write code. While powerful, this can lead to "autonomous drift," where the AI generates messy logic or introduces breaking changes. Ghostwriter solves this using `@guidance` annotations. By locking instructions directly inside native code comments, the AI only updates files when compile-time commands are run, ensuring the code remains standard-compliant and deterministic.
* **The Popular Agents Approach:** Cursor, Windsurf, and Aider focus heavily on the creative side of coding—helping you write new features, refactor code, and solve bugs on-the-fly. They are conversational and highly interactive but do not leave a reproducible "build rule" behind for future build pipelines.

##### B. Documentation and Diagram Alignment
* **The Ghostwriter Approach:** Ghostwriter shines at preventing **documentation drift**. In a growing project, keeping `README.md` files, API tables, and architecture diagrams (like Mermaid or PlantUML) synchronized with changing Java or TypeScript classes is tedious. Ghostwriter automates this seamlessly (e.g., reading a Java class and auto-updating an associated diagram based on `@guidance` rules).
* **The Popular Agents Approach:** While Copilot or Cursor can write markdown files if prompted, they lack an integrated build-cycle mechanism (like a Maven goal or CLI pipeline sweep) designed to enforce documentation checks natively every time you build the project.

##### C. Build Pipeline vs. Interactive IDE
* **The Ghostwriter Approach:** Ghostwriter is designed as a **CI/CD friendly tool**. Since it can compile into a lightweight CLI tool (`gw.jar`) or run via a Maven Plugin (`gw-maven-plugin`), it fits neatly into automated integration scripts. You can run `mvn gw:act -Dgw.act=">Update version numbers" -Dgw.path=docs/` as part of a release action.
* **The Popular Agents Approach:** GitHub Copilot, Windsurf, and Cursor are built primarily as **highly-visual, interactive IDE experiences**. They require active human feedback (clicking "Accept," typing chats) and are not meant to run headless inside an offline build execution cycle.

#### 4. Summary: Which Should You Use?

* **Choose Machai Ghostwriter if:** You are working on multi-module, highly structured enterprise architectures (especially JVM-based like Maven/Gradle) where keeping documentation, architecture diagrams, and configuration files in sync with source code is a high priority. It is also ideal if you want repeatable, automated repository updates built directly into your local compilation or CI/CD stages.
* **Choose Cursor, Copilot, or Aider if:** You want an interactive assistant that helps you write lines of code faster, troubleshoots errors on the fly, refactors functions inside your editor, and acts as a reactive programming pair.
* **Choose Devin or Sweep if:** You want a fully autonomous worker that can take a Jira ticket or GitHub Issue, investigate it, write a patch, test it, and submit a PR without you having to guide the line-by-block coding process.

## Key Features

- Scans directories, glob patterns, and regex-based targets.
- Detects embedded `@guidance` directives in repository files.
- Processes source code, documentation, project site content, configuration, diagrams, and other relevant artifacts.
- Integrates with configurable GenAI providers and models.
- Supports additional system instructions from plain text, URLs, files, or standard input.
- Applies exclusion rules for selective processing.
- Provides Act mode for reusable TOML-based prompt workflows.
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

## Machai Ghostwriter CLI Pack

Download the Machai Ghostwriter CLI Pack:

[![Download Ghostwriter](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)

This package provides the `gw.jar` file, which incorporates the [bindex-core](https://machai.machanism.org/bindex-core/index.html) library and all required dependencies for seamless operation.

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
2. Configure model, scan defaults, excludes, instructions, and optional acts in `gw.properties`.
3. Run Ghostwriter against a directory or pattern target.
4. Review generated changes in version control.
5. Re-run locally or in CI/CD to keep governed project artifacts current.

### Java Version

Ghostwriter requires **Java 8+**. Practical use also requires a valid GenAI provider and model configuration, plus any connectivity needed by the selected provider.

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
| `-i`, `--instructions [value]` | Specify system instructions as plain text, by URL, or by file path. If used without a value, instructions are read from standard input. Each line is processed, preserving blank lines and resolving URL and file references. | `instructions` from configuration |
| `-e`, `--excludes <csv>` | Specify a comma-separated list of directories to exclude from processing. | `gw.excludes` from configuration |
| `-l`, `--logInputs` | Log LLM request inputs to dedicated log files. | `false` unless enabled in configuration |
| `-as`, `--acts <path>` | Specify the path to the directory containing predefined act prompt files for processing. | `gw.acts` from configuration |
| `-a`, `--act [value]` | Run Ghostwriter in Act mode. If used without a value, Ghostwriter prompts for the act text interactively. | `gw.act` from configuration when applicable |

The positional `<path>` argument defines the scan target. According to the built-in help, it may be a relative path with respect to the current project directory, an absolute path located within the root project directory, a raw directory name, a glob pattern such as `glob:**/*.java`, or a regex pattern such as `regex:^.*/[^/]+\\.java$`. If no scan target is supplied, Ghostwriter falls back to `gw.path` from configuration and then to `.`.

### Example

```bash
java -Dgw.config=gw.properties -jar gw.jar src \
  -d . \
  -m OpenAI:gpt-5.1 \
  -t 4 \
  -e ".git,target" \
  -i ">>>file:./instructions.txt" \
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
