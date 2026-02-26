---
canonical: https://machai.machanism.org/assembly-maven-plugin/index.html
---

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

# Assembly Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/assembly-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin)

## Introduction

Assembly Maven Plugin is a Maven plugin in the Machai ecosystem that automates project assembly using AI-assisted, metadata-driven workflows. It runs an interactive (or file-based) natural-language prompt against a target project directory, recommends candidate libraries from Machai’s **bindex** metadata registry, and then applies the selected changes to the project.

Conceptually, it fits within Machanism’s approach to guided automation and structured change application described by [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html), where a workflow operates over files and project structure in a controlled, repeatable way.

## Overview

The plugin provides a single goal, `assembly`, that orchestrates an end-to-end flow:

1. Acquire an assembly prompt from a file (default `project.txt`) or interactively from the terminal.
2. Use an AI-powered **picker** to search bindex metadata and recommend libraries that match the prompt.
3. Filter recommendations by a configurable score threshold.
4. Run an application assembly step that applies changes to the project directory based on the prompt and recommended libraries.

This improves workflows by:

- Turning requirements into a repeatable assembly process.
- Reducing manual dependency discovery/selection via semantic search over bindex metadata.
- Applying consistent, tool-driven changes to a project’s filesystem.

## Key Features

- Maven goal: `assembly`
- Prompt acquisition from file (`project.txt`) or interactive terminal input
- AI-driven library recommendation via bindex metadata (`Picker`)
- Score-based filtering for recommendations
- Applies assembled changes directly to the Maven execution base directory (`${basedir}`)
- Separate AI provider/model selection for:
  - recommendation/picking (`pick.genai`)
  - assembly/application (`assembly.genai`)
- Optional bindex registration/lookup endpoint support (`bindex.register.url`)

## Getting Started

### Prerequisites

- Java (see version notes below)
- Maven (to run the plugin)
- Network access to your chosen GenAI provider (as configured by Machai)
- Credentials/configuration for the selected GenAI provider(s) available to the runtime environment (for example via environment variables, local configuration, or the provider’s standard authentication mechanism)
- Optional: access to a bindex registration/lookup service if using `bindex.register.url`
- A `bindex.properties` file available to the execution context (used by the plugin at runtime)

### Java Version

- **Build/declared version (from `pom.xml`):** compiled with `maven.compiler.release=8` (Java 8).
- **Functional/runtime considerations:** the plugin delegates to external services and Machai components; depending on your provider SDKs and your Maven runtime, you may need a newer Java at runtime. If in doubt, start with Java 8 for compatibility with the build target, then move to a newer LTS if your environment or dependencies require it.

### Basic Usage

Run the `assembly` goal and provide the prompt via `project.txt` (default) or interactively:

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly
```

To supply explicit parameters:

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly \
  -Dassembly.genai=OpenAI:gpt-5 \
  -Dpick.genai=OpenAI:gpt-5-mini \
  -Dassembly.prompt.file=project.txt \
  -Dassembly.score=0.9
```

### Typical Workflow

1. Create or edit the prompt file (default `project.txt`) describing what you want assembled (requirements, features, libraries, constraints).
2. Run the Maven goal `assembly`.
3. The plugin uses the picker model (`pick.genai`) to recommend libraries (bindex entries) relevant to your prompt.
4. Recommendations below the score threshold (`assembly.score`) are filtered out.
5. The assembly model (`assembly.genai`) applies changes to the project directory (`${basedir}`) using the prompt and the recommended bindex libraries.
6. Review the modified project and commit changes as appropriate.

## Configuration

Common parameters for the `assembly` goal:

| Parameter | Description | Default |
|---|---|---|
| `assembly.genai` | GenAI provider/model identifier used for applying the assembly workflow. | `OpenAI:gpt-5` |
| `pick.genai` | GenAI provider/model identifier used for recommending libraries (picker phase). | `OpenAI:gpt-5-mini` |
| `assembly.prompt.file` | Prompt file path. If the file exists it is read; otherwise the prompt is requested interactively. | `project.txt` |
| `assembly.score` | Minimum score threshold for recommended libraries. | `0.9` |
| `bindex.register.url` | Optional registration/lookup endpoint used by the picker for metadata lookups/registration. | (none) |

## Resources

- Machai documentation: https://machai.machanism.org/
- Guided File Processing (concept): https://www.machanism.org/guided-file-processing/index.html
- GitHub repository (SCM): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin
