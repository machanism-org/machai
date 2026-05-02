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

GW Maven Plugin is the primary Maven adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It brings MachAI Ghostwriter’s [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) model into Maven so guided updates and prompt-driven actions can run directly within normal build, documentation, and maintenance workflows.

The plugin is built around Maven mojos that connect Maven execution context with Ghostwriter processors. Guided goals drive repeatable updates for files containing embedded `@guidance:` instructions, while action goals support prompt-driven transformations and reusable act definitions. The implementation integrates with Maven session and reactor information, supports both execution-root and per-module workflows, can register Java class inspection tools for project-aware processing, and can load provider credentials from Maven `settings.xml` through `genai.serverId` so secrets do not need to be stored in project files.

## Overview

The GW Maven Plugin makes Ghostwriter automation reproducible and natural for Maven-based projects. Instead of running documentation or maintenance flows outside the build system, teams can invoke Maven goals that understand the current project layout, execution root, reactor state, and classpath visibility.

The plugin supports two main workflow styles:

- **Guided processing** for files containing embedded `@guidance:` instructions.
- **Action processing** for one-off prompts and reusable act definitions.

It also supports two execution models:

- **Execution-root orchestration**, where the plugin scans from the root context and can coordinate across modules.
- **Per-module reactor execution**, where processing follows standard Maven reactor behavior for each participating module.

At a project level, the plugin acts as a bridge between the developer, the Maven runtime, Ghostwriter processing pipelines, project files, and optional AI providers. Shared configuration logic prepares scan roots, exclusions, instructions, settings-based credentials, and class-aware tools. Guided and action pipelines then scan source code, documentation, site content, and other relevant files and apply updates based on embedded guidance or prompt input. Class introspection support enriches the workflow with project-aware metadata derived from compiled classes, source locations, and dependency artifacts.

![Project structure overview](./images/project-structure/c4-diagram.png)

The diagram shows a developer invoking Maven goals that run inside the Maven runtime. Inside the plugin boundary, goal implementations share common configuration handling and delegate work either to guided processing or to act-based processing. Those processing paths interact with project files, optional GenAI services, and class introspection support backed by project outputs and dependency metadata. This architecture allows the plugin to keep AI-assisted maintenance tightly aligned with Maven execution and project structure.

## Key Features

- Integrates Ghostwriter guided file processing directly into Maven goals.
- Supports both embedded `@guidance:` workflows and prompt-driven act workflows.
- Provides execution-root goals for broad project scanning and orchestration.
- Provides per-module goals that align with standard Maven reactor execution.
- Supports reverse-order handling for guided execution-root processing so submodules can be processed before parents.
- Can run the main guided goal with `requiresProject=false`, allowing use even when a `pom.xml` is not present in the current directory.
- Reads optional provider credentials and custom configuration from Maven `settings.xml` via `genai.serverId`.
- Supports configurable model selection, scan directories, instructions, excludes, act locations, action prompts, and input logging.
- Can prompt interactively for act input when no explicit action prompt is supplied.
- Includes a cleanup goal to remove temporary workflow artifacts.
- Exposes Java class discovery and reflective inspection tools during processing for project-aware analysis.

## Getting Started

### Prerequisites

- **Java**, with the plugin compiled for Java 8 compatibility and actual runtime compatibility influenced by the Maven runtime, resolved dependencies, and selected provider stack.
- **Apache Maven**.
- **Resolved Ghostwriter dependencies**, including `org.machanism.machai:ghostwriter`.
- **A configured GenAI provider/model** when your workflow requires model-backed execution, typically supplied through `gw.model`.
- **Optional Maven `settings.xml` credentials** when provider authentication is supplied through `genai.serverId`.
- **Project files to process**, including files containing `@guidance:` blocks for guided execution or a supplied action prompt or act definition for action processing.

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
- **Practical runtime note:** actual functional runtime requirements may differ because execution also depends on the Maven runtime, the resolved Ghostwriter dependency graph, and the configured AI/provider integrations.

### Basic Usage

```bash
mvn gw:gw
```

Additional examples:

```bash
mvn gw:gw -Dgw.scanDir=src/site
mvn gw:gw -Dgw.model=openai:gpt-4o-mini -Dgw.scanDir=src/site
mvn gw:act -Dgw.act="Rewrite headings for clarity" -Dgw.scanDir=src/site
mvn gw:gw-per-module
mvn gw:act-per-module -Dgw.act="Summarize module documentation"
```

### Typical Workflow

1. Identify the source, documentation, site, or other project files you want Ghostwriter to maintain.
2. Add or refine embedded `@guidance:` blocks for deterministic guided updates where appropriate.
3. Choose the appropriate goal for the workflow:
   - `gw:gw` for execution-root guided processing.
   - `gw:gw-per-module` for reactor-oriented guided processing.
   - `gw:act` for execution-root prompt-driven action processing.
   - `gw:act-per-module` for reactor-context action processing.
4. Provide any required configuration such as `gw.model`, `gw.scanDir`, `gw.instructions`, `gw.excludes`, `gw.act`, `gw.acts`, or `genai.serverId`.
5. Run the selected Maven goal.
6. Review the resulting file changes using your normal diff or code review process.
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
| `gw.scanDir` | Scan root for files to process. Defaults vary by goal and execution context. | *(goal-dependent)* |
| `gw.instructions` | Instruction locations consumed by guided processing, such as file paths or classpath resources. | *(none)* |
| `gw.excludes` | Exclude patterns or paths skipped during scanning. | *(none)* |
| `genai.serverId` | Maven `settings.xml` server id used to load provider credentials and additional XML configuration values. | *(none)* |
| `logInputs` | Whether to log the workflow input file list. | `false` |
| `gw.act` | Action prompt used by action goals; if omitted for interactive workflows, the plugin can prompt for it. | *(none)* |
| `gw.acts` | Optional location containing predefined act definitions. | *(none)* |
| `gw.threads` | Goal-specific threading control documented for execution-root guided processing. | `false` |

## Resources

- Official MachAI site: https://machai.machanism.org/
- Ghostwriter application: https://machai.machanism.org/ghostwriter/index.html
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- GitHub repository: https://github.com/machanism-org/machai.git
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- Project SCM URL: https://github.com/machanism-org/machai.git
- Apache Maven documentation: https://maven.apache.org/
