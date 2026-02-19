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

GW Maven Plugin brings MachAI Ghostwriter guided file processing into Maven so you can generate and maintain documentation (and other guided artifacts) as a normal part of local development and CI.

When you run the plugin, it configures a Ghostwriter `FileProcessor`, determines the active project layout (including Maven layout when applicable), then scans a configurable root directory for files containing embedded `@guidance:` instructions. Those instructions drive Ghostwriter’s processing so the repository can continuously synthesize and update content in a consistent, repeatable way.

The primary goal, `gw:gw` (implemented by `org.machanism.machai.gw.maven.GW`), is an **aggregator** goal and can run even when a `pom.xml` is not present (`requiresProject=false`). When executed inside a Maven session, it can enrich a detected Maven project layout with the effective Maven model from the reactor, allowing guided processing to incorporate project metadata.

## Overview

The GW Maven Plugin makes guided documentation automation a first-class Maven activity:

- **Keep artifacts current** by running guided processing during local development and CI.
- **Standardize behavior** by centralizing configuration in Maven properties and plugin parameters.
- **Support different build strategies** with both single-goal aggregation (`gw:gw`) and reactor-ordered processing (`gw:reactor`).

Under the hood, the plugin configures Ghostwriter processing (instructions, default guidance, excludes, provider configuration, and optional credential mapping), scans from a configurable root directory, and passes the resolved inputs to Ghostwriter for guided updates.

## Key Features

- Scans **all relevant project files** (source code, documentation, Maven Site content, and more) for embedded `@guidance:` instructions.
- Goal **`gw:gw`** is an **aggregator** goal and can run **without a `pom.xml`** (`requiresProject=false`).
- Supports **multi-module** builds, including reverse-order processing similar to the Ghostwriter CLI (sub-modules first, parent modules last).
- Optional **multi-threaded module processing** for `gw:gw` via `-Dgw.threads=true`.
- Optional **reactor-ordered processing** via `gw:reactor`, with an option to process the execution-root project last.
- Shared configuration via Maven properties and plugin parameters.
- Optional credential loading from `~/.m2/settings.xml` via a configured `<server>` id (`GENAI_USERNAME` / `GENAI_PASSWORD`).
- Optional logging of the resolved input file list (`-Dgw.logInputs=true`).

## Getting Started

### Prerequisites

- Java 8+.
- Apache Maven 3.x.
- Network access to your configured GenAI provider (as required by your workflow).
- A configured GenAI provider/model identifier (for example, `-Dgw.genai=...`) and any required provider credentials.
- (Optional) Maven `~/.m2/settings.xml` credentials if you use `-Dgw.genai.serverId=...`.

### Basic Usage

Run guided file processing for the current project:

```text
mvn gw:gw
```

### Typical Workflow

1. Add `@guidance:` comments to relevant project files (code, docs, site pages, etc.).
2. Configure inputs (as needed):
   - Instruction locations: `-Dgw.instructions=...`
   - Default guidance: `-Dgw.guidance=...`
   - Excludes: `-Dgw.excludes=...`
   - Scan root override: `-Dgw.scanDir=...`
3. Configure GenAI access (as needed):
   - Provide provider credentials using your preferred mechanism, or
   - Add a `<server>` entry in `~/.m2/settings.xml` and run with `-Dgw.genai.serverId=<serverId>`.
4. Execute the plugin: `mvn gw:gw` (or `mvn gw:reactor` for reactor-ordered processing).
5. Review and commit the updated/generated artifacts.

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `gw.genai` | GenAI provider/model identifier forwarded to Ghostwriter (for example, `openai:gpt-4o-mini`). | _(none)_ |
| `gw.scanDir` | Directory to scan for guided files. If omitted, uses Maven execution root directory. | execution root directory |
| `gw.instructions` | Instruction locations consumed by Ghostwriter. | _(none)_ |
| `gw.guidance` | Default guidance text forwarded to Ghostwriter. | _(none)_ |
| `gw.excludes` | Exclude patterns/paths to skip while scanning. | _(none)_ |
| `gw.genai.serverId` | `settings.xml` `<server>` id used to load `GENAI_USERNAME` / `GENAI_PASSWORD`. | _(none)_ |
| `gw.logInputs` | Log the resolved input file list passed to Ghostwriter. | `false` |
| `gw.threads` | Enable multi-threaded module processing for `gw:gw`. | `false` |

## Resources

- Official guided file processing documentation: https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai.git
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
