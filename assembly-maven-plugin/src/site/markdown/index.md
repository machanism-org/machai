---
canonical: https://machai.machanism.org/assembly-maven-plugin/index.html
---

# Assembly Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/assembly-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin)

## Introduction

Assembly Maven Plugin is a Machai Maven plugin that implements the `assembly` goal. It runs an AI-assisted, metadata-driven workflow against the Maven execution base directory (`${basedir}`) to help assemble or update a target project directory in-place.

Conceptually, it follows the same structured pattern as [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): acquire guidance (a prompt), use metadata to inform decisions (Bindex), and apply repeatable automation (project edits).

When executed, the goal:

- Acquires a natural-language prompt by reading `assembly.prompt.file` (default: `project.txt`) when present, or by prompting interactively via the Maven console.
- Creates a `bindex.properties`-backed configuration (`PropertiesConfigurator`).
- Uses a GenAI-backed picker (`pick.genai`) to recommend candidate libraries expressed as Bindex entries (`Picker`; optionally using `bindex.register.url`).
- Filters recommendations by a configurable score threshold (`assembly.score`).
- Runs the assembly phase (`assembly.genai`) to apply changes to the project directory (`${basedir}`) using `ApplicationAssembly`, the prompt, and the selected libraries.

## Overview

The plugin translates “what I want to build” (your prompt) into actionable project updates by combining:

- Prompt acquisition (file-based or interactive).
- Semantic library recommendation using Bindex metadata.
- A deterministic execution flow that logs recommendations and applies edits directly to the current project directory (`${basedir}`).

This reduces manual dependency discovery and boilerplate setup, and supports a repeatable, prompt-driven workflow for creating and evolving projects.

## Key Features

- Maven goal `assembly` for prompt-driven project assembly.
- Prompt acquisition from a file (default `project.txt`) or interactive console entry.
- Uses `bindex.properties` at runtime for Machai/Bindex configuration.
- Library recommendation via Bindex entries using a configurable picker GenAI provider.
- Configurable score threshold for recommended libraries.
- Optional `bindex.register.url` for Bindex metadata discovery/registration during picking.
- Applies changes directly to the Maven execution base directory (`${basedir}`).
- Supports distinct GenAI provider identifiers for picking and assembly phases.

## Getting Started

### Prerequisites

- Maven (to execute the plugin goal).
- A terminal/console that supports interactive prompting (only required if the prompt file is absent).
- Network access and credentials for the configured GenAI provider(s).
- A Machai/Bindex configuration file available at runtime (`bindex.properties`).
- (Optional) Access to a Bindex registration/lookup endpoint if `bindex.register.url` is configured.

### Java Version

- **Build/runtime baseline defined in `pom.xml`:** Java **8** (`maven.compiler.release=8`).
- **Functional requirements may differ:** depending on the configured GenAI providers and runtime dependencies, you may need a newer Java version even if the plugin is compiled for Java 8.

### Basic Usage

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly
```

### Typical Workflow

1. Create a prompt file in the target project directory (default: `project.txt`) describing what you want assembled.
2. Run the `assembly` goal.
3. The plugin reads the prompt file (or requests the prompt interactively if the file is missing).
4. The picker recommends candidate libraries (Bindex entries), filtered by the configured score threshold.
5. The assembly phase applies changes to the project directory (`${basedir}`) using the prompt and recommended libraries.

## Configuration

Common parameters for the `assembly` goal:

| Parameter | Description | Default |
|---|---|---|
| `assembly.genai` | GenAI provider identifier used for the assembly phase. | `OpenAI:gpt-5` |
| `pick.genai` | GenAI provider identifier used for the library recommendation (picker) phase. | `OpenAI:gpt-5-mini` |
| `assembly.prompt.file` | Prompt file path. If it exists, it will be read as the prompt; otherwise the prompt is requested interactively. | `project.txt` |
| `assembly.score` | Minimum score required for a recommended library to be listed/used. | `0.9` |
| `bindex.register.url` | Optional registration/lookup endpoint used by the picker for metadata lookups/registration. | (none) |

## Resources

- Guided File Processing (conceptual foundation): https://www.machanism.org/guided-file-processing/index.html
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin
- Source repository (SCM): https://github.com/machanism-org/machai.git

<!-- @guidance:
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
   - Analyze `/src/main/java/org/machanism/machai/assembly/maven/Assembly.java` to inform the description.
   - Reference [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html) as the conceptual foundation for Machai Ghostwriter.
3. Overview
   - Clearly explain the main functions and value proposition of the plugin.
   - Summarize how the plugin enhances project workflows and documentation.
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
