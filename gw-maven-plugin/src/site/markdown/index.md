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
   - Bindex Badge [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/gw-maven-plugin/bindex.json)
2. Introduction
   - Provide a comprehensive description of the GW Maven plugin, including its purpose and benefits.
   - Analyze java files in the `/src/main/java/org/machanism/machai/gw/maven` to inform the description.
   - Be careful when describing examples of using goals; they must accurately reflect the requirements described in the Javadoc of the implementation classes, 
       e.g.: `-Dgw.act="[act name] [additional user prompt]"`. If only the user prompt for the act mode is used, the first character must be. 
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
   - Explain how to enable debug logging for specific application components. (i.e., `-Dorg.slf4j.simpleLogger.log.[fully-qualified-class-name]=[LEVEL]`).
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
[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/gw-maven-plugin/bindex.json)

## Introduction

GW Maven Plugin is the primary Maven adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It brings Machai Ghostwriter automation into Maven-based projects so teams can scan, analyze, and update project assets using embedded guidance comments and explicit act prompts.

The plugin is built around [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html), where instructions live close to the files they govern. Ghostwriter works with all types of project files, including source code, documentation, site content, configuration, generated metadata, and other relevant artifacts. In a Maven project, this plugin supplies project layout awareness, reactor/module traversal, Maven settings integration, class-introspection tools, and convenient goals for both guidance-tag processing and user-directed act execution.

The implementation provides two main goals:

- `gw:gw` scans project files for guidance comments and processes matching content with Ghostwriter.
- `gw:act` runs a named act or a user prompt across selected project content. For an act with additional prompt text, use a value such as `-Dgw.act="review Focus on public APIs"`; when supplying only a user prompt instead of an act name, start the prompt with `>`, for example `-Dgw.act=">Add missing Javadocs"`.

Both goals can run without a `pom.xml`, are safe for threaded execution, and coordinate multi-module traversal through Ghostwriter. When Maven parallel execution is enabled, module processing is handled by `gw` rather than by the Maven reactor, with sub-modules processed before parent modules.

## Overview

GW Maven Plugin enhances project workflows by making documentation and maintenance automation repeatable from standard Maven commands. It resolves configuration from Maven parameters and settings, initializes Ghostwriter processors, supplies Maven project context, and registers helper tools for class-level project analysis when a Maven project is available.

The plugin is especially useful for keeping documentation synchronized with implementation details, applying consistent review or generation rules across modules, and automating edits requested through guidance tags. Because it operates on the full project tree, teams can use it for source code, tests, documentation, site pages, configuration, and other project assets.

![Project structure overview](./images/project-structure/c4-diagram.png)

The project structure centers on a Maven plugin layer that adapts Maven execution context to Ghostwriter processors. Shared base behavior resolves configuration, credentials, project roots, scan paths, exclusions, and class-analysis tooling. Goal-specific implementations then either process guidance comments found in project files or execute an act prompt against selected content. Supporting tooling exposes project-class information to the AI workflow so generated changes can be informed by the current codebase.

## Key Features

- Maven-native goals for guidance-tag processing and act-driven Ghostwriter automation.
- Works with source code, documentation, site content, configuration, and other project files.
- Multi-module processing with sub-modules handled before parent modules.
- Parallel execution support through Maven, for example `mvn -T 4 gw:gw` or `mvn -T 4 gw:act`.
- Optional execution without a `pom.xml` for non-standard or lightweight workspaces.
- Maven settings integration for AI provider credentials via a configurable server id.
- Configurable scan paths, exclusions, model selection, and additional instructions.
- Interactive prompting for `gw:act` when an act prompt is not supplied.
- Class-introspection tools registered during Maven project execution to improve context-aware processing.
- Usage statistics lifecycle hooks around processing execution.

## Getting Started

### Prerequisites

- Apache Maven with access to the project where Ghostwriter processing should run.
- Java runtime compatible with the plugin and the Maven build.
- Network access to the configured AI provider, when the selected provider requires remote API calls.
- AI provider credentials configured through environment variables, Maven settings, or provider-specific configuration supported by Ghostwriter.
- Optional Maven `settings.xml` server entry when using `-Dgenai.serverId=...` to resolve credentials.
- Project files containing `@guidance:` comments for `gw:gw`, or an act prompt/act definition for `gw:act`.

