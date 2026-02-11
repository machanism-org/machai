<!-- @guidance:
Page Structure:
# Header
   - **Project Title:**  
     - Automatically extract the project title from `pom.xml`.
   - **Maven Central Badge:**  
     - Display the Maven Central badge using the following Markdown:  
       `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`
     - Replace `[groupId]` and `[artifactId]` with values from `pom.xml`.
# Introduction
   - Provide a comprehensive description of the GW Maven plugin, including its purpose and benefits.
   - Analyze `/src/main/java/org/machanism/machai/maven/StandardProcess.java` and `/src/main/java/org/machanism/machai/maven/GW.java` to inform the description.
# Overview
   - Clearly explain the main functions and value proposition of the GW Maven plugin.
   - Summarize how the plugin enhances project workflows and documentation.
# Key Features
   - Present a bulleted list of the primary capabilities and unique features of the plugin.
# Getting Started
   - **Prerequisites:**  
     - List all required software, services, and environment settings needed to use the plugin.
   - **Basic Usage:**  
     - Provide an example command for running the plugin.
   - **Typical Workflow:**  
     - Outline the step-by-step process for using the plugin and its artifacts.
# Configuration
   - Include a table of common configuration parameters, with columns for parameter name, description, and default value.
   - Ensure descriptions are clear and concise.
# Resources
   - Provide a list of relevant links, including:
     - Official platform or documentation site
     - GitHub repository
     - Maven Central page
     - Any other useful resources
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, tables, code blocks, and links.
- Ensure clarity, conciseness, and easy navigation throughout the page.
- Organize content logically for optimal readability and user experience.
-->

# GW Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/gw-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin)

## Introduction

GW Maven Plugin (Ghostwriter Maven Plugin) is a documentation automation plugin for Maven-based Java projects. It scans documentation sources (typically under `src/site`) and runs them through the MachAI Generative Workflow (GW) pipeline to generate or update project documentation from embedded guidance tags and project context.

The plugin is designed to keep documentation consistent and current across a multi-module build. It can run as an aggregator goal (processing the whole reactor) or as a standard goal (processing a single module), and it supports provider/model selection, optional instruction inputs, default guidance, and selective scanning via exclude patterns.

## Overview

At its core, the plugin delegates to a `FileProcessor` that:

- Discovers documentation inputs under a configurable scan root.
- Applies optional instructions and default guidance to steer the generation workflow.
- Uses a configured GenAI provider/model identifier (for example, `OpenAI:gpt-5`) and can load credentials from Maven `settings.xml`.
- Processes modules either sequentially or in parallel (multi-threaded), depending on the goal and configuration.

This improves project workflows by reducing the manual effort required to maintain documentation, enabling repeatable documentation builds (locally or in CI), and aligning generated content with project structure and Maven metadata.

## Key Features

- Maven goals to run the MachAI GW documentation pipeline from your build.
- Scans documentation sources (commonly `src/site`) with an optional scan root override.
- Supports additional instruction inputs and default guidance to control output style/content.
- Supports exclude patterns for skipping directories/files during scanning.
- Can load GenAI credentials from Maven `settings.xml` via a `<server>` id.
- Aggregator execution for reactor builds, with optional multi-threading.
- Optional logging of the input file set passed to the workflow.

## Getting Started

### Prerequisites

- Java 11+ (project compiles with `maven.compiler.release=11`).
- Maven 3.x.
- Access to a supported GenAI provider/model (configured via `gw.genai`).
- (Optional) Maven `settings.xml` `<server>` entry if the provider requires credentials (configured via `gw.genai.serverId`).
- Documentation sources to scan (commonly `src\site` in a Maven project).

### Basic Usage

Run the aggregator goal:

```text
mvn org.machanism.machai:gw-maven-plugin:gw -Dgw.genai=OpenAI:gpt-5
```

If credentials are stored in `settings.xml`:

```text
mvn org.machanism.machai:gw-maven-plugin:gw -Dgw.genai=OpenAI:gpt-5 -Dgw.genai.serverId=genai
```

Run the standard (module) goal:

```text
mvn gw:std -Dgw.genai=OpenAI:gpt-5
```

### Typical Workflow

1. Add and/or update documentation sources under `src\site` (for example, `src\site\markdown`).
2. Add embedded guidance tags in your documentation sources (as expected by your workflow).
3. (Optional) Provide a shared instructions file and/or default guidance.
4. Configure credentials in Maven `settings.xml` and reference them via `gw.genai.serverId` if needed.
5. Run `mvn org.machanism.machai:gw-maven-plugin:gw` (reactor) or `mvn gw:std` (single module).
6. Review generated/updated documentation artifacts and commit changes.

## Configuration

Common parameters:

| Parameter | Description | Default |
|---|---|---|
| `gw.genai` (`genai`) | Provider/model identifier forwarded to the workflow. | *(none)* |
| `rootDir` | Maven module base directory. | `${basedir}` |
| `gw.scanDir` (`scanDir`) | Scan root override for discovering documentation inputs. | `${basedir}` |
| `gw.instructions` (`instructions`) | Instruction location(s) consumed by the workflow. | *(none)* |
| `gw.guidance` (`guidance`) | Default guidance text forwarded to the workflow. | *(none)* |
| `gw.excludes` (`excludes`) | Exclude patterns/paths to skip during scanning. | *(none)* |
| `gw.genai.serverId` (`serverId`) | `settings.xml` `<server>` id used to load GenAI credentials. | *(none)* |
| `gw.threads` (`threads`) | Enables/disables multi-threaded module processing (aggregator goal). | `true` |
| `gw.logInputs` (`logInputs`) | Logs the list of input files passed to the workflow. | `false` |

## Resources

- GitHub repository: https://github.com/machanism-org/machai
- Maven Central (artifact): https://central.sonatype.com/artifact/org.machanism.machai/gw-maven-plugin
- Maven Plugin Tools (Mojo annotations): https://maven.apache.org/plugin-tools/maven-plugin-annotations/
- Maven Settings Reference (`settings.xml`): https://maven.apache.org/settings.html
