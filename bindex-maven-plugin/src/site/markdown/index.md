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

The **Bindex Maven Plugin** enables automated generation and registration of **bindex** metadata for Maven projects. It generates a structured `bindex.json` descriptor for each Maven module so downstream tools can reliably discover, integrate, and assemble libraries.

Key benefits:

- Produces a consistent, machine-readable `bindex.json` per module.
- Keeps metadata aligned with your module as it evolves (optional update mode).
- Improves downstream discovery, integration, and assembly workflows that rely on structured metadata.
- Supports metadata-driven automation (including GenAI-powered semantic search) within the Machanism ecosystem.

## Overview

This plugin generates and maintains **bindex** metadata for the current Maven module. You can run it on-demand or bind it to a Maven lifecycle phase (for example, `generate-resources`) so metadata is always produced as part of the build.

During execution it will:

- Generate `bindex.json` when it does not exist.
- Optionally update an existing `bindex.json` (when enabled) to keep metadata synchronized with the module.

## Key Features

- Generates a `bindex.json` descriptor for the current Maven module.
- Optionally updates an existing descriptor to keep metadata synchronized with project changes.
- Can be executed directly or bound to a lifecycle phase.
- Standardizes metadata for discovery, indexing, and assembly workflows.

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
mvn org.machanism.machai:bindex-maven-plugin:0.0.9-SNAPSHOT:bindex
```

### Typical Workflow

1. Add the plugin to your project `pom.xml` (optionally bind it to a lifecycle phase).
2. Run the `bindex` goal to generate `bindex.json` for the module.
3. Commit `bindex.json` if your repository policy expects generated metadata to be versioned.
4. Use `bindex.json` with downstream tooling that consumes **bindex** descriptors.

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `update` | Update an existing `bindex.json` if present. | `false` |

Example (system property):

```bash
mvn org.machanism.machai:bindex-maven-plugin:0.0.9-SNAPSHOT:bindex -Dupdate=true
```

## Resources

- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven](https://maven.apache.org)
