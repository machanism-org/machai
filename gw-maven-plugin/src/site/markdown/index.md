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

GW Maven Plugin integrates the **Ghostwriter** guided file processing workflow into Maven builds to automate and maintain project documentation. It scans your project (source code, documentation, site content, configuration, and other relevant files), detects embedded guidance tags, and orchestrates AI-assisted synthesis to generate consistent, up-to-date artifacts.

The primary goal, `gw:gw`, is designed to work well in multi-module builds and mirrors the Ghostwriter CLI processing approach: it processes sub-modules first and then parent modules (reverse order). It can also be executed even when a `pom.xml` is not present in the current directory (the goal is declared with `requiresProject=false`).

## Overview

At a high level, the plugin:

- Creates a configured Ghostwriter `FileProcessor` from Maven execution context.
- Scans a target directory (defaulting to the Maven execution root) for supported files.
- Applies includes/excludes and optional instruction/guidance inputs.
- Runs guided processing and writes results back into the project, enabling repeatable documentation updates.

This makes documentation automation a first-class part of your Maven workflow, suitable for CI usage and for keeping project sites and docs synchronized with code changes.

## Key Features

- Maven goal to run Ghostwriter guided file processing from the command line or CI
- Works with multi-module reactors; supports reverse-order module processing (sub-modules before parents)
- Can run without a `pom.xml` in the current directory (`requiresProject=false`)
- Configurable scan root (`gw.scanDir`) and exclude patterns (`gw.excludes`)
- Optional instruction sources (`gw.instructions`) and default guidance (`gw.guidance`)
- Optional logging of workflow input files (`gw.logInputs`)
- Optional multi-threaded module processing (`gw.threads`)
- Supports credentials sourced from `~/.m2/settings.xml` via `gw.genai.serverId`

## Getting Started

### Prerequisites

- Java (see version notes below)
- Maven 3.x
- A supported GenAI provider configuration (provider/model string passed via `gw.genai`)
- (Optional) Maven `settings.xml` entry if you want to supply credentials via `gw.genai.serverId`

### Java Version

- **Build configuration (from `pom.xml`):** compiled with `maven.compiler.release=8` (Java 8 bytecode).
- **Practical runtime requirements:** may be higher depending on the runtime requirements of the Ghostwriter/GenAI dependencies and your chosen provider integration.

### Basic Usage

```cmd
mvn gw:gw
```

Common variants:

```cmd
mvn gw:gw -Dgw.scanDir=src\site
```

```cmd
mvn gw:gw -Dgw.genai=openai:gpt-4o-mini
```

```cmd
mvn gw:gw -Dgw.threads=true
```

### Typical Workflow

1. Add guidance tags (and any project-specific instruction files) to the repository.
2. Configure the execution (provider/model, scan root, excludes).
3. Run the goal:
   - `mvn gw:gw`
4. Review generated/updated outputs.
5. Commit the changes and repeat in CI to keep docs current.

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `gw.genai` | Provider/model identifier forwarded to the Ghostwriter workflow. | (none) |
| `gw.scanDir` | Scan root directory. If omitted, defaults to the Maven execution root directory. | `${session.executionRootDirectory}` |
| `gw.instructions` | Instruction locations consumed by the workflow (for example, file paths or classpath locations). | (none) |
| `gw.guidance` | Default guidance text forwarded to the workflow. | (none) |
| `gw.excludes` | Exclude patterns/paths to skip while scanning documentation sources. | (none) |
| `gw.genai.serverId` | `settings.xml` `<server>` id used to read GenAI credentials. | (none) |
| `gw.logInputs` | Logs the list of input files passed to the workflow. | `false` |
| `gw.threads` | Enables multi-threaded module processing. | `false` |

## Resources

- Ghostwriter guided file processing overview: https://www.machanism.org/guided-file-processing/index.html
- GitHub (parent repository): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- Machai / Ghostwriter docs (API links referenced by the build):
  - https://machai.machanism.org/ghostwriter/apidocs/
  - https://macha.machanism.org/core/core-commons/configurator/apidocs/
