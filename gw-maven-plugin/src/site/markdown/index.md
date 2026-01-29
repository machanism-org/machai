<!-- @guidance:
Page Structure: 
# Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge ([![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
# Introduction
   - Full description of purpose and benefits.
# Overview
   - Explanation of the project function and value proposition.
# Key Features
   - Bulleted list highlighting the primary capabilities of the project.
# Getting Started
   - Prerequisites: List of required software and services.
   - Environment Variables: Table describing necessary environment variables.
   - Basic Usage: Example command to run the plugin.
   - Typical Workflow: Step-by-step outline of how to use the project artifacts.
# Configuration
   - Table of common configuration parameters, their descriptions, and default values.
   - Example: Command-line example showing how to configure and run the plugin with custom parameters.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

## Introduction

GW Maven Plugin (Ghostwriter Maven Plugin) is a documentation automation plugin for Maven-based Java projects. It scans your repository for embedded `@guidance:` directives and uses them to generate and update Maven Site documentation, helping keep docs consistent, current, and aligned with the in-repo source of truth.

Benefits:

- Reduces documentation drift by generating pages from in-repo guidance
- Encourages consistent structure and best practices across modules
- Makes documentation updates repeatable as part of the Maven build

## Overview

The Ghostwriter Maven Plugin automates Maven Site documentation by scanning the repository for embedded `@guidance:` directives and using them to assemble and update Markdown pages. This keeps documentation consistent across modules and aligned with what’s in the codebase.

## Key Features

- Generates and refreshes Maven Site pages from embedded `@guidance:` comments
- Scans project sources and resources to discover `@guidance:` directives
- Integrates with standard Maven Site layout and workflows
- Supports documentation that stays aligned with code, tests, and evolving requirements
- Can be run from the command line or bound into the Maven lifecycle

## Getting Started

### Prerequisites

- Java 11+
- Maven 3+

### Environment Variables

| Name | Required | Description | Default |
| --- | --- | --- | --- |
| N/A | No | The plugin does not require environment variables for basic operation. | N/A |

### Basic Usage

Add the plugin to your `pom.xml`:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>REPLACE_WITH_LATEST_VERSION</version>
</plugin>
```

Run the plugin goal:

```sh
mvn gw:gw
```

### Typical Workflow

1. Add `@guidance:` comments close to the code or artifacts they describe.
2. Run `mvn gw:gw` to (re)generate or update the Maven Site Markdown pages.
3. Run `mvn site` to render the site and review the generated documentation.
4. Iterate: update code and `@guidance:` comments as requirements evolve, then re-run the goals.

## Configuration

Common configuration parameters are set under the plugin `<configuration>` block in your `pom.xml`.

| Parameter | Description | Default |
| --- | --- | --- |
| `genai` | Selects the GenAI backend/provider configuration used by Ghostwriter. | (implementation-defined) |
| `serverId` | Maven `settings.xml` server id used to resolve provider credentials/config. | (implementation-defined) |

Example configuration:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>REPLACE_WITH_LATEST_VERSION</version>
  <configuration>
    <genai>CodeMie:gpt-5-2-2025-12-11</genai>
    <serverId>CodeMie</serverId>
  </configuration>
</plugin>
```

Example command line with custom parameters:

```sh
mvn gw:gw -Dgw.genai=CodeMie:gpt-5-2-2025-12-11 -Dgw.serverId=CodeMie
```

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- GitHub: https://github.com/machanism-org/machai
- Maven Plugin Development: https://maven.apache.org/guides/plugin/guide-java-plugin-development.html
- Maven Site Plugin: https://maven.apache.org/plugins/maven-site-plugin/
