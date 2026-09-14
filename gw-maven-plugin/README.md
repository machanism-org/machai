<!-- @guidance: >>> ${guidances}/readme-content.md -->

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/gw-maven-plugin/refs/heads/main/bindex.json)

GW Maven Plugin is the Maven adapter for [Machai Ghostwriter](https://machai.machanism.org/ghostwriter/index.html). It brings guided, AI-assisted processing to Maven projects, helping teams analyze and maintain source code, tests, documentation, site content, configuration, and other relevant project files.

## Introduction

The plugin follows the [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) approach: guidance comments embedded in project files describe repeatable changes, and Ghostwriter processes selected content using those instructions. The `gw:gw` goal performs guidance processing, while `gw:act` runs a predefined act or a direct prompt.

Maven provides project, session, reactor, settings, dependency, and interactive-input context. The plugin uses that context to configure Ghostwriter, resolve provider credentials and options, select files for processing, and expose Java class-discovery and metadata tools. This lets AI-assisted workflows use Maven-aware project information while keeping credentials in Maven settings when appropriate.

For act processing, pass a predefined act name directly, append prompt text to an act name, or begin a prompt-only act value with `>`:

```bash
mvn gw:act -Dgw.act=review
mvn gw:act '-Dgw.act=review Improve the API documentation'
mvn gw:act '-Dgw.act=>Add missing Javadocs to public classes'
```

## Overview

The architecture separates Maven integration from Ghostwriter processing. Shared goal support resolves common parameters, provider configuration, scan options, usage logging, and Java metadata. Guidance and act goals then configure the appropriate processor, which identifies the effective project layout, processes selected content, and can request assistance from an external AI provider.

Project-wide goals coordinate processing across a project hierarchy and can pass Maven parallel-build concurrency to Ghostwriter. Reactor-oriented goals run module by module in Maven's scheduling order, allowing Maven to retain responsibility for module sequencing. In either mode, processing can cover source, test, documentation, site, configuration, and other relevant project files.

### Project Structure

The plugin is the Maven-facing boundary for Ghostwriter processing. Maven invokes project-wide guidance and act goals or their per-module counterparts. Shared goal support resolves project, session, reactor, settings, dependency, scan, provider-configuration, and interactive-input context; the guidance and act implementations then configure the corresponding processor. Maven settings can provide provider credentials, and an interactive Maven prompt can supply act input when needed. The aggregator guidance and act goals can coordinate traversal across a project hierarchy, while per-module goals let Maven retain dependency-driven module scheduling.

Ghostwriter processors determine the effective project layout, combine configuration sources, scan and update selected project content, request AI processing, and record usage information. Java metadata support builds project-aware class indexes and exposes class discovery and metadata from project sources and resolved dependencies to AI workflows. Project-wide goals allow the processor to coordinate work across a hierarchy and can use Maven's parallel-build concurrency. Per-module goals run in Maven's dependency-driven reactor order, keeping module scheduling under Maven's control while constraining work to the active module. The component design separates Maven mojos, configuration and usage support, class-introspection tools, Ghostwriter processors, project-layout detection, provider access, and the project files being processed.

## Key Features

- **Guidance-driven processing:** finds embedded guidance comments and applies their requested updates.
- **Act execution:** runs reusable acts or direct prompts against selected project content.
- **Project-wide and per-module goals:** supports both hierarchy-wide coordination and Maven reactor execution.
- **Maven-aware configuration:** combines Maven properties, settings-server credentials, and Ghostwriter configuration.
- **Selective scanning:** supports paths, patterns, additional instructions, and exclusions.
- **Java metadata tools:** makes class discovery and class metadata available to AI workflows.
- **Parallel processing support:** project-wide goals can use Maven's parallel execution settings.

## Getting Started

### Prerequisites

- Maven and a JDK available on `PATH`.
- Network access to download Maven dependencies and reach the selected AI provider.
- A compatible Ghostwriter provider/model configuration. Store provider credentials in Maven `settings.xml` where possible.
- A project path containing the files to process; guidance mode also requires embedded guidance comments that describe the intended changes.

The module compiles with Java 8 (`maven.compiler.release` is `8`). The runtime requirement can be higher because Maven, Ghostwriter libraries, the selected AI provider, and transitive dependencies must support the JDK used to run Maven.

### Basic Usage

Run the plugin by its Maven coordinate, replacing `VERSION` with the version to use:

```bash
mvn org.machanism.machai:gw-maven-plugin:VERSION:gw
mvn org.machanism.machai:gw-maven-plugin:VERSION:act -Dgw.act=review
```

When the plugin is configured in the build, use the short goal names:

```bash
mvn gw:gw -Dgw.path=src -Dgw.excludes=target,node_modules
mvn gw:act -Dgw.act='>Update the project documentation'
```

For multi-module work, project-wide goals can use Maven parallel execution when appropriate:

```bash
mvn -T 4 gw:gw
mvn -T 4 gw:act
```

Use `gw:gw-per-module` or `gw:act-per-module` when Maven should process modules individually in reactor order.

### Typical Workflow

1. Invoke the plugin by its Maven coordinate or configure it in the build.
2. Configure the selected model and provider credentials, preferably with a Maven `settings.xml` server entry.
3. Choose a scan path and exclusions, and add guidance comments for repeatable guidance processing or select an act for an explicit task.
4. Run a project-wide or per-module goal, review the generated changes, then build and test the project.

## Configuration

Common command-line properties include:

| Property | Purpose | Default value |
|---|---|---|
| `gw.model` | Selects the provider/model passed to Ghostwriter. | Provider or Ghostwriter default. |
| `gw.path` | Selects a file, directory, glob, or supported pattern to scan. | Project execution root for `gw:gw`; module base directory for per-module goals. |
| `gw.instructions` | Supplies additional instructions or an instruction-file location. | Unset. |
| `gw.excludes` | Specifies comma-separated paths or patterns to skip. | Unset. |
| `genai.serverId` | Selects a Maven `settings.xml` server containing provider credentials and configuration. | Unset; Ghostwriter configuration is used. |
| `gw.config` | Specifies a Ghostwriter properties file when no server ID is used. | Unset; Ghostwriter's default configuration location is attempted. |
| `gw.act` | Supplies a predefined act, an act plus additional prompt text, or a prompt-only value beginning with `>`. | Unset; interactive input may be requested. |
| `gw.acts` | Selects a directory or URL containing act definitions. | Act processor default location. |
| `gw.interactive` | Enables or disables interactive prompting when act configuration is incomplete. | Processor/configuration default. |

Store provider credentials in Maven `settings.xml` where possible. Enable targeted debug logging with Maven SimpleLogger by replacing the class name and level as needed:

```bash
mvn -Dorg.slf4j.simpleLogger.log.org.machanism.machai.gw.maven=DEBUG gw:gw
```

The general form is `-Dorg.slf4j.simpleLogger.log.[fully-qualified-class-name]=[LEVEL]`.

## Resources

- [Machai Ghostwriter](https://machai.machanism.org/ghostwriter/index.html)
- [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html)
- [Machai documentation](https://machai.machanism.org/)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [GW Maven Plugin on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)
- [Maven plugin configuration guide](https://maven.apache.org/guides/mini/guide-configuring-plugins.html)
