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

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/ghostwriter/bindex.json)

## Introduction

Ghostwriter is an advanced documentation engine and command-line processor for project-wide, AI-assisted file processing. It scans source code, tests, documentation, site content, configuration, and other project artifacts; applies embedded guidance; and uses a configured generative-AI provider to make focused, repeatable updates. This approach helps teams automate documentation maintenance and repository-wide transformations while retaining instructions close to the content they govern.

The project is based on [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): guidance tags turn a file into an explicit, reviewable contract for an AI-assisted processing run. Ghostwriter also supports Act mode for reusable, episode-driven workflows and can be integrated into scripted or CI/CD processes.

## Overview

Ghostwriter resolves project layout and runtime configuration, walks selected paths, filters excluded content, and delegates each supported artifact to an appropriate processor or reviewer. Guidance mode discovers directives in files and applies them through format-aware processing. Act mode loads a named workflow, shares context between episodes, and exposes host tools for files, commands, web resources, acts, and project context. Both modes use the configured GenAI provider and record operational usage and logging information.

The project structure is organized around a command-line boundary, configuration and project-layout services, a scanning and AI-processing core, guidance and Act orchestration, format-specific reviewers, and provider function tools. Local project resources are read or updated through a file-system boundary, while remote instructions, APIs, and the GenAI service are accessed through explicit external boundaries. This separation keeps project traversal, workflow control, AI interaction, and host operations extensible and testable.

![Ghostwriter C4 component diagram](./images/c4-diagram.png)

The diagram shows a user invoking the CLI, which selects Guidance or Act mode. The scanning engine uses project metadata and the local project system; AI processing combines configuration, layout context, reviewers, provider management, and registered tools; and external resources supply remote instructions or model responses.

## Machai Ghostwriter vs. Other Tools

The closest comparable tool is **Aider** because both can operate across an entire repository rather than only completing the current editor buffer, can be driven from a terminal, and can be scripted as part of engineering workflows. Aider is primarily an interactive coding assistant centered on conversational edits and version-control review. Ghostwriter is a guided file-processing and documentation engine: its guidance tags, format-aware reviewers, reusable Acts, host function tools, and Maven delivery model make repository automation and documentation generation first-class concerns.

### Similarities and differences with Aider

**Similarities**

- Both use a configured LLM to understand repository context and modify multiple project artifacts.
- Both support terminal-oriented operation, explicit prompts, and model selection.
- Both can be wrapped by scripts or CI jobs and extended through project-level conventions.

**Differences**

- Ghostwriter uses inline `@guidance` contracts and dedicated reviewers for supported formats; Aider generally relies on conversational instructions and repository context.
- Ghostwriter provides Guidance and Act modes, reusable episode workflows, project-scoped context variables, and host tools for files, commands, REST/web content, and nested acts.
- Ghostwriter is distributed as a Java/Maven CLI pack and is designed for documentation generation and repeatable project processing; Aider is a Python-based coding assistant commonly used interactively.
- Ghostwriter can process documentation, site resources, diagrams, configuration, and source files under one scanning model, whereas Aider is primarily optimized for code changes and their associated tests.

### Broader comparison

| Tool | Project-wide automation | Custom guidance | CI/CD integration | Documentation generation |
|---|---|---|---|---|
| **Machai Ghostwriter** | **Yes** — scanners, Acts, paths, and excludes | **Yes** — `@guidance` tags and Act definitions | **Yes** — CLI, scripts, and delivery pack | **Yes** — a primary use case |
| Aider | Yes — repository-aware sessions and scripting | Partial — prompt and repository conventions | Yes — command-line automation | Partial — possible through prompts |
| Tabnine | Partial — mainly IDE/workspace assistance | Partial — team/enterprise configuration | Partial — depends on surrounding tooling | Partial — generated through assistant usage |
| GitHub Copilot | Partial — workspace/agent capabilities vary by product | Partial — instructions and repository context | Partial — strongest through GitHub workflow features | Partial — possible, but not its central workflow |
| Claude Code | Yes — agentic terminal repository workflows | Yes — project instructions and tool permissions | Yes — scriptable terminal execution | Partial — possible through prompts |
| Cursor | Partial — workspace agent and editor automation | Yes — project rules and context | Partial — generally mediated by scripts or external CI | Partial — possible, but editor-centric |

Ghostwriter is unique in combining explicit, versionable guidance contracts with a format-aware, project-wide scanner, reusable workflow orchestration, extensible host tools, and documentation-focused output in one CLI suitable for local and automated execution.

## Key Features

- Project-wide scanning of files, folders, glob patterns, and regular-expression paths.
- Guidance mode that discovers and processes embedded `@guidance` directives.
- Act mode for predefined prompts, inheritance, episodes, nested execution, and shared project context.
- Format-aware reviewers for Java, Markdown, PlantUML, HTML, Python, TypeScript, and text content.
- Configurable GenAI provider/model, instructions, exclusions, project directory, and concurrency.
- Extensible tools for reading, writing, patching, listing, command execution, web access, REST calls, and act control.
- Maven packaging, usage statistics, operational logging, and optional provider-input capture.

