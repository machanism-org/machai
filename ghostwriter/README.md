<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src\\site\\markdown\\index.md` content summary.
   - Add `![](src/site/resources/images/machai-ghostwriter-logo.png)` before the title.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)` after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
   - Add the Ghostwriter CLI application jar download link: [![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download) to the installation section.
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

![](src/site/resources/images/machai-ghostwriter-logo.png)

# Machai Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)

[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/ghostwriter/bindex.json)

Machai Ghostwriter is an advanced documentation engine and command-line processor for project-wide, AI-assisted file processing. It scans source code, tests, documentation, site content, configuration, and other project artifacts; applies embedded guidance; and uses a configured generative-AI provider to make focused, repeatable updates.

## Introduction

Ghostwriter is based on [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html). Guidance tags place explicit, reviewable instructions next to the content they govern, helping teams automate documentation maintenance and repository-wide transformations without separating instructions from the files they describe.

Ghostwriter supports both Guidance mode and Act mode. Guidance mode discovers directives embedded in project files and applies format-aware processing. Act mode runs reusable, episode-driven workflows with shared project context and extensible tools, making the same capabilities suitable for local work, scripts, and CI/CD automation.

## Overview

Ghostwriter resolves project layout and runtime configuration, walks selected paths, filters excluded content, and delegates each supported artifact to an appropriate processor or reviewer. It can process documentation, diagrams, configuration, site resources, and source files under one project-wide scanning model. The command-line boundary, configuration and project-layout services, scanning and AI-processing core, guidance and Act orchestration, format-specific reviewers, and provider tools are separated to keep processing extensible and testable.

![Ghostwriter C4 component diagram](src/site/resources/images/c4-diagram.png)

The diagram shows a user invoking the CLI, which selects Guidance or Act mode. The scanning engine uses project metadata and the local project system; AI processing combines configuration, layout context, reviewers, provider management, and registered tools; and explicit external boundaries provide remote instructions, APIs, and model responses.

## Machai Ghostwriter vs. Other Tools

The closest comparable tool is **Aider** because both operate across an entire repository, run from a terminal, and can be scripted for engineering workflows. Aider is primarily an interactive coding assistant centered on conversational edits and version-control review. Ghostwriter is a guided file-processing and documentation engine whose guidance tags, format-aware reviewers, reusable Acts, host function tools, and Maven delivery model make repository automation and documentation generation first-class concerns.

### Similarities and differences with Aider

**Similarities**

- Both use a configured large language model to understand repository context and modify multiple project artifacts.
- Both support terminal-oriented operation, explicit prompts, and model selection.
- Both can be wrapped by scripts or CI jobs and extended through project-level conventions.

**Differences**

- Ghostwriter uses inline `@guidance` contracts and dedicated reviewers for supported formats; Aider generally relies on conversational instructions and repository context.
- Ghostwriter provides Guidance and Act modes, reusable episode workflows, project-scoped context variables, and host tools for files, commands, REST/web content, and nested acts.
- Ghostwriter is distributed as a Java/Maven CLI pack and is designed for documentation generation and repeatable project processing; Aider is a Python-based coding assistant commonly used interactively.
- Ghostwriter processes documentation, site resources, diagrams, configuration, and source files under one scanning model, while Aider is primarily optimized for code changes and associated tests.

| Tool | Project-wide automation | Custom guidance | CI/CD integration | Documentation generation |
|---|---|---|---|---|
| **Machai Ghostwriter** | **Yes** — scanners, Acts, paths, and exclusions | **Yes** — `@guidance` tags and Act definitions | **Yes** — CLI, scripts, and delivery pack | **Yes** — a primary use case |
| Aider | Yes — repository-aware sessions and scripting | Partial — prompts and repository conventions | Yes — command-line automation | Partial — possible through prompts |
| Tabnine | Partial — mainly IDE/workspace assistance | Partial — team or enterprise configuration | Partial — depends on surrounding tooling | Partial — generated through assistant usage |
| GitHub Copilot | Partial — workspace and agent capabilities vary by product | Partial — instructions and repository context | Partial — strongest through GitHub workflow features | Partial — possible, but not its central workflow |
| Claude Code | Yes — agentic terminal repository workflows | Yes — project instructions and tool permissions | Yes — scriptable terminal execution | Partial — possible through prompts |
| Cursor | Partial — workspace agent and editor automation | Yes — project rules and context | Partial — generally mediated by scripts or external CI | Partial — possible, but editor-centric |

Ghostwriter is unique in combining explicit, versionable guidance contracts with a format-aware project-wide scanner, reusable workflow orchestration, extensible host tools, and documentation-focused output in one CLI suitable for local and automated execution.

## Key Features

- Project-wide scanning of files, folders, glob patterns, and regular-expression paths.
- Guidance mode for discovering and processing embedded `@guidance` directives.
- Act mode for predefined prompts, episodes, nested execution, and shared project context.
- Format-aware reviewers for Java, Markdown, PlantUML, HTML, Python, TypeScript, and text.
- Configurable GenAI provider/model, instructions, exclusions, project directory, and concurrency.
- Extensible tools for files, patches, commands, web resources, REST APIs, and Act control.
- Maven packaging, usage statistics, operational logging, and optional provider-input capture.

## Usage

### Prerequisites

- Java 8 or newer at runtime; the Maven project targets Java release 8.
- A supported GenAI provider and model, with credentials and endpoint settings available to the client.
- A project directory readable and writable by the process, with referenced instructions and Act resources accessible.
- Maven 3.x and network access to dependency repositories when building from source. The `pack` profile expects `MACHANISM_PACK_DIR` for delivery-pack output.

### Installation

Download and unpack the Ghostwriter CLI application pack:

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)

