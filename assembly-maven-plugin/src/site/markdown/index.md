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

# Assembly Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/assembly-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin)

## Introduction

Assembly Maven Plugin automates the assembly of projects within the Machanism ecosystem by integrating libraries based on bindex metadata. It streamlines dependency resolution, library selection, and project packaging using GenAI-powered semantic search and metadata-driven workflows.

## Overview

The Assembly Maven Plugin helps automate creation of a runnable or distributable project layout by selecting and integrating libraries based on bindex metadata. It is designed to reduce manual dependency curation by leveraging semantic search across available artifacts.

As a standard Maven plugin, it can be run from the command line or configured in a project’s `pom.xml`.

## Key Features

- Metadata-driven library integration using bindex metadata
- GenAI-powered semantic search to help select relevant libraries
- Automates common assembly steps for Machanism ecosystem projects
- Runs as a standard Maven plugin goal during a Maven build

## Getting Started

### Prerequisites

- Java 8 (project compiles with `maven.compiler.release=8`)
- Maven 3.x
- Access to repositories hosting the Machanism artifacts you depend on

### Environment Variables

This plugin does not require any environment variables by default.

| Variable | Required | Description | Default |
|---|---:|---|---|
| (none) | No | No environment variables are required for basic usage. | N/A |

### Basic Usage

Run the plugin goal directly:

```bash
mvn org.machanism.machai:assembly-maven-plugin:<version>:assembly
```

### Typical Workflow

1. Ensure your project (and repositories) provide bindex metadata for the artifacts you want to use.
2. Configure the plugin in your `pom.xml` (optional) to set output location and behavior.
3. Invoke the `assembly` goal to assemble the output based on available metadata.
4. Package or distribute the assembled output using your standard Maven lifecycle.

## Configuration

Supported configuration parameters depend on the goal. To list available goals and parameters, run:

```bash
mvn help:describe -Dplugin=org.machanism.machai:assembly-maven-plugin -Ddetail
```

| Parameter | Description | Default |
|---|---|---|
| `interactive` | Enables interactive prompting when the plugin needs user input. | `false` |
| `searchQuery` | Semantic search query used to select candidate libraries. | (none) |
| `bindexMetadata` | Location or coordinates for bindex metadata to drive assembly. | (none) |
| `outputDirectory` | Where the assembled output is written. | `${project.build.directory}` |

Example configuration in a `pom.xml`:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>assembly-maven-plugin</artifactId>
  <version><!-- plugin version --></version>
  <configuration>
    <interactive>false</interactive>
    <outputDirectory>${project.build.directory}\\assembled</outputDirectory>
  </configuration>
</plugin>
```

Example command-line invocation with custom parameters:

```bash
mvn org.machanism.machai:assembly-maven-plugin:<version>:assembly -Dinteractive=false
```

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/assembly-maven-plugin
- Source (SCM): https://github.com/machanism-org/machai