### Java Version

The build configuration defines `<maven.compiler.release>8</maven.compiler.release>`, so the plugin source is compiled for Java 8 bytecode compatibility. Actual functional requirements may differ depending on the Maven runtime, Ghostwriter dependency, AI provider client, and any project-specific tooling invoked during processing; verify the runtime requirements of the complete toolchain used in your environment.

### Basic Usage

Run guidance-tag processing over the current Maven project:

```bash
mvn org.machanism.machai:gw-maven-plugin:gw
```

Run a user prompt against a specific path:

```bash
mvn org.machanism.machai:gw-maven-plugin:act -Dgw.act=">Add missing Javadocs" -Dgw.path=src/main/java
```

Run a predefined act with additional prompt text:

```bash
mvn org.machanism.machai:gw-maven-plugin:act -Dgw.act="review Focus on public APIs" -Dgw.path=src/main/java
```

Use Maven parallel execution for larger multi-module projects:

```bash
mvn -T 4 org.machanism.machai:gw-maven-plugin:gw
```

### Typical Workflow

1. Add or update `@guidance:` comments near the project content that needs automated maintenance.
2. Configure AI provider credentials using the supported Ghostwriter configuration method or a Maven `settings.xml` server entry.
3. Choose the appropriate goal: `gw:gw` for embedded guidance comments or `gw:act` for a named act/free-form prompt.
4. Limit scope when needed with `-Dgw.path=...` and exclusions.
5. Run the Maven command, optionally with `-T` for parallel execution.
6. Review generated changes carefully, run project tests and documentation builds, then commit accepted updates.
7. Reuse predefined acts by setting `-Dgw.acts=...` and passing the act name through `-Dgw.act=...`.

## Configuration

| Parameter | Description | Default value |
| --- | --- | --- |
| `gw.model` | Provider/model identifier used by Ghostwriter. | Provider configuration default |
| `gw.path` | File, directory, glob, or supported path expression to scan. | Execution root or base directory |
| `gw.instructions` | Additional instructions or instruction locations supplied to processing. | Not set |
| `gw.excludes` | Comma-separated paths or patterns to skip during scanning. | Not set |
| `gw.act` | Act name plus optional prompt text, or direct user prompt prefixed with `>` for `gw:act`. | Prompted interactively or read from configuration |
| `gw.acts` | Directory or path containing predefined act definitions for `gw:act`. | Ghostwriter default act lookup location |
| `gw.interactive` | Enables or disables interactive prompting when act configuration is incomplete. | Processor default |
| `genai.serverId` | Maven `settings.xml` server id used to resolve AI provider username, password, and custom configuration. | Not set |
| `basedir` | Maven module base directory injected by Maven. | `${basedir}` |
| `project` | Current Maven project injected by Maven when a project is present. | `${project}` |
| `session` | Current Maven session injected by Maven. | `${session}` |
| `settings` | Maven settings used to resolve configured server credentials. | `${settings}` |
| `reactorProjects` | Reactor project list injected by Maven for multi-module builds. | `${reactorProjects}` |

### Debug Logging

The plugin uses SLF4J simple logger configuration conventions. Enable debug or trace logging for a specific component by setting a fully qualified class name level:

```bash
mvn gw:gw -Dorg.slf4j.simpleLogger.log.org.machanism.machai.gw.maven.GWMojo=DEBUG
```

To run act diagnostics with highly detailed Mojo steps while muting chatty command-tool logs, combine logging parameters:

```bash
mvn gw:act -Dgw.act=review \
  -Dorg.slf4j.simpleLogger.log.org.machanism.machai.gw.maven.ActMojo=DEBUG \
  -Dorg.slf4j.simpleLogger.log.org.machanism.machai.gw.tools.CommandFunctionTools=ERROR
```

## Resources

- [Machai Ghostwriter](https://machai.machanism.org/ghostwriter/index.html)
- [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html)
- [GitHub repository](https://github.com/machanism-org/machai)
- [GW Maven Plugin on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)
- [GW Maven Plugin Bindex](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/gw-maven-plugin/bindex.json)