## Getting Started

### Prerequisites

- Java 8 or newer at runtime. The Maven build explicitly sets `maven.compiler.release` to `8`.
- A GenAI provider and model supported by the Machai GenAI client, normally supplied with `--model` or configuration. Provider credentials and endpoint settings must be available to that client.
- A project directory readable and writable by the process, with any referenced instruction or act resources accessible.
- Maven 3.x and network access to dependency repositories when building from source. The `pack` profile also expects `MACHANISM_PACK_DIR` for delivery-pack output.

### Machai Ghostwriter CLI Pack

[![Download Ghostwriter](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)

The related [bindex-core](https://machai.machanism.org/bindex-core/index.html) component provides project/library indexing support.

### Basic usage

After downloading and unpacking the delivery pack, run the executable JAR with a scan path:

```bash
java -jar gw.jar "glob:**/*.java"
```

The path may be a relative directory/name, a `glob:` pattern, or a `regex:` pattern. An absolute path must remain inside the project directory. With no positional path, Ghostwriter uses the configured path or `.`.

### Typical workflow

1. Build or download the CLI pack and provide the model provider credentials.
2. Set the project directory and any persistent properties, or pass the corresponding CLI options.
3. Put precise `@guidance` directives beside the files or folders they govern, or select a reusable Act.
4. Run a narrow scan first, review generated changes and logs, then expand to the project-wide path.
5. Run the command in CI/CD after tests or validation steps, review the resulting diff, and publish updated documentation or other artifacts.

The Java requirement is Java 8 because the Maven project targets release 8. The selected provider, valid credentials, network access where required, write permission for generated content, and sufficient provider quota are additional functional requirements.

## Configuration

Ghostwriter loads command-line values with precedence over persisted configuration. Its CLI options are defined by `org.machanism.machai.gw.processor.Ghostwriter`; `--help` prints the usage header and examples. Short and long forms are shown below.

| Option | Description | Default |
|---|---|---|
| `-h`, `--help` | Show help and exit without processing. | Disabled |
| `-d <dir>`, `--projectDir <dir>` | Set the project directory used for processing. | Current user directory |
| `-c <file>`, `--config <file>` | Set the configuration properties file. Relative paths are resolved from the initial project directory. | `gw.properties` in the initial project directory, unless the `gw.config` system property is set |
| `-t <n>`, `--threads <n>` | Set the number of concurrent processing threads. | Configuration value, otherwise processor default |
| `-m <provider:model>`, `--model <provider:model>` | Set the GenAI provider and model, such as `OpenAI:gpt-5.1`. | Configuration value, otherwise provider default/unset |
| `-i [text]`, `--instructions [text]` | Set system instructions; when supplied without a value, read them from standard input. | Configuration value, otherwise unset |
| `-e <list>`, `--excludes <list>` | Comma-separated directories or patterns to exclude. | Configuration value, otherwise unset |
| `-as <dir>`, `--acts <dir>` | Set the directory containing predefined Act prompt files. | Configuration value or built-in Act location |
| `-a [name]`, `--act [name]` | Enable Act mode and select an Act; without a value, prompt for its name. | Guidance mode |
| `<path>` | Positional scan path or pattern; multiple paths are accepted. | Configured path, otherwise `.` |

Ghostwriter reads persisted properties for the project directory, instructions, exclusions, threads, model, path, Act location, and selected Act. The `-c`/`--config` option selects the properties file; otherwise the `gw.config` system property is used when set, falling back to `gw.properties`. A relative configuration-file path is resolved from the initial project directory, and a missing default configuration file is tolerated; an explicitly selected file that cannot be loaded causes startup to fail.

For example, this command selects a project, model, concurrency, exclusions, and a Markdown scan:

```bash
java -jar gw.jar "glob:**/*.md" \
  --projectDir . \
  --model "OpenAI:gpt-5.1" \
  --threads 4 \
  --excludes "target,.git" \
  --instructions "Keep headings consistent and preserve public links."
```

To inspect the built-in syntax and examples directly, run:

```bash
java -jar gw.jar --help
```

The help output documents the positional path rules and examples for a Windows path, a relative path, a glob, and a regular expression. Act mode can be started with `--act` or `-a`; `--acts` or `-as` changes where predefined Act files are loaded from.

## Resources

- [Machai official platform](https://machai.machanism.org/)
- [Machai Ghostwriter documentation](https://machai.machanism.org/ghostwriter/index.html)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [Ghostwriter on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)
- [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html)
- [Ghostwriter CLI download](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)
- [bindex-core](https://machai.machanism.org/bindex-core/index.html)
