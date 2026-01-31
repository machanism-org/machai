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

# Bindex Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)

## Introduction

The **Bindex Maven Plugin** generates and maintains a `bindex.json` metadata descriptor for Maven modules.

Using a standardized, machine-readable metadata file supports consistent library discovery, integration and assembly workflows, and downstream automation (including semantic search/indexing use cases).

## Overview

Run the plugin as part of your Maven build (for example, bound to a lifecycle phase) or on-demand to create or update the `bindex.json` descriptor for the current module:

- If `bindex.json` does not exist, it is generated.
- If it already exists, it can be updated in-place to keep metadata synchronized with the project.

## Key Features

- Generates a `bindex.json` descriptor for the current Maven module.
- Updates an existing descriptor to keep metadata synchronized with the project.
- Enables standardized metadata workflows for discovery, automation, and indexing.

## Getting Started

### Prerequisites

- Java 11+
- Maven 3.6+

### Environment Variables

The plugin does not require any environment variables.

| Variable name | Description |
|---|---|
| _None_ | _Not required._ |

### Basic Usage

Generate (or update) `bindex.json`:

```bash
mvn org.machanism.machai:bindex-maven-plugin:bindex
```

### Typical Workflow

1. Add/configure the plugin in your project `pom.xml` (optionally bind it to a lifecycle phase).
2. Run `mvn org.machanism.machai:bindex-maven-plugin:bindex` to generate or update `bindex.json`.
3. Commit `bindex.json` if your repository policy expects generated metadata to be versioned.
4. Use the generated descriptor in any downstream tooling that consumes `bindex.json`.

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `update` | Whether to update an existing `bindex.json` if present. | `true` |

Example (system property):

```bash
mvn org.machanism.machai:bindex-maven-plugin:bindex -Dupdate=false
```

## Resources

- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven](https://maven.apache.org)
