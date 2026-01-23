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

Assembly Maven Plugin automates assembly-style updates to a local Maven project within the Machanism ecosystem. It applies concrete, reviewable changes to your working tree—such as updating `pom.xml` and related project files—based on a short project concept (for example `project.txt`). When available, it can also use Machanism bindex metadata (for example `bindex.json`) and GenAI-assisted semantic search to recommend and integrate dependencies.

Benefits:

- **Faster bootstrapping:** reduces time spent on boilerplate Maven setup.
- **Metadata-informed dependency choice:** uses bindex metadata to recommend libraries that fit your needs.
- **Human-controlled output:** writes changes to disk so you can inspect, edit, and commit.

## Overview

Use this plugin to generate or evolve a Maven project by applying structured updates locally, instead of performing repetitive manual setup.

Value proposition:

- **Project evolution via local changes:** updates your working tree so results are transparent and reviewable.
- **Smarter dependency selection:** combines bindex metadata with semantic search to propose relevant dependencies.
- **Repeatable workflow:** run the goal as your concept evolves and keep only the changes you want.

## Key Features

- **Project bootstrap and evolution:** creates or updates common project files and structure.
- **Metadata-driven assembly:** uses Machanism bindex metadata (for example `bindex.json`) to guide library selection.
- **GenAI-assisted discovery:** supports semantic discovery of candidate libraries.
- **Reviewable output:** writes changes into your project so you can inspect, adjust, and commit them.

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6.x or newer
- Access to a compatible GenAI provider (for example OpenAI), if GenAI-based steps are enabled

### Environment Variables

| Variable         | Description                    |
|------------------|--------------------------------|
| `OPENAI_API_KEY` | API key for the OpenAI backend |

### Basic Usage

Run the plugin goal:

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly
```

### Typical Workflow

1. Write a short project concept in `project.txt` (or another file you reference).
2. (Optional) Provide bindex metadata such as `bindex.json`.
3. Run the `assembly` goal.
4. Review the updated `pom.xml` and any generated/modified project files.
5. Keep what you want (edit as needed) and commit the changes.

## Configuration

Common parameters:

| Parameter              | Description                                  | Default             |
|------------------------|----------------------------------------------|---------------------|
| `assembly.genai`       | GenAI model used for assembly tasks          | `OpenAI:gpt-5`      |
| `pick.genai`           | Model used for library selection             | `OpenAI:gpt-5-mini` |
| `assembly.prompt.file` | Path to the project concept file             | `project.txt`       |
| `assembly.score`       | Minimum confidence score for recommendations | `0.80`              |

### Example

```bash
mvn org.machanism.machai:assembly-maven-plugin:assembly \
  -Dassembly.prompt.file=project.txt \
  -Dassembly.genai=OpenAI:gpt-5 \
  -Dpick.genai=OpenAI:gpt-5-mini \
  -Dassembly.score=0.80
```

## Resources

- [Machanism Platform](https://machanism.org)
- [Machai on GitHub](https://github.com/machanism-org/machai)
- [Maven Central Artifact](https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin)
- [Maven](https://maven.apache.org)
