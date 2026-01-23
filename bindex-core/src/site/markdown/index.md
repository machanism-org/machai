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

# Bindex Core

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-core.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)

## Introduction

Bindex Core is the foundational Java library for producing and consuming **bindex** metadata. It provides the building blocks for metadata generation, registration, library selection, and project assembly—enabling automated discovery, integration, and assembly workflows within the Machanism ecosystem.

## Overview

Bindex Core provides APIs and reference implementations to:

- Generate bindex metadata for Java artifacts
- Read, merge, and analyze bindex metadata produced by other tools
- Integrate with Maven-oriented models and plugins

## Key Features

- Metadata generation for Java projects and artifacts
- Registration and indexing of produced metadata
- Aggregation and merging across modules and dependency graphs
- Library selection utilities to support automated assembly workflows
- Extensible components suitable for Maven models and plugin APIs

## Getting Started

### Prerequisites

- Java 9+ (project property: `java.version=9`)
- Maven 3.x

### Environment Variables

Bindex Core does not require environment variables by default.

| Variable | Required | Description | Default |
|---|---:|---|---|
| (none) | No | No environment variables are required for the core library. | N/A |

### Basic Usage

Add it to your `pom.xml`:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>bindex-core</artifactId>
  <version>${bindex-core.version}</version>
</dependency>
```

Build the module:

```bash
mvn -pl bindex-core clean verify
```

### Typical Workflow

1. Add `bindex-core` as a dependency to the tool/plugin or application that needs to produce/consume bindex metadata.
2. Generate bindex metadata during your build (typically via a Maven plugin or build integration).
3. Register and publish produced metadata so it can be discovered by downstream tools.
4. Load and merge metadata from multiple modules/artifacts.
5. Apply selection/assembly logic using the merged metadata.

## Configuration

Bindex Core is a library; most configuration is performed through the consuming tool/plugin. The table below lists commonly used, project-relevant settings and their defaults.

| Parameter | Description | Default |
|---|---|---|
| `java.version` | Target Java version used to compile the project. | `9` |
| `maven.javadoc.skip` | Whether to skip Javadoc generation. | `false` |

Example (set project properties when building):

```bash
mvn -pl bindex-core -Djava.version=11 -Dmaven.javadoc.skip=true clean verify
```

## Resources

- GitHub: <https://github.com/machanism-org/machai>
- Maven Central: <https://central.sonatype.com/artifact/org.machanism.machai/bindex-core>
- Issues: <https://github.com/machanism-org/machai/issues>
