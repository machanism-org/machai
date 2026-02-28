---
canonical: https://machai.machanism.org/gw-maven-plugin/index.html
---

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

## Introduction

GW Maven Plugin is the primary adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html).
It integrates Ghostwriter’s guided file processing into Maven builds so you can generate and maintain project documentation (and other guided updates) as part of a consistent, repeatable workflow.

At its core, the plugin scans your project for files containing embedded `@guidance:` blocks and then delegates the transformation/synthesis work to the Machai Ghostwriter engine.
This is based on the concept of [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): guidance is authored “in place” (close to the content it affects), and the processor uses that guidance to produce coherent, up-to-date results across the project.

The implementation in `src/main/java/org/machanism/machai/gw/maven` provides Maven goals (Mojos) that:

- Configure and run a `GuidanceProcessor` against a scan root (usually the Maven execution root or `src/site`).
- Support common workflow inputs such as instruction sources, default guidance, and exclusion patterns.
- Optionally source GenAI credentials from `~/.m2/settings.xml` via a Maven `<server>` entry.
- Offer multiple execution modes: standard module reverse-order processing (`gw:gw`), Maven-reactor ordering (`gw:reactor`), and interactive action bundles (`gw:act`).

## Overview

The GW Maven Plugin turns Ghostwriter into a first-class Maven capability.
Instead of running an external CLI step, you can invoke Ghostwriter processing via Maven goals, integrate it with your build lifecycle and CI pipelines, and apply consistent documentation automation across single-module and multi-module projects.

The main value proposition is:

- **Repeatable automation**: run Ghostwriter processing the same way locally and in CI.
- **Project-aware scanning**: use Maven context (basedir, session, reactor projects) to process modules reliably.
- **Guidance-driven updates**: keep documentation and other project content aligned with the source via embedded `@guidance:` blocks.

## Key Features

- **Guidance scanning and processing** of documentation and project files using Ghostwriter’s guided file processing model.
- **Multiple execution strategies**:
  - `gw:gw` (aggregator) for reverse-order module processing (sub-modules first, then parent modules), similar to the Ghostwriter CLI.
  - `gw:reactor` for Maven-reactor ordering, with an option to defer execution-root processing.
  - `gw:act` for interactive, predefined “actions” backed by resource bundles.
- **Flexible scan root** via `gw.scanDir` to target a specific directory tree.
- **Credential integration** by reading GenAI credentials from Maven `settings.xml` (`gw.genai.serverId`).
- **Configurable inputs**: instruction locations (`gw.instructions`), default guidance (`gw.guidance`), excludes (`gw.excludes`), and input logging (`gw.logInputs`).

## Getting Started

### Prerequisites

- **Maven**: a Maven version compatible with your build.
- **A GenAI provider configuration** supported by Machai Ghostwriter.
  - Optionally supply credentials through Maven `~/.m2/settings.xml` using a `<server>` entry and `-Dgw.genai.serverId=...`.
- **Project files containing `@guidance:` blocks** (source code, documentation, site content, etc.), depending on what you want Ghostwriter to process.

### Java Version

- **Build / compilation target**: the project’s `pom.xml` specifies `maven.compiler.release=8`.
- **Functional runtime requirements**: the plugin and its dependencies may still run on newer Java versions, but you should treat **Java 8** as the baseline requirement unless your selected GenAI provider/dependency set requires a higher runtime.

### Basic Usage

Run guided processing for the current project:

```bash
mvn gw:gw
```

Run guided processing for a specific scan root:

```bash
mvn gw:gw -Dgw.scanDir=src\site
```

Run the reactor-ordered goal:

```bash
mvn gw:reactor
```

Run interactive actions:

```bash
mvn gw:act
```

### Typical Workflow

1. Add `@guidance:` blocks to the files you want Ghostwriter to manage (documentation pages, READMEs, site content, source files, etc.).
2. Provide instructions and/or default guidance:
   - `-Dgw.instructions=...` to point at instruction sources.
   - `-Dgw.guidance="..."` for a default prompt when a file needs guidance.
3. Configure your GenAI provider/model via `-Dgw.genai=...`.
4. (Optional) Configure excludes with `-Dgw.excludes=...`.
5. Run `mvn gw:gw` (or `mvn gw:reactor`) and review the updated outputs.
6. Commit the results.

## Configuration

Common parameters used by the goals in this plugin:

| Parameter | Description | Default |
|---|---|---|
| `gw.genai` | GenAI provider/model identifier passed to the workflow. | _(none)_ |
| `gw.scanDir` | Scan root override. If omitted, the plugin defaults to the Maven execution root directory. | Maven execution root |
| `gw.instructions` | Instruction locations consumed by the workflow (e.g., file paths or classpath locations). | _(none)_ |
| `gw.guidance` | Default guidance text forwarded to the workflow. | _(none)_ |
| `gw.excludes` | Exclude patterns/paths to skip while scanning documentation sources. | _(none)_ |
| `gw.genai.serverId` | Maven `settings.xml` `<server>` id used to read GenAI credentials (username/password). | _(none)_ |
| `gw.logInputs` | Whether to log the list of input files passed to the workflow. | `false` |
| `gw.threads` | (`gw:gw` only) Enables/disables multi-threaded module processing. | `false` |
| `gw.rootProjectLast` | (`gw:reactor` only) If `true`, delays processing of the execution-root project until other reactor projects complete. | `true` |

## Resources

- Ghostwriter: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- GitHub (SCM): https://github.com/machanism-org/machai

<!-- @guidance:
**VERY IMPORTANT NOTE:** Ghostwriter works with **all types of project files—including source code, documentation, project site content, and other relevant files**.
Ensure that your content generation and documentation efforts consider the full range of file types present in the project.
**GW Maven Plugin is the primary adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html).**  
It serves as the main integration point, enabling Ghostwriter’s features and automation within Maven-based projects.
# Page Structure
1. Header
   - **Project Title:**  
     - Automatically extract the project title from `pom.xml`.
   - **Maven Central Badge:**  
     - Display the Maven Central badge using the following Markdown:  
       `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`
     - Replace `[groupId]` and `[artifactId]` with values from `pom.xml`.
2. Introduction
   - Provide a comprehensive description of the GW Maven plugin, including its purpose and benefits.
   - Analyze java files in the `/src/main/java/org/machanism/machai/gw/maven` to inform the description.
   - Reference [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) as the conceptual foundation for Machai Ghostwriter.
3. Overview
   - Clearly explain the main functions and value proposition of the GW Maven plugin.
   - Summarize how the plugin enhances project workflows and documentation.
4. Key Features
   - Present a bulleted list of the primary capabilities and unique features of the plugin.
5. Getting Started
   - **Prerequisites:**  
     - List all required software, services, and environment settings needed to use the plugin.
   - **Java Version:**  
     Note that the required Java version is defined in `pom.xml`, but actual functional requirements may differ. Clearly state both.
   - **Basic Usage:**  
     - Provide an example command for running the plugin.
   - **Typical Workflow:**  
     - Outline the step-by-step process for using the plugin and its artifacts.
6. Configuration
   - Include a table of common configuration parameters, with columns for parameter name, description, and default value.
   - Ensure descriptions are clear and concise.
7. Resources
   - Provide a list of relevant links, including:
     - Official platform or documentation site
     - GitHub repository
     - Maven Central page
     - Any other useful resources
# General Instructions     
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, tables, code blocks, and links.
- Ensure clarity, conciseness, and easy navigation throughout the page.
-->
