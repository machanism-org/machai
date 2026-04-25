---
<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.

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
canonical: https://machai.machanism.org/gw-maven-plugin/index.html
---

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

## Introduction

GW Maven Plugin is the primary Maven adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It brings MachAI Ghostwriter’s [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) model into Maven so guided updates and prompt-driven actions can run directly within normal build and project-maintenance workflows.

The plugin is implemented around Maven mojos in `org.machanism.machai.gw.maven`. Guided goals use `GuidanceProcessor` to scan files for embedded `@guidance:` instructions and apply repeatable updates across source code, documentation, Maven site content, and other project files. Action goals use `ActProcessor` to execute one-off or reusable prompt-based transformations. The plugin also integrates with Maven session and reactor context, supports module-aware execution strategies, exposes Java class inspection tools to Ghostwriter processing, and can load provider credentials from Maven `settings.xml` using `genai.serverId` so secrets do not need to be stored in project files.

## Overview

The GW Maven Plugin makes Ghostwriter automation reproducible and natural for Maven-based projects. Instead of running documentation or maintenance workflows outside the build environment, teams can invoke dedicated Maven goals that understand the current project layout, Maven reactor state, and execution root.

The plugin provides two main workflow styles:

- **Guided processing** with `gw:gw` and `gw:gw-per-module`, designed for files that contain embedded `@guidance:` instructions.
- **Action processing** with `gw:act` and `gw:act-per-module`, designed for prompt-driven transformations and reusable act definitions.

It also supports two execution models:

- **Aggregator execution** for broad project scanning from the execution root, including reverse-order module handling for `gw:gw`.
- **Per-module reactor execution** for standard Maven dependency-ordered processing in `gw:gw-per-module` and `gw:act-per-module`.

This combination helps teams keep documentation and other maintained assets aligned with the actual codebase while fitting into familiar Maven commands and automation pipelines.

## Key Features

- Integrates Ghostwriter guided file processing directly into Maven goals.
- Supports both guided `@guidance:` workflows and action-based prompt workflows.
- Provides aggregator goals `gw:gw` and `gw:act` for execution-root orchestration.
- Provides reactor-oriented goals `gw:gw-per-module` and `gw:act-per-module` for standard Maven module execution.
- Supports reverse-order module processing for `gw:gw`, where submodules are processed before parent modules.
- Can run `gw:gw` with `requiresProject=false`, allowing use even when a `pom.xml` is not present in the current directory.
- Reads optional provider credentials and custom configuration from Maven `settings.xml` via `genai.serverId`.
- Supports configurable model selection, scan directories, instructions, excludes, acts locations, action prompts, and input logging.
- Can prompt interactively for `gw:act` input when no `gw.act` value is supplied.
- Includes `gw:clean` to remove temporary workflow artifacts.
- Exposes Java class discovery and reflective inspection tools during processing for project-aware analysis.

## Getting Started

### Prerequisites

- **Java**, with the plugin compiled for Java 8 compatibility and runtime compatibility determined by Maven, resolved dependencies, and selected AI/provider integrations.
- **Apache Maven**.
- **Resolved Ghostwriter dependencies**, including `org.machanism.machai:ghostwriter`.
- **A configured GenAI provider/model** when your workflow requires model-backed execution, usually provided through `gw.model`.
- **Optional Maven `settings.xml` credentials** when your provider authentication is supplied through `genai.serverId`.
- **Project files to process**, including files containing `@guidance:` blocks for guided execution or a supplied action prompt / act definition for action execution.

Example `settings.xml` server entry:

```xml
<settings>
  <servers>
    <server>
      <id>my-genai</id>
      <username>...</username>
      <password>...</password>
    </server>
  </servers>
</settings>
```

Example invocation using that server id:

```bash
mvn gw:gw -Dgenai.serverId=my-genai
```

### Java Version

- **Java version defined in `pom.xml`:** Java **8** via `maven.compiler.release=8`.
- **Practical runtime note:** actual functional runtime requirements may differ because execution depends on the Maven runtime, the resolved Ghostwriter dependency graph, and the configured AI provider stack.

### Basic Usage

Run guided processing from the current project:

```bash
mvn gw:gw
```

Scan a specific directory:

```bash
mvn gw:gw -Dgw.scanDir=src/site
```

Run guided processing with an explicit model:

```bash
mvn gw:gw -Dgw.model=openai:gpt-4o-mini -Dgw.scanDir=src/site
```

Run an action prompt against documentation content:

```bash
mvn gw:act -Dgw.act="Rewrite headings for clarity" -Dgw.scanDir=src/site
```

Run the per-module guided goal:

```bash
mvn gw:gw-per-module
```

### Typical Workflow

1. Identify the source, documentation, site, or other project files you want Ghostwriter to maintain.
2. Add or refine embedded `@guidance:` blocks for deterministic guided updates where appropriate.
3. Choose the correct goal for the workflow:
   - `gw:gw` for aggregator guided processing with reverse-order module handling.
   - `gw:gw-per-module` for standard Maven reactor guided processing.
   - `gw:act` for aggregator prompt-driven action processing.
   - `gw:act-per-module` for reactor-context action processing.
4. Provide any required configuration such as `gw.model`, `gw.scanDir`, `gw.instructions`, `gw.excludes`, `gw.act`, `gw.acts`, or `genai.serverId`.
5. Run the selected Maven goal.
6. Review the generated file changes in your normal diff or code review process.
7. Commit the verified updates.
8. Optionally remove temporary workflow artifacts with:

```bash
mvn gw:clean
```

## Configuration

Common configuration parameters supported by the plugin:

| Parameter | Description | Default value |
|---|---|---|
| `gw.model` | Provider/model identifier forwarded to Ghostwriter processing. | *(none)* |
| `gw.scanDir` | Scan root for files to process. Defaults to the execution root for `gw:gw`; `gw:gw-per-module` initializes it from the current module base directory; action goals fall back to the configured value or module base directory. | *(goal-dependent)* |
| `gw.instructions` | Instruction locations consumed by guided processing, such as file paths or classpath resources. | *(none)* |
| `gw.excludes` | Exclude patterns or paths skipped during scanning. | *(none)* |
| `genai.serverId` | Maven `settings.xml` server id used to load provider credentials and additional XML configuration values. | *(none)* |
| `logInputs` | Whether to log the workflow input file list. | `false` |
| `gw.act` | Action prompt used by `gw:act` and `gw:act-per-module`; if omitted for `gw:act`, the plugin can prompt interactively. | *(none)* |
| `gw.acts` | Optional location containing predefined act definitions. | *(none)* |
| `gw.threads` | Goal-specific flag documented by `gw:gw` for multi-threaded module processing. | `false` |

## Resources

- Official MachAI site: https://machai.machanism.org/
- Ghostwriter application: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai.git
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- Project SCM URL: https://github.com/machanism-org/machai.git
