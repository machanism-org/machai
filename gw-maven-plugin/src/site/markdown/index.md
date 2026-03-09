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

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

## Introduction

GW Maven Plugin is the primary Maven adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It brings MachAI Ghostwriter’s *Guided File Processing* model into Maven builds, so documentation can be scanned, evaluated against embedded `@guidance:` blocks, and updated consistently across a project.

At its core, the plugin wires Maven execution context (module base directories, reactor ordering, and optional credentials from `~/.m2/settings.xml`) into Ghostwriter processors:

- `GuidanceProcessor` for guided documentation processing (`gw:gw`, `gw:reactor`)
- `ActProcessor` for applying an interactive or predefined “act” prompt across a scanned document tree (`gw:act`, `gw:act-reactor`)

Conceptually, this follows the same foundation described in [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): documents contain embedded intent (guidance), and automation applies that intent repeatedly and safely so project documentation stays correct, current, and consistent.

## Overview

The GW Maven Plugin enhances documentation workflows by:

- Making Ghostwriter runs repeatable and Maven-native (CI-friendly and consistent across developers).
- Supporting both aggregator-style processing (reverse-order module processing similar to the Ghostwriter CLI) and true Maven reactor ordering.
- Allowing prompts and instructions to be injected via Maven properties and plugin configuration.
- Optionally sourcing GenAI credentials via Maven `settings.xml`, keeping secrets out of source control.

In practice, you use the plugin to scan a directory (often `src/site`) for documentation files, process any `@guidance:` blocks, and write updated content back to disk—without manual copy/paste between tools.

## Key Features

- **Guided File Processing integration** using Ghostwriter processors.
- **Multiple execution modes**:
  - `gw:gw`: aggregator goal that processes modules in reverse order (sub-modules first, then parents), similar to the Ghostwriter CLI.
  - `gw:reactor`: reactor-aware processing aligned with Maven dependency ordering, with an option to defer execution-root processing.
- **Interactive and predefined actions**:
  - `gw:act`: prompts for an “act” if not provided.
  - `gw:act-reactor`: reactor-friendly variant intended for execution-root context.
- **Configurable scan root and exclusions** to focus processing on documentation sources.
- **Credential integration with Maven settings** via `-Dgw.genai.serverId=...`.
- **Optional input logging** for transparency and repeatability.
- **Cleanup support** via `gw:clean` to remove temporary artifacts created during processing.

## Getting Started

### Prerequisites

- **Apache Maven** (run as a standard Maven plugin).
- **A configured GenAI provider** supported by Ghostwriter.
- **Credentials (optional but typical)**:
  - Stored in `~/.m2/settings.xml` under a `<server>` entry.
  - Referenced via `-Dgw.genai.serverId=<serverId>`.

### Java Version

- **Build/toolchain requirement (defined by this module):** Java **8** (`maven.compiler.release=8` in `pom.xml`).
- **Functional/runtime requirements:** may be higher depending on the selected Ghostwriter/GenAI provider stack and your Maven runtime. In general, run with a Java version compatible with both Maven and your chosen Ghostwriter dependencies.

### Basic Usage

Run guided processing in the current project:

```bash
mvn gw:gw
```

Run with explicit model and scan root:

```bash
mvn gw:gw -Dgw.model=openai:gpt-4o-mini -Dgw.scanDir=src\site
```

Run a one-off action prompt over your documentation tree:

```bash
mvn gw:act -Dgw.act="Rewrite headings for clarity" -Dgw.scanDir=src\site
```

### Typical Workflow

1. **Add/maintain documentation** (commonly under `src/site`) and embed `@guidance:` blocks where you want automation.
2. **Run the plugin**:
   - Use `gw:gw` when you want aggregator-style processing (reverse module order, CLI-like behavior).
   - Use `gw:reactor` when you want reactor dependency ordering.
3. **Review changes** in your VCS diff.
4. **Commit updates** so documentation stays in sync with code.
5. (Optional) **Clean temporary files**:

```bash
mvn gw:clean
```

## Configuration

Common configuration parameters (usable as `-D...` system properties or via `<configuration>` in your `pom.xml`):

| Parameter | Description | Default |
|---|---|---|
| `gw.model` | Provider/model identifier forwarded to Ghostwriter (example: `openai:gpt-4o-mini`). | *(none)* |
| `gw.scanDir` | Scan root directory to process. If not provided, defaults to the Maven execution root directory for `gw:gw` and the module base directory for reactor-style usage. | *(varies by goal)* |
| `gw.instructions` | Instruction locations consumed by the workflow (for example, file paths or classpath locations). | *(none)* |
| `gw.guidance` | Default guidance text forwarded to the workflow. | *(none)* |
| `gw.excludes` | Exclude patterns/paths to skip while scanning documentation sources. | *(none)* |
| `gw.genai.serverId` | `settings.xml` `<server>` id used to load GenAI credentials into `GENAI_USERNAME`/`GENAI_PASSWORD`. | *(none)* |
| `gw.logInputs` | Logs the list of input files passed to the workflow. | `false` |
| `gw.threads` | Enables/disables multi-threaded module processing for `gw:gw`. | `false` |
| `gw.rootProjectLast` | For `gw:reactor`: if `true`, defers execution-root project processing until other reactor projects complete. | `true` |
| `gw.act` | For `gw:act` / `gw:act-reactor`: the action prompt to apply (interactive if omitted for `gw:act`). | *(none)* |
| `gw.acts` | For `gw:act`: directory containing predefined action definitions. | *(none)* |

## Resources

- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- Ghostwriter documentation: https://machai.machanism.org/ghostwriter/index.html
- Source repository (SCM): https://github.com/machanism-org/machai.git
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
