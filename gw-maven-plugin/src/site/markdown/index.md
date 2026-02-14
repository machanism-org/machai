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

GW Maven Plugin integrates Ghostwriter guided file processing into Maven builds so documentation and other project artifacts can be generated and kept up to date as part of normal development and CI workflows.

It scans project files (source code, documentation, Maven Site pages, and other content) for embedded `@guidance:` instructions and runs Ghostwriter processing across a single project or an entire multi-module build.

The `gw:gw` goal (implemented by `org.machanism.machai.maven.GW`) is an aggregator goal that can run even when a `pom.xml` is not present (`requiresProject=false`). When working with multi-module builds, it processes modules in reverse order (sub-modules first, then parent), mirroring the Ghostwriter CLI behavior.

For multi-module builds that should follow Maven’s standard reactor ordering, the `gw:reactor` goal (implemented by `org.machanism.machai.maven.ReactorGW`) processes modules according to reactor dependency order, with an option to delay processing of the execution-root project until all other reactor modules complete.

## Overview

The core value proposition is to make guided documentation automation a first-class Maven activity:

- **Keep docs current** by running guided processing during local development and CI.
- **Standardize behavior across modules** by centralizing configuration in Maven properties and plugin parameters.
- **Fit different build strategies** with both CLI-like reverse ordering (`gw:gw`) and reactor dependency ordering (`gw:reactor`).

## Key Features

- Scans **all relevant project files** (code, docs, site pages, and other content) for embedded `@guidance:` instructions.
- Goal **`gw:gw`** can run **without a `pom.xml`** and processes modules **sub-modules first** (reverse order).
- Goal **`gw:reactor`** processes modules using **standard Maven reactor dependency order**.
- Optional **root-project-last** behavior for reactor builds to process the execution root after other modules.
- Shared configuration via Maven properties and plugin parameters.
- Optional credential loading from `~/.m2/settings.xml` via a configured `<server>` id.

## Getting Started

### Prerequisites

- Java 11+.
- Apache Maven.
- A configured GenAI provider/model identifier and any required provider credentials (for example via `-Dgw.genai=...`).
- (Optional) Maven `settings.xml` credentials if you use `-Dgw.genai.serverId=...`.

### Basic Usage

Run guided file processing for the current directory:

```text
mvn gw:gw
```

Run reactor/module processing:

```text
mvn gw:reactor
```

### Typical Workflow

1. Add `@guidance:` comments to relevant project files (code, docs, site pages, etc.).
2. Configure defaults (optional):
   - Default guidance: `-Dgw.guidance=...`
   - Instruction locations: `-Dgw.instructions=...`
   - Excludes: `-Dgw.excludes=...`
3. Configure credentials (optional):
   - Add a `<server>` entry in `~/.m2/settings.xml`.
   - Run with `-Dgw.genai.serverId=<serverId>`.
4. Execute a goal:
   - `gw:gw` for reverse-order (sub-modules first) processing.
   - `gw:reactor` for Maven reactor-ordered processing.
5. Review and commit the updated/generated artifacts.

## Configuration

Common parameters are inherited by both goals from the shared base goal (`AbstractGWGoal`).

| Parameter / Property | Description | Default |
|---|---|---|
| `gw.genai` | GenAI provider/model identifier passed to Ghostwriter processing. | (none) |
| `gw.scanDir` | Scan root override; when omitted, scans the module base directory. | `${basedir}` |
| `gw.instructions` | Instruction locations (file paths or classpath locations) consumed by the workflow. | (none) |
| `gw.guidance` | Default guidance text forwarded to the workflow. | (none) |
| `gw.excludes` | Exclude patterns/paths to skip while scanning. | (none) |
| `gw.genai.serverId` | `settings.xml` `<server>` id used to load `GENAI_USERNAME`/`GENAI_PASSWORD`. | (none) |
| `gw.logInputs` | If `true`, logs the list of input files passed to the workflow. | `false` |
| `gw.threads` | (Goal: `gw:gw`) Enables/disables multi-threaded module processing. | `false` |
| `gw.rootProjectLast` | (Goal: `gw:reactor`) If `true`, delays execution-root processing until all other reactor projects complete. | `false` |

## Resources

- Official documentation: https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai.git
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
