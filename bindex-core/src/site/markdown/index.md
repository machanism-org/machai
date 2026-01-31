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

# bindex-core

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-core.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core)

## Introduction

Bindex Core provides the foundational types and utilities for working with *bindex* metadata in the Machanism ecosystem. It enables tools and applications to generate, publish, discover, validate, and aggregate metadata so that libraries can be selected and assembled consistently across modules and builds.

## Overview

Bindex Core defines the core representation of bindex metadata and the supporting utilities used by other Machanism components. It is commonly consumed by build tools (for example, Maven plugins) and can also be used directly in applications to:

- Generate bindex metadata for produced artifacts.
- Register/publish metadata so it can be discovered by downstream tooling.
- Read, validate, and merge metadata from multiple modules and dependencies.
- Support library selection and project assembly workflows.

## Key Features

- Core model and utilities for generating and consuming bindex metadata.
- Metadata registration/publishing support for downstream discovery.
- Validation and analysis utilities for bindex metadata.
- Merge/aggregation utilities across modules and dependencies.
- APIs that support library selection and project assembly decisions.

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
mvn -pl bindex-core clean verify
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

Example (override build properties):

```bash
mvn -pl bindex-core -Dmaven.compiler.release=17 -Dmaven.javadoc.skip=true clean verify
```

## Resources

- Project: <https://github.com/machanism-org/machai>
- Maven Central: <https://central.sonatype.com/artifact/org.machanism.machai/bindex-core>
- Issues: <https://github.com/machanism-org/machai/issues>
