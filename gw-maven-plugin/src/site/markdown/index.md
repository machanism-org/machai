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
3. **Overview**
   - Clearly explain the main functions and value proposition of the GW Maven plugin.
   - Summarize how the plugin enhances project workflows and documentation.
   Describe the project with diagrams bellow:
     - Create a project structure overview based on the `/src/site/puml/**/*.puml` files.
     - Describe the project without including file names in the description.
     - Use the project structure diagram by the path: `./images/project-structure/c4-diagram.png` (`src/site/puml/project-structure/c4-diagram.puml`).
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

GW Maven Plugin is the primary Maven adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It brings MachAI Ghostwriter’s [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) approach into Maven so teams can run guided updates, prompt-driven actions, and repeatable documentation maintenance directly from standard build workflows.

The plugin is implemented as a set of Maven goals that bridge Maven execution context with Ghostwriter processors. Shared goal infrastructure resolves scan locations, exclusions, optional external instructions, logging behavior, Maven session state, and credentials loaded from `settings.xml` through `genai.serverId`. Guided goals execute document scanning and update flows for files containing embedded `@guidance:` instructions, while action goals run interactive or predefined acts over scanned project content. The implementation also exposes Java class discovery and reflective inspection tools, allowing Ghostwriter workflows to reason about the project’s own compiled classes, source roots, and dependency artifacts.

## Overview

The GW Maven Plugin makes Ghostwriter automation a native part of Maven-based development. Instead of running AI-assisted maintenance outside the build system, teams can invoke Maven goals that understand module structure, execution root behavior, reactor state, project metadata, and classpath information.

The plugin supports two complementary workflow styles:

- **Guided processing**, where Ghostwriter scans files containing embedded `@guidance:` instructions and applies targeted updates.
- **Act-based processing**, where Ghostwriter executes a prompt or reusable act definition across the selected project content.

It also supports two execution patterns:

- **Execution-root orchestration**, where work starts from the execution root and can coordinate across modules.
- **Per-module reactor execution**, where processing follows Maven’s normal reactor flow for each module.

![Project structure overview](./images/project-structure/c4-diagram.png)

At a structural level, a developer invokes Maven goals that run inside the Maven runtime and pass through a shared configuration layer. From there, processing is delegated either to guided workflows, act workflows, or cleanup behavior. Guided and act workflows interact with project files, optional GenAI services, interactive console input for prompted actions, and a class-introspection layer that can inspect project outputs and dependency metadata. This design keeps automated maintenance tightly aligned with Maven execution while remaining aware of project structure, source code, documentation content, and build artifacts.

## Key Features

- Integrates Ghostwriter guided file processing directly into Maven goals.
- Supports both embedded `@guidance:` workflows and prompt-driven act workflows.
- Provides execution-root goals for broad project scanning and cross-module orchestration.
- Provides per-module goals that follow standard Maven reactor execution.
- Processes execution-root guided workflows in reverse order so submodules can be handled before parent modules.
- Allows the main guided goal to run with `requiresProject=false`, enabling execution even when no `pom.xml` is present in the current directory.
- Reads optional provider credentials and custom XML configuration from Maven `settings.xml` via `genai.serverId`.
- Supports configurable model selection, scan directories, instructions, excludes, custom act locations, action prompts, and input logging.
- Supports interactive prompt collection for act execution when no explicit act is provided.
- Includes a cleanup goal for removing temporary workflow artifacts.
- Exposes Java class discovery and reflective metadata lookup tools for project-aware processing.

## Getting Started

### Prerequisites

- **Java**, with the plugin compiled for Java 8 compatibility.
- **Apache Maven** to execute the plugin goals.
- **Resolved Ghostwriter dependency stack**, including `org.machanism.machai:ghostwriter`.
- **A configured GenAI provider/model** when using model-backed workflows, typically supplied through `gw.model`.
- **Optional Maven `settings.xml` credentials** when provider authentication or additional provider configuration is supplied through `genai.serverId`.
- **Project files to process**, including source, documentation, site, or other files, especially files that contain embedded `@guidance:` blocks for guided processing.
- **Interactive console access** when running act goals without explicitly providing `gw.act`.

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

### Java Version

- **Java version defined in `pom.xml`:** Java **8** via `maven.compiler.release=8`.
- **Practical runtime note:** actual functional requirements may differ depending on the Maven runtime, resolved dependency graph, selected AI provider stack, and the environment in which Ghostwriter processing executes.

### Basic Usage

```bash
mvn gw:gw
```

Additional examples:

```bash
mvn gw:gw -Dgw.path=src/site
mvn gw:gw -Dgw.model=openai:gpt-4o-mini -Dgw.path=src/site
mvn gw:act -Dgw.act="Rewrite headings for clarity" -Dgw.path=src/site
mvn gw:gw-per-module
mvn gw:act-per-module -Dgw.act="Summarize module documentation"
mvn gw:clean
```

### Typical Workflow

1. Identify the source, documentation, site, or other project files you want Ghostwriter to maintain.
2. Add or refine embedded `@guidance:` blocks where guided processing should produce deterministic updates.
3. Choose the appropriate goal:
   - `gw:gw` for execution-root guided processing.
   - `gw:gw-per-module` for reactor-oriented guided processing.
   - `gw:act` for execution-root prompt-driven action processing.
   - `gw:act-per-module` for reactor-context action processing.
   - `gw:clean` to remove temporary workflow artifacts.
4. Supply any required configuration such as `gw.model`, `gw.path`, `gw.instructions`, `gw.excludes`, `gw.act`, `gw.acts`, `genai.serverId`, or `logInputs`.
5. Run the selected Maven goal.
6. Review the generated or updated file changes using normal diff or code review practices.
7. Commit the verified updates.

## Configuration

Common configuration parameters supported by the plugin:

| Parameter | Description | Default value |
|---|---|---|
| `gw.model` | Provider/model identifier forwarded to Ghostwriter processing. | *(none)* |
| `gw.path` | Scan root for files to process. When omitted, execution-root goals default to the execution root and per-module guided processing defaults to the current module base directory. | *(goal-dependent)* |
| `gw.instructions` | Instruction locations consumed by guided or act processing, such as file paths or classpath resources. | *(none)* |
| `gw.excludes` | Exclude patterns or paths skipped during scanning. | *(none)* |
| `genai.serverId` | Maven `settings.xml` server id used to load provider credentials and additional XML configuration values. | *(none)* |
| `logInputs` | Whether to log the workflow input file list. | `false` |
| `gw.act` | Action prompt used by action goals; when omitted, act goals can request it interactively. | *(none)* |
| `gw.acts` | Optional location containing predefined act definitions. | *(none)* |
| `gw.threads` | Enables propagation of Maven parallel execution settings for the execution-root guided goal. | `false` |

## Resources

- Official MachAI site: https://machai.machanism.org/
- Ghostwriter application: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai.git
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- Apache Maven documentation: https://maven.apache.org/
- Project SCM URL: https://github.com/machanism-org/machai.git
