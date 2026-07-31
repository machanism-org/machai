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

GW Maven Plugin is the primary Maven adapter for the [Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It brings Machai Ghostwriter automation directly into Maven-based projects so teams can scan, analyze, and update all relevant project files using embedded guidance instructions and AI-assisted synthesis.

The plugin is built around the [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) model: guidance embedded in project artifacts drives controlled updates while preserving the project’s structure and intent. Ghostwriter is not limited to documentation; it can work with source code, project site pages, configuration, tests, diagrams, and other files that participate in the development lifecycle.

Within Maven, the plugin provides goals for full guided processing, predefined or interactive actions, per-module execution, and cleanup. It integrates with Maven sessions and reactor projects, supports non-recursive and parallel builds, resolves model and provider configuration from Maven properties and settings, and exposes class-introspection tools when a Maven project is available.

## Overview

GW Maven Plugin improves project workflows by making AI-assisted maintenance repeatable from the build tool developers already use. It scans project content for guidance, applies a configured Ghostwriter workflow, and writes updates back to the relevant files. This enables consistent documentation, synchronized implementation details, guided refactoring support, and automated maintenance across multi-module Maven projects.

The plugin’s value proposition is to connect Maven project metadata, reactor structure, dependency-aware class discovery, and Ghostwriter’s guided processing engine into one execution path. Child modules can be processed before parent modules, matching Ghostwriter CLI behavior and helping aggregate or parent-level content reflect already-updated module content.

The project structure centers on a Maven plugin layer that delegates common configuration and scan orchestration to shared base behavior, exposes goal-specific execution modes for guided processing and actions, and connects to Ghostwriter processors and Maven project-layout analysis. Supporting tools provide class discovery and project introspection so generated or updated content can be informed by the actual codebase.

![Project structure overview](./images/project-structure/c4-diagram.png)

## Key Features

- Maven-native access to Ghostwriter guided file processing through plugin goals such as `gw:gw` and `gw:act`.
- Processing for all project file types, including source code, documentation, Maven site content, tests, configuration, and diagrams.
- Reverse reactor processing order for guided scans, allowing submodules to be updated before parent modules.
- Support for execution with or without a `pom.xml` for flexible use in Maven projects or plain directories.
- Interactive and predefined action workflows for targeted updates, review tasks, and repeatable automation.
- Maven reactor awareness, non-recursive execution detection, and support for Maven parallelism.
- Optional class-introspection tools that scan Maven project classes and make code structure available to Ghostwriter workflows.
- Provider credentials and custom AI settings resolved from Maven `settings.xml` server entries.
- Configurable scan paths, model selection, instructions, exclusions, and custom action locations.
- Usage statistics initialization and logging around processing execution.

## Getting Started

### Prerequisites

- Apache Maven installed and available on the command line.
- A Maven project, multi-module reactor, or directory containing files to process.
- Network and credentials for the configured AI provider, when the selected model requires them.
- Optional Maven `settings.xml` server entry for AI provider username, password, and provider-specific configuration.
- Files containing Ghostwriter guidance comments or an action prompt that describes the requested update.

### Java Version

The plugin build configuration defines `<maven.compiler.release>8</maven.compiler.release>`, so the project is compiled for Java 8 bytecode compatibility. Actual runtime requirements can depend on the Maven version, Ghostwriter dependencies, AI provider SDKs, and the environment used to execute the plugin; verify those requirements for your target setup. The project site links Java 11 API documentation for generated Javadocs, but the explicit compiler release in this module is Java 8.

### Basic Usage

Run guided processing for the current Maven project or reactor:

```bash
mvn gw:gw
```

Process a specific path with an explicit model:

```bash
mvn gw:gw -Dgw.path=src/site -Dgw.model=provider/model-name
```

Run an action workflow:

```bash
mvn gw:act -Dgw.act="Update documentation to match the current implementation" -Dgw.path=src/site
```

### Typical Workflow

1. Add or review guidance comments in the files that should drive generation or updates.
2. Configure AI provider access through environment settings or a Maven `settings.xml` server entry.
3. Select the target scope with `-Dgw.path` when you do not want to scan the full execution root.
4. Run `mvn gw:gw` for guidance-driven processing, or `mvn gw:act` for a targeted action prompt.
5. Review the changed source, documentation, site, configuration, or diagram files.
6. Run normal project verification such as tests, documentation generation, and site generation.
7. Commit the reviewed updates together with the guidance that explains why they were produced.

## Configuration

| Parameter | Description | Default value |
| --- | --- | --- |
| `gw.model` | Provider/model identifier used by Ghostwriter processing. | Processor or provider default |
| `gw.path` | Scan root or path pattern to process. | Maven execution root for `gw:gw`; module base directory fallback for actions |
| `gw.instructions` | Additional instruction text or instruction locations consumed by the workflow. | Not set |
| `gw.excludes` | Paths or patterns excluded from scanning. | Not set |
| `ai.serverId` | Maven `settings.xml` server id used to resolve AI provider username, password, and custom XML configuration. | Not set |
| `gw.act` | Action prompt used by `gw:act`; if omitted, interactive prompting is used when available. | Prompted interactively |
| `gw.acts` | Optional directory containing predefined action definitions. | Built-in/default action lookup |
| `gw.interactive` | Enables or disables interactive behavior for action processing when supported by configuration. | Processor default |
| Maven `-T` | Maven parallel execution option; the plugin forwards Maven’s degree of concurrency to the processor. | Maven default single-threaded execution |

AI provider credentials can be supplied through Maven settings by creating a server entry and passing its id:

```bash
mvn gw:gw -Dai.serverId=my-ai-provider
```

Enable debug or trace logging for a specific component by setting the SLF4J Simple Logger property for that fully qualified class name:

```bash
mvn gw:gw -Dorg.slf4j.simpleLogger.log.org.machanism.machai.gw.maven.GWMojo=DEBUG
```

Use `TRACE`, `DEBUG`, `INFO`, `WARN`, or `ERROR` as the logging level supported by your logger configuration.

## Resources

- [Machai Ghostwriter](https://machai.machanism.org/ghostwriter/index.html)
- [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html)
- [GW Maven Plugin site](https://machai.machanism.org/gw-maven-plugin/index.html)
- [GitHub repository](https://github.com/machanism-org/machai.git)
- [Maven Central: org.machanism.machai:gw-maven-plugin](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)
- [Bindex metadata](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/gw-maven-plugin/bindex.json)
