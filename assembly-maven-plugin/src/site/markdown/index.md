# Assembly Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/assembly-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin)

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

## Introduction

Assembly Maven Plugin is a Maven plugin that automates assembling and evolving Maven projects in the Machanism ecosystem by applying structured, reviewable updates directly to your local working tree.

It can integrate libraries using bindex metadata (for example, `bindex.json`) and, when enabled, uses GenAI-powered semantic search to help identify and select suitable libraries.

Benefits:

- Speeds up project bootstrapping and iteration by automating repetitive setup and wiring.
- Uses bindex metadata and (optionally) GenAI semantic search to guide library selection.
- Keeps humans in control: changes are written locally so you can review before committing.

## Overview

Use this plugin to generate or evolve a Maven project by applying assembly-style updates to your working tree, rather than performing repetitive manual configuration.

Value proposition:

- **Faster bootstrapping:** reduces time spent on boilerplate project setup.
- **Metadata-informed dependency choice:** integrates libraries based on bindex metadata.
- **Human-controlled output:** changes are written to disk so you can inspect, edit, and commit.

## Key Features

- **Project bootstrap and evolution:** creates or updates common project files and structure.
- **Metadata-driven assembly:** integrates libraries based on bindex metadata (for example, `bindex.json`).
- **GenAI-assisted discovery (optional):** uses semantic search to recommend suitable libraries.
- **Reviewable output:** writes changes into your project so you can inspect, adjust, and commit them.

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6.x or newer
- Network access to an OpenAI-compatible GenAI provider, if you enable GenAI-backed features

### Environment Variables

| Variable | Description |
|---|---|
| *(none required by default)* | The plugin can run without environment variables when GenAI-backed features are disabled. |
| `OPENAI_API_KEY` | API key for an OpenAI-compatible API (only required when GenAI-backed features are enabled). |

### Basic Usage

```text
mvn org.machanism.machai:assembly-maven-plugin:0.0.9-SNAPSHOT:assembly
```

### Typical Workflow

1. Create a short project concept file (for example, `project.txt`).
2. (Optional) Provide bindex metadata (for example, `bindex.json`).
3. Run the `assembly` goal.
4. Review the updated `pom.xml` and any generated/modified project files.
5. Keep what you want (edit as needed) and commit the changes.

## Configuration

Common parameters:

| Parameter | Description | Default |
|---|---|---|
| `assembly.prompt.file` | Path to the project concept file. | `project.txt` |
| `assembly.genai` | GenAI model used for assembly tasks (when enabled). | plugin-defined |
| `pick.genai` | Model used for library selection (when enabled). | plugin-defined |
| `assembly.score` | Minimum confidence score for recommendations. | `0.80` |

### Example

```text
mvn org.machanism.machai:assembly-maven-plugin:0.0.9-SNAPSHOT:assembly ^
  -Dassembly.prompt.file=project.txt ^
  -Dassembly.genai=OpenAI:gpt-5 ^
  -Dpick.genai=OpenAI:gpt-5-mini ^
  -Dassembly.score=0.80
```

## Resources

- [Machanism Platform](https://machanism.org)
- [Machai on GitHub](https://github.com/machanism-org/machai)
- [Maven Central Artifact](https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin)
- [Maven](https://maven.apache.org)
