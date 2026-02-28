---
canonical: https://machai.machanism.org/gw-maven-plugin/index.html
---

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
   - Analyze `/src/main/java/org/machanism/machai/gw/maven/StandardProcess.java` and `/src/main/java/org/machanism/machai/gw/maven/GW.java` to inform the description.
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

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

## Introduction

GW Maven Plugin is the primary Maven adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html), enabling Machai Ghostwriter’s *Guided File Processing* inside standard Maven builds and CI pipelines.

It runs Ghostwriter’s guided processing workflow over **any guided artifacts in your repository**—including source code, documentation, Maven site content, and other project files—by scanning for embedded `@guidance` directives and applying them to generate or update outputs in a repeatable, version-controlled way.

The main goal, `gw:gw` (implemented by `org.machanism.machai.gw.maven.GW`), configures a `GuidanceProcessor` with Maven-aware defaults, optionally loads GenAI credentials from Maven `settings.xml`, and then scans and processes guided inputs from the selected scan root.

As described in [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html), keeping guidance *next to the artifacts it governs* makes the automation reviewable and reliable: guidance evolves with the codebase, and running the plugin repeatedly produces consistent, auditable results.

> Note: the guidance comment references `StandardProcess.java`; this module’s shared scan/execute logic is implemented in `AbstractGWGoal` and used by the concrete goals.

## Overview

The GW Maven Plugin provides Maven goals that execute Ghostwriter as part of your normal developer workflow. Instead of invoking a separate CLI and manually coordinating inputs, you run a Maven goal that:

1. Resolves scan root and module context from Maven.
2. Scans the repository for guided inputs.
3. Runs Ghostwriter processing using your configured provider/model and credentials.
4. Writes updated artifacts back to disk so they can be reviewed and committed.

This helps teams keep documentation and other guided outputs continuously aligned with the codebase using the same commands they already use for builds and CI.

## Key Features

- **Maven goal `gw:gw`** to run Ghostwriter Guided File Processing.
- **Can run without a `pom.xml`** (`requiresProject=false`) for ad-hoc processing in any directory.
- **Reverse module processing** for `gw:gw` (sub-modules first, then parent), aligned with Guided File Processing ordering.
- **Reactor-aware goal `gw:reactor`** for reactor dependency ordering (multi-module builds).
- **Optional multi-threaded module processing** for `gw:gw` via `-Dgw.threads=true`.
- **Exclude patterns** to skip files/paths during scanning (`gw.excludes`).
- **Additional instructions and default guidance injection** via `gw.instructions` and `gw.guidance`.
- **Credential loading from `settings.xml`** via `gw.genai.serverId` (forwarded as `GENAI_USERNAME` / `GENAI_PASSWORD`).
- **Optional input logging** (`-Dgw.logInputs=true`) to help with transparency and debugging.

## Getting Started

### Prerequisites

- **Java** installed.
- **Apache Maven** installed.
- **Ghostwriter GenAI provider configuration**, as required by your Ghostwriter setup:
  - Provider/model selection (for example via `-Dgw.genai=...`).
  - Credentials available to Ghostwriter (commonly via Maven `settings.xml` when using `-Dgw.genai.serverId=...`).
- **Network access** to your GenAI provider endpoint (as required by the provider you configure).

### Java Version

- **Declared build target:** Java **8** (`pom.xml` sets `maven.compiler.release=8`).
- **Practical runtime requirement:** depends on the Ghostwriter runtime and the provider/client libraries you use. While the plugin targets Java 8, some GenAI SDKs and runtime/TLS requirements may require a newer Java version.

### Basic Usage

Run Ghostwriter processing for the current directory:

```cmd
mvn gw:gw
```

Run reactor-ordered processing in a multi-module build:

```cmd
mvn gw:reactor
```

Enable multi-threaded module processing (for `gw:gw`):

```cmd
mvn gw:gw -Dgw.threads=true
```

### Typical Workflow

1. Add or update `@guidance` directives in project files (source, docs, site content, etc.).
2. Optionally provide additional instructions and defaults:
   - `-Dgw.instructions=...`
   - `-Dgw.guidance=...`
3. Configure your GenAI provider/model and credentials as needed.
4. Run a goal:
   - `mvn gw:gw` for reverse module order.
   - `mvn gw:reactor` for reactor dependency ordering.
5. Review diffs, iterate on guidance if needed, and commit updated artifacts.

## Configuration

Common parameters (set via `-D...` system properties and/or plugin configuration where applicable):

| Parameter | Description | Default |
|---|---|---|
| `gw.genai` | Provider/model identifier forwarded to Ghostwriter (for example, `openai:gpt-4o-mini`). | (none) |
| `gw.genai.serverId` | `settings.xml` `<server>` id used to read credentials and forward them as `GENAI_USERNAME` / `GENAI_PASSWORD`. | (none) |
| `gw.scanDir` | Scan root directory. When omitted, uses Maven’s execution root directory. | execution root directory |
| `gw.instructions` | Additional instruction locations consumed by the workflow. | (none) |
| `gw.guidance` | Default guidance text forwarded to the workflow. | (none) |
| `gw.excludes` | Exclude patterns/paths to skip while scanning. | (none) |
| `gw.logInputs` | Log the list of input files passed to the workflow. | `false` |
| `gw.threads` | Enable multi-threaded module processing for `gw:gw`. | `false` |
| `gw.rootProjectLast` | For `gw:reactor`, delay processing the execution-root project until other reactor projects complete. | `false` |

## Resources

- Ghostwriter (official documentation): https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing (concept and ordering): https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
