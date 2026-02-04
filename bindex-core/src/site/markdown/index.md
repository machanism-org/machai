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

Bindex Core (`bindex-core`) provides core functionality for bindex metadata management, including generation, registration, library selection, and project assembly. It enables automated handling of library metadata to support efficient discovery, integration, and assembly workflows within the Machanism ecosystem.

## Overview

Bindex Core defines the canonical representation of bindex metadata and provides APIs intended for other Machanism components (for example, Maven plugins and build integrations). Typical uses include:

- Generating bindex metadata for produced artifacts.
- Reading and validating metadata from modules and dependencies.
- Merging and aggregating metadata across multi-module builds.
- Driving library selection and project assembly decisions based on metadata.
- Supporting publishing/registration so metadata can be discovered by downstream tooling.

## Key Features

- Canonical model for representing bindex metadata.
- APIs and utilities for generating, reading, and validating metadata.
- Merge/aggregation helpers for multi-module and dependency scenarios.
- Library selection and project assembly support based on metadata.
- Hooks for publishing/registration workflows used by downstream tools.

## Getting Started

### Prerequisites

- Java 11+
- Maven 3.9+

### Environment Variables

Bindex Core does not require environment variables by default.

| Variable | Required | Description | Default |
|---|:---:|---|---|
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

Build this module:

```bash
mvn clean verify
```

### Typical Workflow

1. Add `bindex-core` as a dependency to the tool/plugin or application that needs to produce or consume bindex metadata.
2. Produce bindex metadata during your build (typically via a Maven plugin or other build integration).
3. Publish produced metadata so it can be discovered and used by downstream tooling.
4. Load, validate, and merge metadata from multiple modules/artifacts.
5. Apply selection and assembly logic using the resulting metadata.

## Configuration

Bindex Core is a library; most configuration is performed by the consuming tool/plugin. The table below lists commonly relevant Maven build properties and their typical defaults.

| Parameter | Description | Default |
|---|---|---|
| `maven.compiler.release` | Java release target used to compile the project. | `11` |
| `maven.javadoc.skip` | Whether to skip Javadoc generation. | `false` |
| `skipTests` | Whether to skip unit tests during the build. | `false` |

Example (override build properties):

```bash
mvn -Dmaven.compiler.release=17 -DskipTests=true clean verify
```

## Resources

- GitHub: <https://github.com/machanism-org/machai>
- Maven Central: <https://central.sonatype.com/artifact/org.machanism.machai/bindex-core>
- Issues: <https://github.com/machanism-org/machai/issues>
