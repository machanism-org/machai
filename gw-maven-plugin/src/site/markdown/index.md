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

GW Maven Plugin is the primary Maven adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It brings MachAI Ghostwriter’s [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) model into Maven builds so teams can automate updates across source code, project documentation, Maven site content, and other scanned files that contain embedded `@guidance:` instructions.

The plugin wraps Ghostwriter processors in Maven goals under `org.machanism.machai.gw.maven`. Guided goals use `GuidanceProcessor` to scan files and apply repeatable, instruction-driven changes, while action goals use `ActProcessor` to run prompt-based transformations across a selected file set. The plugin also integrates with Maven session and reactor metadata, can inspect project classes through helper tools, and can read provider credentials from Maven `settings.xml` so secrets stay out of the project source tree.

## Overview

The GW Maven Plugin makes Ghostwriter execution repeatable, automatable, and natural for Maven users. Instead of running document and content automation outside the build, you can invoke dedicated Maven goals that operate within the current project context, understand multi-module builds, and reuse familiar Maven conventions.

The plugin supports two main processing styles:

- **Guided processing** for files that already include embedded `@guidance:` directives and need deterministic, repeatable updates.
- **Action processing** for one-off or reusable prompt-driven tasks such as rewriting sections, refining structure, or applying a predefined act.

It also offers two module-processing modes:

- **Aggregator goals** (`gw:gw`, `gw:act`) that can work from the execution root and coordinate broader scans.
- **Per-module goals** (`gw:gw-per-module`, `gw:act-per-module`) that align with standard Maven reactor execution.

Together, these goals help keep code-adjacent documentation and other maintained assets synchronized with the actual state of the project.

## Key Features

- Integrates Ghostwriter guided file processing directly into Maven.
- Supports both **guided** and **action-based** workflows.
- Provides **aggregator goals** for execution-root orchestration and CLI-like behavior.
- Provides **reactor-friendly per-module goals** that follow normal Maven build execution.
- Can run `gw:gw` **without a `pom.xml`** in the current directory.
- Processes modules in different ways depending on the goal:
  - `gw:gw` processes submodules first, then parent modules, mirroring Ghostwriter CLI reverse-order behavior.
  - `gw:gw-per-module` follows standard Maven reactor dependency ordering.
- Supports configurable scan roots, instruction sources, excludes, model selection, and input logging.
- Can prompt interactively for `gw:act` input when no action text is supplied.
- Reads GenAI credentials and additional server configuration from Maven `settings.xml` via `genai.serverId`.
- Includes a `gw:clean` goal to remove temporary workflow artifacts.
- Exposes class-discovery helper tools so Ghostwriter processing can inspect project and dependency classes when running inside a Maven project.

## Getting Started

### Prerequisites

- **Java** runtime compatible with the plugin build and the selected provider stack.
- **Apache Maven**.
- **MachAI Ghostwriter dependencies** resolved by Maven.
- **A configured GenAI model/provider** supplied through `gw.model` when required by the workflow.
- **Optional Maven credentials** in `~/.m2/settings.xml` when your provider requires authentication through `genai.serverId`.
- **Project files containing `@guidance:` blocks** for guided processing, or an action prompt / act definition for action processing.

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
- **Practical runtime note:** actual execution requirements may differ because the plugin runs inside Maven and delegates to Ghostwriter and provider integrations. Your effective runtime must remain compatible with your Maven installation, resolved dependencies, and selected AI provider configuration.

### Basic Usage

Run guided processing from the current project:

```bash
mvn gw:gw
```

Process a specific scan root:

```bash
mvn gw:gw -Dgw.scanDir=src/site
```

Run guided processing with an explicit model:

```bash
mvn gw:gw -Dgw.model=openai:gpt-4o-mini -Dgw.scanDir=src/site
```

Run an action prompt across documentation files:

```bash
mvn gw:act -Dgw.act="Rewrite headings for clarity" -Dgw.scanDir=src/site
```

Run the reactor-oriented guided goal:

```bash
mvn gw:gw-per-module
```

### Typical Workflow

1. Add or update project files that should be maintained with Ghostwriter.
2. Embed `@guidance:` blocks where repeatable automation rules are needed.
3. Choose the appropriate goal:
   - `gw:gw` for reverse-order aggregator processing.
   - `gw:gw-per-module` for Maven reactor ordering.
   - `gw:act` for interactive or prompt-based action processing.
   - `gw:act-per-module` for reactor-context action execution.
4. Supply configuration such as `gw.scanDir`, `gw.model`, `gw.excludes`, `gw.instructions`, or `genai.serverId`.
5. Run the Maven goal and let Ghostwriter scan and update matching files.
6. Review generated changes in your diff or code review workflow.
7. Commit the updates.
8. Optionally run cleanup when needed:

```bash
mvn gw:clean
```

## Configuration

Common configuration parameters supported by the plugin:

| Parameter | Description | Default value |
|---|---|---|
| `gw.model` | Provider/model identifier passed to Ghostwriter. | *(none)* |
| `gw.scanDir` | Root directory to scan for files. For `gw:gw`, it defaults to the execution root when omitted; for per-module usage it is commonly the current module base directory. | *(goal-dependent)* |
| `gw.instructions` | Instruction locations consumed by the workflow, such as file paths or classpath resources. | *(none)* |
| `gw.excludes` | Comma-separated exclude patterns or paths skipped during scanning. | *(none)* |
| `genai.serverId` | Maven `settings.xml` server id used to load credentials and additional server configuration. | *(none)* |
| `logInputs` | Logs the files passed into the workflow. | `false` |
| `gw.act` | Action prompt used by `gw:act` and `gw:act-per-module`. If omitted for `gw:act`, the plugin can prompt interactively. | *(none)* |
| `gw.acts` | Location of predefined act definitions for action processing. | *(none)* |
| `gw.threads` | Enables or disables multi-threaded module processing for `gw:gw`, as documented by the goal. | `false` |

## Resources

- Official MachAI site: https://machai.machanism.org/
- Ghostwriter documentation: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing concept: https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai.git
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- Project API and site reports are typically published from the Maven site generated for this module.
