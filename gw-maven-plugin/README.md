<!-- @guidance: >>> ${guidances}/readme-content.md -->

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/gw-maven-plugin/refs/heads/main/bindex.json)

The **GW Maven Plugin** is the primary Maven adapter for the [Machai Ghostwriter application](https://machai.machanism.org/ghostwriter/index.html). It integrates guided, AI-assisted processing into Maven projects so teams can analyze and maintain source code, tests, documentation, site content, configuration, and other relevant project files.

## Introduction

The plugin follows the [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) approach: guidance comments embedded in project files describe repeatable changes, and Ghostwriter processes selected content using those instructions. The `gw:gw` goal performs guidance processing, while `gw:act` runs a predefined act or a direct prompt.

Maven provides project, session, reactor, settings, dependency, and interactive-input context. The plugin uses that context to configure Ghostwriter, resolve provider credentials and options, select files for processing, and expose Java class-discovery and metadata tools. Credentials can remain in Maven settings while the AI workflow receives Maven-aware project information.

Act input accepts a predefined act name, an act name followed by additional prompt text, or a prompt-only value beginning with `>`:

```bash
mvn gw:act -Dgw.act=review
mvn gw:act '-Dgw.act=review Improve the API documentation'
mvn gw:act '-Dgw.act=>Add missing Javadocs to public classes'
```

## Overview

The plugin is a Maven-facing orchestration layer. Shared goal support resolves project, session, reactor, settings, dependency, scan, provider-configuration, and interactive-input context. Guidance and act goals then configure the appropriate Ghostwriter processor, which detects the effective project layout, processes selected content, requests assistance from an external AI provider, and records usage information.

It provides four execution modes:

- **Project-wide guidance processing** scans a project hierarchy for embedded guidance comments and coordinates changes across modules.
- **Per-module guidance processing** processes each module independently in Maven's dependency-driven reactor order.
- **Project-wide act processing** applies a named act or direct prompt across the project hierarchy.
- **Per-module act processing** applies the act separately to each module while Maven retains responsibility for scheduling.

Project-wide goals can pass Maven's parallel-build concurrency to Ghostwriter. Java metadata support builds project-aware class indexes and exposes class discovery and metadata from project sources and resolved dependencies to AI workflows. The processors can work with source, test, documentation, site, configuration, act-definition, and other relevant files selected by the scan settings.

### Project Structure

The component architecture places the plugin between Apache Maven and the Ghostwriter processor libraries. Maven invokes aggregator or per-module goals and supplies project, reactor, settings, dependency, concurrency, and interactive-input context. Shared Maven goal support combines those values with provider settings and scan options, while the guidance and act implementations configure their respective processors.

Ghostwriter processors detect the effective project layout, combine configuration sources, scan and update selected project content, request AI processing from the configured provider, and record usage statistics. Java class tools provide project and dependency metadata to the processors. Maven settings provide credentials and provider configuration, and the Maven prompter supplies act input when interactive configuration is enabled.

## Key Features

- **Guidance-driven processing:** finds embedded `@guidance` comments and applies their requested updates.
- **Act execution:** runs reusable acts or direct prompts against selected project content.
- **Project-wide and per-module goals:** supports hierarchy-wide coordination and Maven reactor execution.
- **Maven-aware configuration:** combines Maven properties, settings-server credentials, configuration files, and Ghostwriter defaults.
- **Selective scanning:** supports files, directories, patterns, additional instructions, and exclusions.
- **Java metadata tools:** makes class discovery and class metadata available to AI workflows.
- **Interactive input:** supports act prompts and multi-line input when configuration requires human clarification.
- **Parallel processing support:** project-wide goals can use Maven's parallel execution settings.
- **Diagnostics and usage tracking:** supports component-level SLF4J logging and usage statistics.

## Getting Started

### Prerequisites

- Maven and a JDK available on `PATH`.
- Network access to download Maven dependencies and reach the selected AI provider.
- A compatible Ghostwriter provider/model configuration. Store provider credentials in Maven `settings.xml` where possible.
- A Maven project for per-module goals and for the current guidance execution path; the `gw:act` aggregator can also process a directory without a `pom.xml`.
- A project path containing the files to process; guidance mode also requires embedded guidance comments describing the intended changes.

This module compiles with Java 8 (`maven.compiler.release` is `8`). The runtime requirement can be higher because Maven, Ghostwriter libraries, the selected AI provider, and transitive dependencies must support the JDK used to run Maven.

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
3. Choose a scan path and exclusions, and add guidance comments for repeatable processing or select an act for an explicit task.
4. Run a project-wide or per-module goal and review the generated changes.
5. Build and test the project before committing the resulting documentation or code changes.

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
| `gw.threads` | Controls processor worker threads when an aggregator coordinates parallel module processing. | Maven concurrency when parallel execution is enabled; otherwise processor default. |
| `gw.nonRecursive` | Disables recursive module traversal for applicable act or per-module execution. | Derived from Maven reactor context. |

A Maven server entry can contain provider credentials and custom configuration:

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

Enable targeted debug logging with Maven SimpleLogger by replacing the class name and level as needed:

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
