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

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/gw-maven-plugin/bindex.json)

## Introduction

**GW Maven Plugin** is the Maven adapter for the [Machai Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It brings guided, AI-assisted file processing into a Maven build so that teams can analyze and maintain source code, tests, documentation, site content, configuration, and other project files from the same workflow. The plugin discovers Maven project context, reads provider configuration and credentials, exposes Java class-introspection tools, and delegates the actual work to Ghostwriter processors.

Its design follows [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): guidance comments embedded in files describe the desired change, and Ghostwriter uses those instructions while scanning and updating the selected paths. The `gw:gw` goal processes files containing guidance, while `gw:act` applies a named act or a user-supplied prompt. Both goals support project-wide and per-module execution, exclusions, extra instructions, model selection, and Maven settings integration.

The implementation supports Maven projects and no-POM directories for the aggregator goals. `gw:gw` can coordinate modules itself, including parallel execution; `gw:act` processes modules in reverse order when it is coordinating the build, with submodules handled before parent modules. Per-module variants (`gw:gw-per-module` and `gw:act-per-module`) instead participate in Maven's standard reactor and require a Maven project.

### Act prompt syntax

For a predefined act, pass its name directly. For a prompt-only act, prefix the prompt with `>` so it is interpreted as user input rather than an act name. Additional prompt text can follow an act name:

```bash
mvn gw:act -Dgw.act=review
mvn gw:act '-Dgw.act=review Improve the API documentation'
mvn gw:act '-Dgw.act=>Add missing Javadocs to public classes'
```

## Overview

The plugin is a Maven-facing orchestration layer: it resolves the effective project and module layout, loads configuration, selects the scan path, registers optional class tools, and invokes `GuidanceProcessor` or `ActProcessor`. The processors then inspect and update the requested project files through the configured AI provider. Maven settings can supply a provider server's username, password, and custom XML configuration; a local Ghostwriter properties configuration can be used when no server id is supplied.

The project structure is centered on a developer invoking Maven, which supplies project, session, reactor, and settings context to shared plugin goal implementations. Those goals configure Ghostwriter guidance or act processors. The processors discover the Maven layout, read and write project content, request AI assistance from an external provider, and record usage statistics. Java projects can additionally expose class discovery and reflective class metadata to the processor, allowing documentation work to be grounded in the compiled or project classpath. The overview is illustrated by [the C4 project-structure diagram](./images/project-structure/c4-diagram.png).

## Understanding the Plugin Architecture

The Ghostwriter Maven Plugin serves as a bridge between Maven build processes and AI-powered code processing capabilities. This plugin enables developers to leverage artificial intelligence for automated code analysis, documentation, and transformation tasks within their Maven projects.

### Core Purpose and Philosophy

At its heart, the plugin operates on two fundamental principles: **guidance-based processing** and **action-based processing**. These represent different approaches to how AI interacts with your codebase.

Guidance processing allows developers to embed special comments directly in their source files. These comments act as instructions that tell the AI what to do when it encounters them. Think of it as leaving notes for an intelligent assistant who will later read through your code and follow your directions.

Action processing, on the other hand, applies predefined or custom operations across selected files. Rather than relying on embedded instructions, actions are like recipes that the AI follows to transform or analyze your code in consistent ways.

### The Four Processing Modes

The plugin provides four distinct ways to invoke these capabilities, each designed for different scenarios:

**Project-wide guidance processing** scans an entire project hierarchy, looking for embedded guidance comments and processing them according to the instructions found. This mode understands the full context of multi-module projects and can coordinate changes across module boundaries.

![Project-wide Guidance Processing](images/project-wide-guidance-processing.png)

**Module-specific guidance processing** operates within the confines of individual Maven modules. While it still looks for guidance comments, it respects Maven's module boundaries and processes each module in isolation according to Maven's dependency order.

![Module-specific Guidance Processing](images/module-specific-guidance-processing.png)

**Project-wide action processing** applies a specified action across an entire project structure. Like its guidance counterpart, it can traverse module hierarchies and coordinate changes across the full project scope.

![Project-wide Action Processing](images/project-wide-action-processing.png)

**Module-specific action processing** constrains actions to individual modules, ensuring that each module is processed independently without affecting its siblings or children.

![Module-specific Action Processing](images/module-specific-action-processing.png)

### Configuration Philosophy

The plugin embraces flexibility in configuration, recognizing that different teams and environments have varying needs for managing credentials and settings. Configuration can come from multiple sources, with a clear precedence order that allows both centralized management and local overrides.

When using Maven's server configuration mechanism, teams can centralize their AI provider credentials and settings in Maven's settings file. This approach keeps sensitive information out of project files while still allowing project-specific customization through additional parameters.

Alternatively, configuration can be loaded from dedicated configuration files, either explicitly specified or discovered by convention. This approach suits teams who prefer file-based configuration management or need to share configurations across different tools.

The plugin always allows runtime parameters to override any loaded configuration, ensuring that developers can adjust behavior for specific invocations without modifying persistent configuration.

### Processing Flow and Intelligence

The plugin acts as an intelligent coordinator between Maven's build system and the AI processing engine. When processing begins, it first establishes the context by understanding the project structure, module relationships, and build configuration.

For multi-module projects, the plugin can operate in two distinct modes. In recursive mode, it allows the AI processor to discover and traverse module hierarchies according to its own logic. In non-recursive mode, it constrains processing to specific modules, preventing duplicate processing when Maven itself is handling module iteration.

The plugin enriches the AI processor's understanding of the project by providing Maven-specific context. This includes compiled class information, project metadata, and build artifacts. This enrichment allows the AI to make more informed decisions based on the actual build state rather than just source files.

### Execution Patterns and Parallelism

Understanding when and how processing occurs is crucial for effective use. Aggregator goals run once at the project root and coordinate processing across the entire project structure. Per-module goals run multiple times, once for each module that Maven processes.

The plugin respects Maven's parallel execution capabilities. When Maven runs with multiple threads, aggregator goals pass this concurrency information to the AI processor, allowing it to parallelize its work appropriately. Per-module goals rely on Maven's own parallel scheduling, ensuring that module dependencies are respected.

### Interactive Capabilities

The plugin recognizes that AI processing sometimes requires human input or clarification. It provides interactive modes where developers can be prompted for additional information during processing. This interaction is carefully coordinated to avoid confusion in parallel builds, ensuring that only one prompt appears at a time even when multiple threads are active.

Multi-line input is supported for complex instructions or prompts, allowing developers to provide detailed context or specifications when needed. The plugin handles the mechanics of collecting this input and passing it to the AI processor in a usable format.

### Error Handling and Resilience

The plugin takes a pragmatic approach to error handling. Configuration errors that prevent processing from starting are reported immediately and clearly. Processing errors are logged with full context before being translated into Maven's error reporting system.

The plugin distinguishes between different types of termination. Normal completion, even if early, is handled gracefully. Exceptional termination with error codes is propagated appropriately, ensuring that build pipelines can respond correctly to different failure modes.

### Integration Boundaries

The plugin maintains a clear separation of concerns. It handles all Maven-specific aspects: reading project structure, resolving dependencies, managing configuration, coordinating module processing, and translating between Maven's world and the AI processor's world.

The actual AI processing—understanding guidance comments, executing actions, generating changes, and managing the details of code transformation—remains the responsibility of the underlying processor implementation. This separation ensures that the plugin remains focused on integration while allowing the AI processing engine to evolve independently.

## Key Features

- **Guidance-driven processing:** scans selected files for `@guidance` comments and applies the requested updates.
- **Act execution:** runs reusable acts or direct prompts against project content.
- **Four Maven goals:** aggregator and per-module variants for both guidance and act workflows.
- **Whole-project coverage:** supports source, test, documentation, site, configuration, and other file types.
- **Maven-aware context:** uses project layout, execution-root, reactor, settings, and parallel-build information.
- **Provider configuration:** resolves model settings and credentials from Maven properties, a configuration file, or `settings.xml` server entries.
- **Selective scanning:** accepts files, directories, patterns, instructions, and exclusions.
- **Java introspection tools:** can find classes and retrieve class metadata, methods, fields, annotations, and source/artifact locations.
- **Parallel and reactor workflows:** supports Maven concurrency for aggregator goals and standard reactor scheduling for per-module goals.
- **Usage tracking and diagnostics:** initializes usage statistics and supports component-level SLF4J debug logging.

## Getting Started

### Prerequisites

- JDK and Maven installed and available on `PATH`.
- Network access to download Maven dependencies and reach the configured GenAI provider.
- A Ghostwriter-compatible provider/model configuration. Credentials may be stored in Maven `settings.xml` rather than on the command line.
- A Maven project for the per-module goals. The aggregator goals can also run against a directory without a `pom.xml`.
- A project path containing the files to process and, for guidance mode, guidance comments describing the intended changes.

### Java version

This module sets `<maven.compiler.release>8</maven.compiler.release>`, so its published bytecode target is Java 8. The practical runtime requirement can be higher because Maven, the Ghostwriter libraries, the selected AI provider, and their transitive dependencies must all support the JDK used to run Maven. Use a current supported JDK when provider or dependency documentation requires one, while retaining Java 8 compatibility for the plugin's own compilation target.

### Basic usage

With the plugin available through Maven coordinates, run guidance processing or an act from the project root. Replace `VERSION` with the plugin version being used:

```bash
mvn org.machanism.machai:gw-maven-plugin:VERSION:gw
mvn org.machanism.machai:gw-maven-plugin:VERSION:act -Dgw.act=review
```

When the plugin is configured in the build, the shorter goal form is available:

```bash
mvn gw:gw -Dgw.path=src -Dgw.excludes=target,node_modules
mvn gw:act -Dgw.act='>Update the project documentation'
```

For a build with several modules, use `mvn -T 4 gw:gw` or `mvn -T 4 gw:act` when parallel processing is appropriate. Use `gw:gw-per-module` or `gw:act-per-module` when each module should be handled by Maven's reactor.

### Typical workflow

1. Add the plugin to the build or invoke it by its fully qualified Maven coordinate.
2. Configure the model and provider credentials, preferably through a Maven `settings.xml` server.
3. Select a project path and exclusions; provide an instruction file or inline instructions if the default behavior needs clarification.
4. Add guidance comments to files for repeatable documentation/code maintenance, or select an act for an explicit task.
5. Run the appropriate aggregator or per-module goal and review the generated changes.
6. Build and test the project, then commit the guidance and resulting documentation together when the workflow is intended to be repeatable.

## Practical Usage Patterns

In practice, teams typically use guidance processing during active development, embedding instructions in code that help maintain consistency, generate documentation, or ensure compliance with coding standards. The AI reads these embedded instructions and acts on them, like an intelligent pair programmer following written notes.

Action processing serves different needs, such as project-wide refactoring, analysis, or transformation tasks. Teams might define standard actions for common operations like updating documentation, reviewing security patterns, or modernizing code structures. These actions can then be applied consistently across projects or modules as needed.

The choice between project-wide and module-specific processing depends on the task at hand and the project structure. Project-wide processing suits tasks that need global context or coordination. Module-specific processing works better when modules should be treated independently or when integration with Maven's build lifecycle is important.

## Configuration

The following properties are the common command-line names used by the mojos. They can also be supplied in the plugin's `<configuration>` element where Maven parameter names are used.

| Parameter | Description | Default value |
|---|---|---|
| `gw.model` (`model`) | Provider/model identifier passed to the Ghostwriter processor. | Provider or library default; unset in the mojo. |
| `gw.path` (`path`) | File, directory, glob, or supported pattern to scan. | Execution-root directory for aggregator scanning; module base directory for per-module usage. |
| `gw.instructions` (`instructions`) | Additional inline instructions or an instruction-file location. | Unset. |
| `gw.excludes` (`excludes`) | Comma-separated paths/patterns, or configured exclusion values, skipped during scanning. | Unset. |
| `genai.serverId` (`serverId`) | Maven `settings.xml` server id from which provider credentials and custom configuration are read. | Unset; the configured Ghostwriter properties file is used instead. |
| `gw.config` (`configFile`) | Optional Ghostwriter properties configuration file used when `genai.serverId` is not set. | Unset in the mojo; the module POM supplies `../gw.properties` when that project configuration is applied, otherwise Ghostwriter's default location is used. |
| `gw.act` (`act`) | Predefined act name, act plus prompt text, or a prompt-only value beginning with `>`. | Unset; interactive input may be requested. |
| `gw.acts` (`acts`) | Directory or URL containing predefined act definitions. | Act processor default location. |
| `gw.interactive` (`interactive`) | Enables or disables interactive prompting when act configuration is incomplete. | Processor/configuration default. |
| `gw.threads` | Internal processor setting for worker threads when an aggregator coordinates parallel module processing; normally use Maven's `-T` option instead. | Maven degree of concurrency when parallel execution is enabled; otherwise processor default. |
| `gw.nonRecursive` | Internal/user-property setting that can disable recursive module traversal for act or per-module execution. | Derived from the Maven reactor context. |
| `basedir` | Maven module base directory injected into the mojo. | Maven `${basedir}`. |
| `project` | Current Maven project context used for layout and classpath metadata. | Maven `${project}` when a project is present. |
| `session` | Maven session used for execution-root, reactor, and parallel-build context. | Maven `${session}`. |
| `reactorProjects` | Read-only Maven reactor project list used during multi-module processing. | Maven `${reactorProjects}`. |
| `params` | Additional key-value entries merged into the effective provider configuration. | Unset. |

A Maven server entry may contain `username`, `password`, and provider-specific child configuration values:

```xml
<server>
  <id>my-ai-provider</id>
  <username>provider-user</username>
  <password>provider-secret</password>
  <configuration>
    <AUTH_URL>https://provider.example/auth</AUTH_URL>
  </configuration>
</server>
```

Enable targeted debug logging with Maven's SimpleLogger property. For example:

```bash
mvn -Dorg.slf4j.simpleLogger.log.org.machanism.machai.gw.maven=DEBUG gw:gw
mvn -Dorg.slf4j.simpleLogger.log.org.machanism.machai.gw.processor=DEBUG gw:act -Dgw.act=review
```

Replace the package with any fully qualified class name and use an appropriate level such as `TRACE`, `DEBUG`, `INFO`, `WARN`, or `ERROR`:
`-Dorg.slf4j.simpleLogger.log.[fully-qualified-class-name]=[LEVEL]`.

## Resources

- [Machai Ghostwriter](https://machai.machanism.org/ghostwriter/index.html) — the application and processing platform.
- [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) — the conceptual foundation.
- [Machai documentation](https://machai.machanism.org/)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [GW Maven Plugin on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)
- [Maven plugin configuration guide](https://maven.apache.org/guides/mini/guide-configuring-plugins.html)
- [JDK installation](https://adoptium.net/)