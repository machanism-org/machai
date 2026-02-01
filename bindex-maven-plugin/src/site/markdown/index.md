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

The **Bindex Maven Plugin** enables automated generation and registration of `bindex` metadata for Maven projects. It facilitates library discovery, integration, and assembly by leveraging structured metadata and GenAI-powered semantic search within the Machanism ecosystem.

## Overview

The plugin is typically run during a Maven build (optionally bound to a lifecycle phase) or executed on-demand.

At execution time it will:

- Generate `bindex.json` when it does not exist.
- Update an existing `bindex.json` (when enabled) to keep metadata aligned with the module.

## Key Features

- Generates a `bindex.json` descriptor for the current Maven module.
- Updates an existing descriptor to keep metadata synchronized with project changes.
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
2. Run `mvn org.machanism.machai:bindex-maven-plugin:bindex` to generate or update `bindex.json`.
3. Commit `bindex.json` if your repository policy expects generated metadata to be versioned.
4. Use the generated descriptor with any downstream tooling that consumes `bindex.json`.

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
