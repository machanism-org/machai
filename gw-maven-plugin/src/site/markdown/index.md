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
   - Analyze `/src/main/java/org/machanism/machai/maven/StandardProcess.java` and `/src/main/java/org/machanism/machai/maven/GW.java` to inform the description.
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

GW Maven Plugin is the Maven integration for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html) and the primary adapter that brings Ghostwriter’s Guided File Processing into standard Maven builds.

It runs Ghostwriter’s processing pipeline inside a repeatable Maven workflow, scanning project files (source code, documentation, Maven site content, and other guided artifacts) for embedded `@guidance` directives and applying them to generate or update outputs.

Internally, the plugin centers around a standard processing flow (as implemented in `StandardProcess`) and goal entry points (such as `GW`) that:

- Determine an appropriate scan root from Maven’s execution context.
- Build a resolved list of input files, applying configured exclude patterns.
- Combine embedded `@guidance` with optional default guidance and extra instructions.
- Execute Ghostwriter against those inputs and write updated artifacts back to disk.

As described in [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html), this approach treats guidance as a first-class part of your repository, enabling reliable, repeatable automation for documentation and other source-adjacent outputs.

## Overview

The GW Maven Plugin provides Maven goals that integrate Ghostwriter into builds and CI pipelines:

- **`gw:gw`** runs Ghostwriter for a directory (including cases where no `pom.xml` is present), processing modules in reverse order (sub-modules first, then parent) to match Ghostwriter CLI behavior.
- **`gw:reactor`** runs Ghostwriter using Maven reactor dependency ordering to better align with multi-module build semantics.

In practice, this lets teams keep guided artifacts consistent with the evolving codebase by running standard Maven commands as part of everyday development.

## Key Features

- Goal `gw:gw` to run Ghostwriter Guided File Processing.
- Can be executed without a `pom.xml` (`requiresProject=false`).
- Reverse module processing for `gw:gw` (sub-modules first, then parent).
- Reactor-aware goal `gw:reactor` that uses Maven dependency ordering.
- Optional multi-threaded module processing for `gw:gw` (`-Dgw.threads=true`).
- Supports additional instruction inputs and optional default guidance injection.
- Supports exclude patterns to skip files/paths.
- Can load GenAI credentials from Maven `settings.xml` (`-Dgw.genai.serverId=...`).
- Optional logging of the resolved input file list (`-Dgw.logInputs=true`).

## Getting Started

### Prerequisites

- Java installed.
- Maven installed.
- Access to a GenAI provider configured for Ghostwriter (if using an online provider).
- Optional: Maven `settings.xml` with a `<server>` entry if using `gw.genai.serverId`.

### Java Version

- **Build/runtime target in this project:** Java **8** (from `pom.xml`: `maven.compiler.release=8`).
- **Functional requirement:** depends on the Ghostwriter runtime and your selected GenAI provider/client libraries. The plugin targets Java 8, but some provider stacks may require a newer Java version.

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

1. Add or update `@guidance` directives in project files.
2. (Optional) Provide additional instructions via `-Dgw.instructions=...`.
3. Run a goal:
   - `mvn gw:gw` for reverse module order (sub-modules first, then parent).
   - `mvn gw:reactor` for Maven reactor dependency ordering.
4. Review the generated/updated outputs and commit the results.

## Configuration

Common parameters (set via `-D...` system properties or plugin configuration where applicable):

| Parameter | Description | Default |
|---|---|---|
| `gw.genai` | GenAI provider/model identifier forwarded to the workflow. | (none) |
| `gw.genai.serverId` | `settings.xml` `<server>` id used to read GenAI credentials and map them to `GENAI_USERNAME` / `GENAI_PASSWORD`. | (none) |
| `gw.scanDir` | Scan root directory. When omitted, uses the Maven execution root directory. | execution root directory |
| `gw.instructions` | Instruction locations consumed by the workflow. | (none) |
| `gw.guidance` | Default guidance text forwarded to the workflow. | (none) |
| `gw.excludes` | Exclude patterns/paths to skip while scanning. | (none) |
| `gw.logInputs` | Log the list of input files passed to the workflow. | `false` |
| `gw.threads` | Enable multi-threaded module processing for `gw:gw`. | `false` |
| `gw.rootProjectLast` | For `gw:reactor`, delay processing the execution-root project until other reactor projects complete. | `false` |

## Resources

- Ghostwriter (official documentation): https://machai.machanism.org/ghostwriter/index.html
- Guided file processing (concept and ordering): https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
