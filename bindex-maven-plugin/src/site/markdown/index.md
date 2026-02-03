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

The **Bindex Maven Plugin** enables automated generation and registration of `bindex` metadata for Maven projects.

It helps:

- Produce a consistent, machine-readable descriptor (`bindex.json`) for each Maven module.
- Keep that descriptor aligned with the module as it evolves.
- Improve downstream discovery, indexing, and assembly workflows that rely on structured metadata (including GenAI-assisted semantic search within the Machanism ecosystem).

## Overview

The plugin generates and maintains `bindex` metadata for the current Maven module.

It can be run on-demand or bound to a Maven lifecycle phase (for example, `generate-resources`) so that metadata is always produced as part of a build.

During execution it will:

- Generate `bindex.json` when it does not exist.
- Optionally update an existing `bindex.json` (when enabled) to keep metadata aligned with the module.

## Key Features

- Generates a `bindex.json` descriptor for the current Maven module.
- Updates an existing descriptor to keep metadata synchronized with project changes.
- Designed to be run manually or bound to a lifecycle phase.
- Standardizes metadata for discovery, automation, and indexing workflows.

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
2. Run the plugin to generate `bindex.json` for the module.
3. Commit `bindex.json` if your repository policy expects generated metadata to be versioned.
4. Use `bindex.json` with downstream tooling that consumes `bindex` descriptors.

## Configuration

| Parameter | Description | Default |
|---|---|---|
| `update` | Whether to update an existing `bindex.json` if present. | `false` |

Example (system property):

```bash
mvn org.machanism.machai:bindex-maven-plugin:bindex -Dupdate=false
```

## Resources

- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven](https://maven.apache.org)
