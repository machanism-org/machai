<!-- @guidance:
**VERY IMPORTANT NOTE:**  
Ghostwriter works with **all types of project files—including source code, documentation, project site content, and other relevant files**.
Ensure that your content generation and documentation efforts consider the full range of file types present in the project.

**GW Maven Plugin is the primary adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html).**  
It serves as the main integration point, enabling Ghostwriter’s features and automation within Maven-based projects.

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
   - **Java Version:**  
     Note that the required Java version is defined in `pom.xml`, but actual functional requirements may differ. Clearly state both.
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

GW Maven Plugin is the Maven integration for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It brings Ghostwriter’s guided file processing into Maven builds so documentation and other project artifacts can be generated and kept current directly from your project sources.

The plugin executes Ghostwriter’s scanning and processing pipeline against the current project (or even a directory without a `pom.xml`) and supports reactor-aware behavior such as processing order and optional multi-threaded module processing.

## Overview

The plugin provides Maven goals that:

- Scan a project (including multi-module reactors) for guided content.
- Apply instructions and `@guidance` directives embedded in files to produce consistent outputs.
- Optionally source GenAI credentials from Maven `settings.xml` and pass them into the Ghostwriter workflow.

In practice, this allows teams to run documentation automation using standard Maven invocations (local developer machines and CI), keeping generated documentation aligned with the current codebase.

## Key Features

- Maven goal for running Ghostwriter guided file processing (`gw:gw`).
- Works even when a `pom.xml` is not present (`requiresProject=false`).
- Reactor-aware execution with reverse processing order (sub-modules first, then parent), matching Ghostwriter CLI behavior.
- Optional multi-threaded module processing (`-Dgw.threads=true`).
- Supports instruction inputs and default guidance injection.
- Supports exclude patterns to skip files/paths.
- Can load GenAI credentials from Maven `settings.xml` via `-Dgw.genai.serverId=...`.
- Optional logging of the resolved input file list (`-Dgw.logInputs=true`).

## Getting Started

### Prerequisites

- Java installed.
- Maven 3.x installed.
- Network access to your chosen GenAI provider (if using an online provider).
- Maven `settings.xml` configured with credentials (optional) when using `gw.genai.serverId`.

### Java Version

- **Build/runtime target in this project:** Java **8** (from `pom.xml`: `maven.compiler.release=8`).
- **Functional requirement:** depends on the Ghostwriter runtime and the selected provider tooling; Java 8 is the intended baseline for this plugin, but your environment may require a newer Java to match the provider/client libraries used by the overall Ghostwriter stack.

### Basic Usage

Run Ghostwriter processing for the current directory:

```cmd
mvn gw:gw
```

Enable multi-threaded module processing:

```cmd
mvn gw:gw -Dgw.threads=true
```

### Typical Workflow

1. Add or update `@guidance` directives in project files (source, docs, site content, etc.).
2. (Optional) Provide additional instructions via `-Dgw.instructions=...`.
3. Run the Maven goal (`mvn gw:gw`).
4. Review generated/updated outputs and commit the results.
5. In multi-module projects, rely on reactor processing (sub-modules first) to ensure parent artifacts aggregate the latest module changes.

## Configuration

Common parameters (set via `-D...` system properties or plugin configuration where applicable):

| Parameter | Description | Default |
|---|---|---|
| `gw.genai` | GenAI provider/model identifier forwarded to the workflow. | (none) |
| `gw.genai.serverId` | `settings.xml` `<server>` id used to read GenAI credentials and map them to `GENAI_USERNAME` / `GENAI_PASSWORD`. | (none) |
| `gw.scanDir` | Scan root directory. When omitted, uses Maven execution root directory. | execution root directory |
| `gw.instructions` | Instruction locations consumed by the workflow. | (none) |
| `gw.guidance` | Default guidance text forwarded to the workflow. | (none) |
| `gw.excludes` | Exclude patterns/paths to skip while scanning. | (none) |
| `gw.logInputs` | Log the list of input files passed to the workflow. | `false` |
| `gw.threads` | Enable multi-threaded module processing for `gw:gw`. | `false` |

## Resources

- Ghostwriter: https://machai.machanism.org/ghostwriter/index.html
- Guided file processing (processing order details): https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central (artifact page): https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
