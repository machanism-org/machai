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

`assembly-maven-plugin` runs MachAI’s project assembly workflow from Maven. It takes a natural-language prompt describing the project you want to assemble, recommends relevant libraries via MachAI **bindex** metadata, and then applies the selected set to a target directory.

At the center of the plugin is the `assembly` Mojo (`org.machanism.machai.assembly.maven.Assembly`). The goal is intentionally configured with `requiresProject=false`, so it can be run outside of a standard Maven project while still operating on the configured Maven `${basedir}`.

The workflow follows a metadata-driven, repeatable process aligned with Machanism’s [Guided File Processing](https://www.machanism.org/guided-file-processing/index.html): high-level intent (the prompt) is converted into consistent, automated file and configuration changes.

## Overview

The plugin’s value is turning “what I want to build” into a concrete and reproducible project setup:

- Captures intent as a prompt (file-based or interactive).
- Uses `Picker` to semantically search bindex entries and recommend libraries.
- Runs `ApplicationAssembly` to apply the assembled changes to the target directory.

This enables rapid bootstrapping and standardized assembly across projects.

## Key Features

- Maven goal `assembly` that can run with or without a Maven project (`requiresProject=false`).
- Prompt input via `project.txt` (configurable), with interactive fallback.
- GenAI-backed library recommendations through MachAI `Picker`.
- Minimum recommendation score threshold (`assembly.score`).
- Optional bindex registration/lookup URL support (`bindex.register.url`).
- Separate model selection for recommendation (`picker.model`) vs assembly (`assembly.model`).
- Applies changes directly to the configured base directory (`${basedir}`).

## Getting Started

### Prerequisites

- **Java:** see version notes below.
- **Maven:** a Maven installation capable of executing plugins.
- **GenAI provider configuration:** the MachAI provider referenced by the configured model identifiers must be available (credentials and environment variables depend on the chosen provider).
- **bindex configuration:** a `bindex.properties` file must be available at execution time (used by the workflow configuration).
- (Optional) Network access to any configured bindex registration/lookup service.

### Java Version

- **Build-level requirement (from `pom.xml`):** compiled with `maven.compiler.release=8` (Java 8).
- **Functional/runtime note:** runtime needs may vary by GenAI provider integrations and environment, but the plugin is intended to be compatible with Java 8 as declared.

### Basic Usage

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly
```

To provide a prompt via file, create `project.txt` in the directory you are running from (or set `-Dassembly.prompt.file=...`).

### Typical Workflow

1. Choose the directory where changes should be applied (the Maven `${basedir}`).
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
| `bindex.register.url` | Optional registration URL used by the picker for metadata lookups/registration. | _(none)_ |

## Resources

- MachAI documentation: https://machai.machanism.org/
- Guided File Processing: https://www.machanism.org/guided-file-processing/index.html
- GitHub (monorepo): https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin
