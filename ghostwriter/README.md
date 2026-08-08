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

Machai Ghostwriter is a Java command-line processor and documentation engine for project-wide, AI-assisted file processing. It scans source code, tests, documentation, site content, configuration, and other project artifacts; applies embedded guidance; and uses a configured generative-AI provider to make focused, repeatable updates.

## Introduction

Ghostwriter is based on [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html). Guidance tags place explicit, reviewable instructions next to the content they govern, allowing teams to automate documentation maintenance and repository-wide transformations without separating instructions from the files they describe.

The application supports both Guidance mode and Act mode. Guidance mode discovers directives embedded in project files and applies format-aware processing. Act mode runs reusable, episode-driven workflows with shared project context and extensible tools. Together, these capabilities support repeatable local workflows as well as scripted CI/CD automation.

## Overview

Ghostwriter resolves project configuration, walks selected paths, filters excluded content, and delegates supported artifacts to specialized reviewers and the configured GenAI provider. It can process documentation, diagrams, configuration, and source files under one project-wide scanning model. The project structure separates command-line handling, project layout and configuration, scanning, AI processing, workflow orchestration, format-specific review, and provider tools so that processing remains extensible and testable.

![Ghostwriter C4 component diagram](src/site/resources/images/c4-diagram.png)

## Key Features

- Project-wide scanning using paths, glob patterns, or regular expressions.
- Embedded `@guidance` directives for precise, versionable processing requirements.
- Reusable Acts with episodes, nested workflows, and shared project context.
- Format-aware processing for Java, Markdown, PlantUML, HTML, Python, TypeScript, and text.
- Configurable GenAI provider, model, instructions, exclusions, project directory, and concurrency.
- Extensible tools for files, patches, commands, web resources, REST APIs, and Act control.
- CLI packaging suitable for local use, scripts, and CI/CD pipelines.

## Usage

### Prerequisites

- Java 8 or newer. The Maven project targets Java release 8.
- A supported GenAI provider, model, credentials, and any required endpoint configuration.
- A readable and writable project directory.
- Maven 3.x and network access to dependency repositories when building from source.

### Installation

Download and unpack the Ghostwriter CLI application pack:

[![Download](https://custom-icon-badges.demolab.com/badge/-Download-blue?style=for-the-badge&logo=download&logoColor=white "Download")](https://sourceforge.net/projects/machanism/files/machai/ghostwriter/gw.zip/download)

### Basic usage

Run the application with a project path or pattern:

```bash
java -jar gw.jar "glob:**/*.java"
```

A Markdown-focused example with custom configuration is:

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

### Typical workflow

1. Download and unpack the CLI pack, then provide the model provider credentials.
2. Set the project directory and persistent properties, or provide options on the command line.
3. Add precise `@guidance` directives beside the files they govern, or select a reusable Act.
4. Start with a narrow scan, review the generated changes and logs, and then expand the scope.
5. Run the validated command in CI/CD and publish the updated documentation or other artifacts.

With no positional path, Ghostwriter uses the configured path or the current directory. Paths may be relative directories, `glob:` patterns, or `regex:` patterns. The selected provider, valid credentials, network access where required, write permission, and sufficient provider quota are additional functional requirements.

## Resources

- [Machai Ghostwriter documentation](https://machai.machanism.org/ghostwriter/index.html)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [Ghostwriter on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/ghostwriter)
- [Machai platform](https://machai.machanism.org/)
- [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html)