The related [bindex-core](https://machai.machanism.org/bindex-core/index.html) component provides project/library indexing support.

### Basic usage

After downloading and unpacking the delivery pack, run the executable JAR with a scan path:

```bash
java -jar gw.jar "glob:**/*.java"
```

Paths may be relative directories, `glob:` patterns, or `regex:` patterns. With no positional path, Ghostwriter uses the configured path or the current directory.

### Typical workflow

1. Build or download the CLI pack and provide the model provider credentials.
2. Set the project directory and persistent properties, or provide options on the command line.
3. Add precise `@guidance` directives beside the files they govern, or select a reusable Act.
4. Start with a narrow scan, review generated changes and logs, and then expand the scope.
5. Run the validated command in CI/CD and publish the updated documentation or other artifacts.

The selected provider, valid credentials, network access where required, write permission, and sufficient provider quota are additional functional requirements.

## Configuration

Ghostwriter loads command-line values with precedence over persisted configuration. Its CLI options are defined by `org.machanism.machai.gw.processor.Ghostwriter`; `--help` prints the usage header and examples.

| Option | Description | Default |
|---|---|---|
| `-h`, `--help` | Show help and exit without processing. | Disabled |
| `-d <dir>`, `--projectDir <dir>` | Set the project directory used for processing. | Current user directory |
| `-c <file>`, `--config <file>` | Set the configuration properties file. Relative paths are resolved from the initial project directory. | `gw.properties` in the initial project directory, unless the `gw.config` system property is set |
| `-t <n>`, `--threads <n>` | Set the number of concurrent processing threads. | Configuration value, otherwise processor default |
| `-m <provider:model>`, `--model <provider:model>` | Set the GenAI provider and model, such as `OpenAI:gpt-5.1`. | Configuration value, otherwise provider default or unset |
| `-i [text]`, `--instructions [text]` | Set system instructions; without a value, read them from standard input. | Configuration value, otherwise unset |
| `-e <list>`, `--excludes <list>` | Set comma-separated directories or patterns to exclude. | Configuration value, otherwise unset |
| `-as <dir>`, `--acts <dir>` | Set the directory containing predefined Act prompt files. | Configuration value or built-in Act location |
| `-a [name]`, `--act [name]` | Enable Act mode and select an Act; without a value, prompt for its name. | Guidance mode |
| `<path>` | Positional scan path or pattern; multiple paths are accepted. | Configured path, otherwise `.` |

Ghostwriter reads persisted properties for the project directory, instructions, exclusions, threads, model, path, Act location, and selected Act. The `-c`/`--config` option selects the properties file; otherwise the `gw.config` system property is used when set, falling back to `gw.properties`. A relative configuration-file path is resolved from the initial project directory, and a missing default configuration file is tolerated; an explicitly selected file that cannot be loaded causes startup to fail.

For example, configure and run a Markdown scan with a selected model and exclusions:

```bash
java -jar gw.jar "glob:**/*.md" \
  --projectDir . \
  --model "OpenAI:gpt-5.1" \
  --threads 4 \
  --excludes "target,.git" \
  --instructions "Keep headings consistent and preserve public links."
```

Use `--help` to display the available command-line syntax and examples:

```bash
java -jar gw.jar --help
```

Act mode can be started with `--act` or `-a`; `--acts` or `-as` changes where predefined Act files are loaded from.

## Resources

- [Machai official platform](https://machai.machanism.org/)
- [Machai Ghostwriter documentation](https://machai.machanism.org/ghostwriter/index.html)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [Ghostwriter on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)
- [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html)
- [Ghostwriter CLI download](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)
- [bindex-core](https://machai.machanism.org/bindex-core/index.html)
