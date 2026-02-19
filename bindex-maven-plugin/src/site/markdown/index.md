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

# Introduction

The **Bindex Maven Plugin** automates the generation and (optionally) registration of **bindex metadata** for Maven projects.
This metadata is designed to improve library discovery, integration, and assembly—particularly when paired with the Machanism ecosystem’s structured metadata model and GenAI-powered semantic search.

# Overview

This plugin runs during your Maven build to produce machine-readable bindex metadata describing your project and its outputs.
Downstream tools can then index and query that metadata to:

- discover compatible libraries faster,
- enrich search and dependency analysis with semantic information,
- support automated assembly/integration workflows.

# Key Features

- Generates bindex metadata as part of a standard Maven build.
- Supports repeatable builds (metadata can be regenerated deterministically from the project model).
- Integrates with Machanism tooling for metadata indexing and semantic search.

# Getting Started

## Prerequisites

- Java 8+ (build runtime)
- Maven 3.x
- Network access (only required if you publish/register metadata to a remote service)

## Environment Variables

This plugin does not require any environment variables for basic metadata generation.

| Environment variable | Required | Description | Default |
|---|---:|---|---|
| (none) | No | No environment variables are required for basic usage. | n/a |

## Basic Usage

Add the plugin to your project’s `pom.xml` and run Maven normally:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.machanism.machai</groupId>
      <artifactId>bindex-maven-plugin</artifactId>
      <version><!-- use the latest release from Maven Central --></version>
      <executions>
        <execution>
          <goals>
            <goal>bindex</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

```cmd
mvn verify
```

## Typical Workflow

1. Add the plugin to your `pom.xml`.
2. Run your normal build lifecycle (`mvn verify`, `mvn package`, etc.).
3. Collect the generated bindex metadata from the configured output location.
4. (Optional) Publish/register the generated metadata so it can be indexed and searched by other tools.

# Configuration

Common configuration parameters (names may vary by plugin version):

| Parameter | Description | Default |
|---|---|---|
| `outputDirectory` | Where generated bindex metadata is written. | Plugin-defined |
| `skip` | Skips bindex generation when `true`. | `false` |
| `register` | Registers/publishes generated metadata when `true`. | `false` |

Example with custom parameters:

```cmd
mvn -Dbindex.skip=false -Dbindex.register=false verify
```

# Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin
- Project site: https://machai.machanism.org/bindex-maven-plugin
- Source repository: https://github.com/machanism-org/machai
- Issue tracker: https://github.com/machanism-org/machai/issues
