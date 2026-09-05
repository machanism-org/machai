<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src\\site\\markdown\\index.md` content summary.
    - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/[artifactId].svg)](https://central.sonatype.com/artifact/org.machanism.machai/[artifactId])` and 
     [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/[artifactId]/bindex.json) in one line after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
3. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
   - Add the Ghostwriter CLI application jar download link: [![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download) to the installation section.
4. **Resources**
   - [Ghostwriter MCP Server](https://github.com/machanism-org/gw-mcp-server)
   - **Contact Information:** Include contact details for support or inquiries.
   - **Support Links:** Provide links to:
     - The project’s issue tracker.
     - Documentation or FAQs.
     - Any relevant community forums or chat channels.
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
- If used resources by uri: `src/site/resources/images`, need to use project site location: `https://machai.machanism.org/ghostwriter/images`.
-->

# Ghostwriter

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/ghostwriter.svg)](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/ghostwriter/bindex.json)

Ghostwriter is an AI-powered command-line agent for guided, project-wide work across source code, documentation, website content, configuration, diagrams, tests, and other project files.

## Introduction

Ghostwriter scans selected project paths and delegates supported artifacts to format-aware processors. Embedded `@guidance` directives keep instructions beside the content they govern, enabling focused, repeatable updates through a configured generative-AI provider. Its reusable Act workflows and host tools extend that approach to multi-step repository automation, documentation maintenance, and CI/CD-friendly processing.

The project is founded on [Guidance-Driven Processing (GDP)](https://www.machanism.org/guided-file-processing/index.html), which makes file-level instructions explicit and reviewable, and [Act-Driven Workflows (ADW)](https://www.machanism.org/act/index.html), which supports reusable episode-driven workflows.

## Overview

Ghostwriter resolves project and runtime configuration, scans paths while honoring exclusions, and processes each supported file through the appropriate reviewer. In Guidance mode it discovers directives embedded in files; in Act mode it runs a named workflow with shared project context and tools for local files, commands, web content, REST APIs, and nested Acts.

The architecture separates the command-line entry point, project configuration and layout, scanning and AI processing, guidance and workflow orchestration, format-specific reviewers, and provider tools. This lets local project operations and remote model or instruction access remain extensible and testable.

![Ghostwriter component diagram](https://machai.machanism.org/ghostwriter/images/c4-diagram.png)

## Key Features

- Project-wide scanning using directories, file names, `glob:` patterns, and `regex:` patterns.
- Guidance mode for embedded `@guidance` directives and Act mode for reusable workflows.
- Format-aware processing for Java, Markdown, PlantUML, HTML, Python, TypeScript, and text.
- Configurable model, instructions, exclusions, project directory, concurrency, and Act source.
- Extensible host tools for file operations, commands, web access, REST calls, and workflow control.
- Maven-packaged CLI suitable for local runs and scripted CI/CD automation.

## Installation

### Prerequisites

- Java 8 or newer. The project is compiled for Java 8.
- A supported Machai GenAI provider and model, with its credentials and endpoint settings available.
- Read/write access to the target project and network access where the selected provider or remote Act resources require it.
- Maven 3.x when building from source; the delivery-pack build also requires `MACHANISM_PACK_DIR`.

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)

Download and unpack the Ghostwriter CLI delivery pack, or build the project with Maven. The related [Bindex Core](https://machai.machanism.org/bindex-core/index.html) component provides project and library indexing support.

## Usage

Run the executable JAR with a scan path:

```bash
java -jar gw.jar "glob:**/*.java"
```

A path can be a relative directory or file name, a `glob:` pattern, or a `regex:` pattern. With no positional path, Ghostwriter uses the configured path or `.`. Absolute paths must be inside the project directory.

Typical use:

1. Provide provider credentials and choose a model.
2. Configure the project directory, exclusions, instructions, or an Act as needed.
3. Add precise `@guidance` directives beside the content to process, or choose a reusable Act.
4. Start with a narrow scan, review the changes and logs, then expand the scope or run it in CI/CD.

### Common options

| Option | Purpose |
|---|---|
| `-h`, `--help` | Show help and exit. |
| `-d <dir>`, `--projectDir <dir>` | Set the project directory. |
| `-c <file>`, `--config <file>` | Select a properties file. |
| `-t <n>`, `--threads <n>` | Set processing concurrency. |
| `-m <provider:model>`, `--model <provider:model>` | Select the GenAI provider and model. |
| `-i [text]`, `--instructions [text]` | Set instructions, or read them from standard input when no value is supplied. |
| `-e <list>`, `--excludes <list>` | Set comma-separated exclusions. |
| `-as <dir>`, `--acts <dir>` | Set the local or HTTP(S) Act source. |
| `-a [name]`, `--act [name]` | Enable Act mode and select an Act. |

For example:

```bash
java -jar gw.jar "glob:**/*.md" \
  --projectDir . \
  --model "OpenAI:gpt-5.1" \
  --threads 4 \
  --excludes "target,.git" \
  --instructions "Keep headings consistent and preserve public links."
```

Run `java -jar gw.jar --help` for the complete option syntax and examples.

## Resources and Support

- [Machanism platform](https://www.machanism.org/)
- [Ghostwriter documentation](https://machai.machanism.org/ghostwriter/index.html) and [guidance-tag documentation](https://machai.machanism.org/ghostwriter/guidance-tag.html)
- [Ghostwriter on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)
- [Source repository](https://github.com/machanism-org/machai) and [issue tracker](https://github.com/machanism-org/machai/issues)
- [Ghostwriter MCP Server](https://github.com/machanism-org/gw-mcp-server)
- [Machanism GitHub organization](https://github.com/machanism-org) for community projects and discussions

For support or inquiries, open an issue in the [project issue tracker](https://github.com/machanism-org/machai/issues). Consult the [Ghostwriter documentation](https://machai.machanism.org/ghostwriter/index.html) and [FAQ/search resources](https://github.com/machanism-org/machai/issues?q=is%3Aissue) before reporting a problem.
