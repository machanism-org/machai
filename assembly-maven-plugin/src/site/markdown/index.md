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

Assembly Maven Plugin is a Maven plugin that automates project assembly tasks within the Machanism ecosystem.

It helps you go from a short project concept to concrete, reviewable changes in your working tree (for example updates to `pom.xml` and related project files). It can also use Machanism bindex metadata (for example `bindex.json`) to guide dependency selection and streamline setup.

## Overview

Use this plugin when you want to quickly generate or evolve a Maven project by applying structured updates locally, rather than performing repetitive manual setup.

Value proposition:

- **Faster bootstrapping:** reduces time spent on boilerplate project setup.
- **Better library choices:** can recommend dependencies using metadata and semantic search.
- **Human-controlled output:** all changes are written to disk so you can inspect, edit, and commit.

## Key Features

- **Project bootstrap and evolution:** creates or updates common project files and structure.
- **Metadata-driven assembly:** can use Machanism bindex metadata (for example `bindex.json`) to guide library selection.
- **GenAI-assisted search and selection:** supports semantic discovery of candidate libraries.
- **Reviewable output:** writes changes into your project so you can inspect, adjust, and commit them.

## Getting Started

### Prerequisites

- Java 8 or higher
- Maven 3.6.x or newer
- Access to a compatible GenAI provider (for example OpenAI)

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
2. Run the `assembly` goal.
3. Review the updated `pom.xml` and any generated/modified project files.
4. Keep what you want (edit as needed) and commit the changes.

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
