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

GW Maven Plugin (also referenced in the POM description as the Ghostwriter Maven Plugin) integrates **Ghostwriter guided file processing** into Maven builds.
It scans project files (source, documentation, and site content) for embedded guidance and runs an AI-assisted synthesis workflow to generate and maintain consistent, up-to-date documentation.

The plugin provides goals that:

- process a single project directory (including cases where a `pom.xml` is not present), and
- process a multi-module build using the Maven reactor.

## Overview

The value proposition of the plugin is to make guided documentation generation part of normal Maven workflows:

- Run Ghostwriter processing during local development or CI to keep docs current.
- Use Maven properties / plugin parameters to control scan roots, guidance defaults, and credentials.
- Support both aggregator processing order (sub-modules before parent) and reactor processing (Maven dependency order).

## Key Features

- Maven goal **`gw:gw`** that can run even without a `pom.xml` (goal is marked `requiresProject=false`).
- Processes modules in **reverse order** (sub-modules first, then parent), similar to the Ghostwriter CLI.
- Maven goal **`gw:mod`** for **reactor-aware** multi-module processing using standard Maven reactor dependency ordering.
- Shared configuration via common parameters (inherited from the base goal) for scan directory, excludes, instructions, and default guidance.
- Optional credential loading from `~/.m2/settings.xml` via a configured `<server>` id.
- Optional logging of input files sent to the workflow.

## Getting Started

### Prerequisites

- Java 11+ (plugin compiles with `maven.compiler.release=11`).
- Apache Maven.
- A configured GenAI provider/model identifier (for example, via `-Dgw.genai=...`), plus any required provider credentials.
- (Optional) Maven `settings.xml` server credentials if you use `-Dgw.genai.serverId=...`.

### Basic Usage

Run guided file processing for the current directory:

```text
mvn gw:gw
```

Run reactor/module processing:

```text
mvn gw:mod
```

### Typical Workflow

1. Add `@guidance:` comments to relevant project files (code, docs, site pages, etc.).
2. Configure defaults (optional):
   - Provide default guidance (`-Dgw.guidance=...`).
   - Provide instruction locations (`-Dgw.instructions=...`).
   - Configure excludes (`-Dgw.excludes=...`).
3. Configure credentials (optional):
   - Add a `<server>` entry in `~/.m2/settings.xml`.
   - Run with `-Dgw.genai.serverId=<serverId>`.
4. Execute one of the goals:
   - `gw:gw` for reverse-order (submodules first) processing similar to the CLI.
   - `gw:mod` for Maven reactor-ordered processing.
5. Review and commit the updated/generated documentation.

## Configuration

Common parameters are inherited by both goals from the shared base goal.

| Parameter / Property | Description | Default |
|---|---|---|
| `gw.genai` | GenAI provider/model identifier passed to the workflow. | (none) |
| `gw.scanDir` | Scan root override; when omitted, scans the module base directory. | `${basedir}` |
| `gw.instructions` | Instruction locations (file paths or classpath locations) consumed by the workflow. | (none) |
| `gw.guidance` | Default guidance text forwarded to the workflow. | (none) |
| `gw.excludes` | Exclude patterns/paths to skip while scanning. | (none) |
| `gw.genai.serverId` | `settings.xml` `<server>` id used to load `GENAI_USERNAME`/`GENAI_PASSWORD`. | (none) |
| `gw.logInputs` | If `true`, logs the list of input files passed to the workflow. | `false` |
| `gw.threads` | (Goal: `gw:gw`) Enables/disables multi-threaded module processing. | `false` |
| `gw.rootProjectLast` | (Goal: `gw:mod`) If `true`, delays execution-root processing until all other reactor projects complete. | `false` |

## Resources

- Project repository (SCM): https://github.com/machanism-org/machai.git
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- Guided file processing overview: https://www.machanism.org/guided-file-processing/index.html
