---
canonical: https://machai.machanism.org/assembly-maven-plugin/index.html
---

# Assembly Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/assembly-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin)

## Introduction

Assembly Maven Plugin is a Maven plugin in the Machai ecosystem that assembles and updates projects using an AI-assisted, metadata-driven workflow.

At its core, the `assembly` goal runs an automated workflow against a target project directory (`${basedir}`): it reads a natural-language project prompt (from a file such as `project.txt` or via interactive input), recommends relevant libraries using Bindex metadata, and then applies the resulting changes to the project.

This approach builds on the same conceptual foundation as [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): a structured, repeatable process that uses guidance plus automation to produce consistent project updates.

## Overview

The plugin helps you go from “what I want to build” to an updated project structure by:

- Accepting a natural-language prompt describing the desired project outcome.
- Using Machai’s Bindex metadata and AI-assisted selection to recommend candidate libraries.
- Applying an assembly phase that modifies the project directory to match the intended design.

This reduces manual dependency discovery and repetitive setup work, and enables a more consistent, prompt-driven workflow for project creation and evolution.

## Key Features

- Maven goal `assembly` for prompt-driven project assembly.
- Prompt acquisition from a file (default `project.txt`) or interactive entry.
- Bindex-powered library recommendation (via a picker) with configurable score threshold.
- Optional registration/lookup endpoint for Bindex metadata discovery.
- Applies changes directly to the Maven execution base directory (`${basedir}`).
- Designed to support provider identifiers resolved by Machai’s GenAI provider manager and standard function tools.

## Getting Started

### Prerequisites

- Maven (to run the plugin).
- Network access to the configured GenAI provider(s) referenced by `assembly.genai`.
- A Machai/Bindex configuration file available to the run (the plugin uses `bindex.properties`).
- (Optional) Access to a Bindex registration/lookup endpoint if you set `bindex.register.url`.
- If you plan to type prompts interactively, a terminal environment that supports Maven interactive prompting.

### Java Version

- **Build/runtime baseline defined in `pom.xml`:** Java **8** (`maven.compiler.release=8`).
- **Functional requirements may differ:** depending on the GenAI providers and transitive dependencies used at runtime, you may need a newer Java version even if the plugin is compiled for Java 8.

### Basic Usage

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly
```

### Typical Workflow

1. Create a prompt file in the project directory (default: `project.txt`) describing what you want assembled.
2. Run the `assembly` goal.
3. The plugin reads the prompt (or asks you interactively if the file is missing).
4. The picker recommends candidate libraries (Bindex entries), filtered by the configured score threshold.
5. The assembly phase applies changes to the project directory based on the prompt and the selected recommendations.

## Configuration

Common parameters for the `assembly` goal:

| Parameter | Description | Default |
|---|---|---|
| `assembly.genai` | GenAI provider identifier used for the assembly workflow (resolved by Machai provider management). | (Required; no default in the parameter annotation) |
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
