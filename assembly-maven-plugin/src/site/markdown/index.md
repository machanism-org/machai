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

`assembly-maven-plugin` runs MachAI’s project **assembly** workflow from Maven. It takes a natural-language prompt describing what you want to build, recommends relevant libraries via MachAI **bindex** metadata, and then applies the selected set to a target project directory.

At the center of the plugin is the `assembly` Mojo (`org.machanism.machai.assembly.maven.Assembly`). It can be run **outside** a standard Maven project (`requiresProject=false`), reads the assembly prompt from a file (default `project.txt`) or interactively prompts you, and then orchestrates:

1. **Library recommendation** using `Picker` (GenAI-powered semantic search over bindex metadata).
2. **Project updates** using `ApplicationAssembly`, which applies changes to the configured project directory.

Conceptually, this aligns with Machanism’s [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): the plugin acts as a repeatable, metadata-driven workflow that produces consistent project changes from high-level intent.

## Overview

The plugin’s value proposition is to turn “what I want to build” into a concrete, reproducible project setup:

- It captures intent as a prompt (file-based or interactive).
- It discovers and ranks candidate libraries using bindex entries.
- It applies the assembled result to a project directory, enabling fast bootstrapping and standardized assembly across projects.

## Key Features

- Maven goal `assembly` that can run with or without a Maven project.
- Prompt input via `project.txt` (or a configurable prompt file), with interactive fallback.
- GenAI-backed library recommendations through MachAI `Picker`.
- Minimum recommendation score threshold control.
- Optional bindex registration/lookup URL support.
- Applies changes directly to the configured base directory.
- Separate model selection for recommendation (`pickModel`) vs assembly (`assemblyModel`).

## Getting Started

### Prerequisites

- **Java:** see version notes below.
- **Maven:** a Maven installation suitable for running plugins.
- **GenAI provider configuration:** the MachAI provider referenced by the configured model identifiers must be available (credentials/environment depend on the provider you choose).
- **bindex configuration:** a `bindex.properties` file available to the execution (used by the workflow configuration).
- (Optional) Network access to any configured bindex registration/lookup service.

### Java Version

- **Build-level requirement (from `pom.xml`):** compiled with `maven.compiler.release=8` (Java 8).
- **Functional/runtime note:** depending on the selected GenAI provider integrations and your execution environment, you may choose to run on a newer Java runtime; however, the plugin is intended to be compatible with Java 8 as declared.

### Basic Usage

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly
```

To provide a prompt via file, create `project.txt` in the directory you are running from (or set `-Dassembly.prompt.file=...`).

### Typical Workflow

1. Create or choose a target directory (the Maven `${basedir}` where changes will be applied).
2. Write your assembly prompt in `project.txt` (or plan to answer the interactive prompt).
3. Ensure `bindex.properties` is present and configured for your environment.
4. Run the `assembly` goal.
5. Review the “Recommended libraries” output (bindex IDs and scores).
6. The plugin applies the assembly workflow to the project directory.

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `assembly.model` | GenAI provider/model identifier for the assembly workflow (`ApplicationAssembly.MODEL_PROP_NAME`). | `ApplicationAssembly.DEFAULT_MODEL` |
| `picker.model` | GenAI provider/model identifier for library recommendations (`Picker.MODEL_PROP_NAME`). | `Picker.DEFAULT_MODEL` |
| `assembly.prompt.file` | Prompt file to read as plain text; if missing, the plugin prompts interactively. | `project.txt` |
| `assembly.score` | Minimum score threshold used by the picker when recommending libraries. | `ApplicationAssembly.DEFAULT_SCORE_VALUE` |
| `bindex.register.url` | Optional registration/lookup URL used by the picker for metadata operations. | _(none)_ |

## Resources

- MachAI documentation: https://machai.machanism.org/
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- GitHub (monorepo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin
