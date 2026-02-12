<!-- @guidance:
**VERY IMPORTANT NOTE:**  
Ghostwriter works with **all types of project files—including source code, documentation, project site content, and other relevant files**.
Ensure that your content generation and documentation efforts consider the full range of file types present in the project.
# Header
   - **Project Title:**  
     - Automatically extract the project title from `pom.xml`.
   - **Maven Central Badge:**  
     - Display the Maven Central badge using the following Markdown:  
       `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`
     - Replace `[groupId]` and `[artifactId]` with values from `pom.xml`.
# Introduction
   - Provide a comprehensive description of the GW Maven plugin, including its purpose and benefits.
   - Analyze `/src/main/java/org/machanism/machai/maven/StandardProcess.java` and `/src/main/java/org/machanism/machai/maven/GW.java` to inform the description.
# Overview
   - Clearly explain the main functions and value proposition of the GW Maven plugin.
   - Summarize how the plugin enhances project workflows and documentation.
# Key Features
   - Present a bulleted list of the primary capabilities and unique features of the plugin.
# Getting Started
   - **Prerequisites:**  
     - List all required software, services, and environment settings needed to use the plugin.
   - **Basic Usage:**  
     - Provide an example command for running the plugin.
   - **Typical Workflow:**  
     - Outline the step-by-step process for using the plugin and its artifacts.
# Configuration
   - Include a table of common configuration parameters, with columns for parameter name, description, and default value.
   - Ensure descriptions are clear and concise.
# Resources
   - Provide a list of relevant links, including:
     - Official platform or documentation site
     - GitHub repository
     - Maven Central page
     - Any other useful resources
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, tables, code blocks, and links.
- Ensure clarity, conciseness, and easy navigation throughout the page.
- Organize content logically for optimal readability and user experience.
-->

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

## Introduction

GW Maven Plugin (Ghostwriter Maven Plugin) is a Maven plugin that integrates Ghostwriter guided file processing into your build so documentation and other repository content stays accurate as your code evolves.

It scans a configurable directory (often `src\site`) for project files (Markdown, source, and other artifacts) that include embedded `@guidance` blocks and other instructions. Those inputs are then passed to the configured GenAI provider/model, which synthesizes updates and writes resulting changes back to the working tree.

The plugin provides two goals:

- `gw:std` — standard, per-module execution.
- `gw:gw` — aggregator/reactor execution across all modules.

Based on the implementation:

- `StandardProcess` (`gw:std`) builds configuration and processes the current module; when invoked at the reactor root it can optionally delay execution-root processing until all other reactor projects complete (`gw.rootProjectLast`).
- `GW` (`gw:gw`) runs in aggregator mode, enriches each module’s Maven model from the reactor, supports multi-threaded module processing (`gw.threads`), and processes modules in reverse order (sub-modules first), matching Ghostwriter CLI behavior.

## Overview

The GW Maven Plugin enhances documentation and repository maintenance workflows by integrating guidance-driven automation into Maven:

- **Guidance-driven generation:** Embedded `@guidance` blocks act as localized, file-scoped requirements for what should be generated or updated.
- **Maven-aware context:** Processing runs with a Maven project layout so the workflow can consider module boundaries and POM metadata alongside docs and sources.
- **Flexible execution modes:** Run on a single module with `gw:std`, or across a full multi-module reactor with `gw:gw`.
- **CI-friendly:** As a standard Maven plugin, it can run in CI pipelines to keep docs synchronized with code changes.

## Key Features

- Two goals: standard (`gw:std`) and aggregator/reactor (`gw:gw`) modes
- Scans a configurable root (often `src\site`) and respects exclude patterns
- Consumes embedded `@guidance` from Markdown and other project files
- Supports default guidance text and external instruction inputs
- Optional credential loading from Maven `settings.xml` via `gw.genai.serverId` (mapped to `GENAI_USERNAME` / `GENAI_PASSWORD`)
- Optional logging of the exact input file set passed to the workflow (`gw.logInputs`)
- Aggregator mode supports multi-threaded module processing (`gw.threads`) and reverse module order processing

## Getting Started

### Prerequisites

- Java 11+ (this module compiles with `maven.compiler.release=11`)
- Apache Maven 3.x
- Access to a supported GenAI provider/model (configured via `gw.genai`) and any required network/proxy settings
- (Optional) Maven `settings.xml` credentials if using `gw.genai.serverId`

### Basic Usage

Run the standard goal:

```cmd
mvn gw:std
```

Run the aggregator goal:

```cmd
mvn gw:gw
```

Specify a provider/model and load credentials from a `settings.xml` `<server>`:

```cmd
mvn gw:gw -Dgw.genai=openai:gpt-4.1-mini -Dgw.genai.serverId=genai
```

### Typical Workflow

1. Add or update project files (documentation under `src\site\markdown`, source code, etc.).
2. Add or refine embedded `@guidance` blocks in the files you want Ghostwriter to maintain.
3. Configure the plugin in `pom.xml` and/or pass system properties (for example `-Dgw.genai=...`).
4. (Optional) Add GenAI credentials to Maven `settings.xml` and reference them via `-Dgw.genai.serverId=...`.
5. Run `mvn gw:std` for a single module or `mvn gw:gw` for the full reactor.
6. Review generated changes and commit them.

## Configuration

Common configuration parameters (via plugin `<configuration>` and/or system properties):

| Parameter | Description | Default |
|---|---|---|
| `gw.genai` | Provider/model identifier forwarded to the workflow (for example `openai:gpt-4.1-mini`). | *(none)* |
| `rootDir` | Maven module base directory. | `${basedir}` |
| `gw.scanDir` | Scan root override (path to scan for documentation sources). | `${basedir}` |
| `gw.instructions` | Instruction location(s) consumed by the workflow (for example a file path or URI). | *(none)* |
| `gw.guidance` | Default guidance text forwarded to the workflow. | *(none)* |
| `gw.excludes` | Exclude patterns/paths skipped during scanning. | *(none)* |
| `gw.genai.serverId` | `settings.xml` `<server>` id used to load GenAI credentials (username/password). | *(none)* |
| `gw.logInputs` | Logs the list of input files passed to the workflow. | `false` |
| `gw.rootProjectLast` | Delays execution-root processing until other reactor projects complete (`gw:std` only). | `false` |
| `gw.threads` | Enables/disables multi-threaded module processing (`gw:gw` only). | `true` |

Example `pom.xml` configuration:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>...</version>
  <configuration>
    <genai>openai:gpt-4.1-mini</genai>
    <scanDir>${project.basedir}\src\site</scanDir>
    <instructions>file:${project.basedir}\src\site\instructions.md</instructions>
    <guidance>Write concise release notes.</guidance>
    <logInputs>false</logInputs>
  </configuration>
</plugin>
```

## Resources

- Official documentation (guided file processing): https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- Maven Plugin Development (Apache Maven): https://maven.apache.org/guides/plugin/guide-java-plugin-development.html
- Maven Settings reference: https://maven.apache.org/settings.html
