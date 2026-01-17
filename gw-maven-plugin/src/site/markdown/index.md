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

GW Maven Plugin (Ghostwriter Maven Plugin) is a documentation automation plugin for Maven-based Java projects. It scans your codebase for embedded `@guidance:` directives and uses them to generate and update Maven Site documentation, keeping your docs consistent, current, and aligned with the source of truth in the repository.

Benefits:

- Reduces documentation drift by generating pages from in-repo guidance
- Encourages consistent structure and best practices across modules
- Makes documentation updates repeatable as part of the Maven build

## Overview

The plugin discovers `@guidance:` comments in your project sources/resources and synthesizes them into Markdown pages under the Maven Site structure. You can run it on demand during development or bind it into the Maven lifecycle (for example, `site`) so documentation is refreshed whenever you build the site.

## Key Features

- Generates and refreshes Maven Site pages from embedded `@guidance:` comments
- Scans project sources and resources to discover `@guidance:` directives
- Integrates with standard Maven Site layout and workflows
- Supports documentation that stays aligned with code, tests, and evolving requirements
- Can be run from the command line or bound into the Maven lifecycle

## Getting Started

### Prerequisites

- Java 8+
- Maven 3+

### Environment Variables

No environment variables are required by default.

| Name | Required | Description | Default |
| --- | --- | --- | --- |
| N/A | No | The plugin does not require environment variables for basic operation. | N/A |

### Basic Usage

Add the plugin to your `pom.xml`:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>0.0.2</version>
</plugin>
```

Run the plugin goal:

```sh
mvn gw:gw
```

### Typical Workflow

1. Add `@guidance:` comments close to the code or artifacts they describe.
2. Run `mvn gw:gw` to (re)generate/update the Maven Site Markdown pages.
3. Run `mvn site` to render the site and review the generated documentation.
4. Iterate: update code and `@guidance:` comments as requirements evolve, then re-run the goals.

## Configuration

Common configuration parameters are set under the plugin `<configuration>` block in your `pom.xml`.

| Parameter | Description | Default |
| --- | --- | --- |
| `genai` | Selects the GenAI backend/provider configuration used by Ghostwriter. | (implementation-defined) |

Example configuration:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>0.0.2</version>
  <configuration>
    <genai>Web:CodeMie</genai>
  </configuration>
</plugin>
```

Example command line with custom parameters:

```sh
mvn gw:gw -Dgw.genai=Web:CodeMie
```

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- Shields.io badge: https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg
