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

The **Bindex Maven Plugin** generates and (optionally) registers **bindex metadata** for Maven projects. It helps make build outputs easier to discover and integrate by emitting structured, machine-readable metadata derived from the Maven project model.

Downstream tooling (including components in the Machanism ecosystem) can use this metadata to improve library discovery, enrich dependency analysis, and enable GenAI-assisted semantic search and automated assembly workflows.

# Overview

During a Maven build, the plugin inspects the current project (typically skipping aggregator/parent projects with `pom` packaging) and uses Maven coordinates and model information (packaging, dependencies, and related descriptors) to create or update a local bindex index.

When registration is enabled, the plugin can also publish the generated metadata to a registry endpoint so it can be discovered across projects.

Value proposition:

- **Repeatable, automated metadata generation** as part of a standard Maven lifecycle.
- **Improved discoverability** of artifacts for both humans and tools.
- **Better integration workflows** by enabling indexing, search, and semantic enrichment based on structured metadata.

# Key Features

- Maven goals to **create**, **update**, **clean**, and **register** bindex metadata for a project.
- Deterministic metadata generation based on the Maven project model.
- Optional registration/publishing to a configurable registry endpoint.

# Getting Started

## Prerequisites

- Java 8+
- Maven 3.x
- Network access (only required if registration/publishing is enabled and targets a remote service)

## Environment Variables

This plugin does not require environment variables for basic index/metadata generation.

| Environment variable | Required | Description | Default |
|---|---:|---|---|
| (none) | No | No environment variables are required for basic usage. | n/a |

## Basic Usage

Add the plugin to your project’s `pom.xml` and run a goal.

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
            <goal>create</goal>
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

Or run it directly from the command line:

```cmd
mvn org.machanism.machai:bindex-maven-plugin:create -Dbindex.genai=OpenAI:gpt-5
```

## Typical Workflow

1. Configure the plugin in your `pom.xml` (or invoke a goal directly).
2. Run your standard Maven lifecycle (`mvn verify`, `mvn package`, etc.).
3. Use `create` (first run) or `update` (subsequent runs) to generate/refresh the project’s bindex metadata.
4. (Optional) Use `register` to publish the metadata to a registry endpoint.
5. Use downstream tooling to search, analyze, or assemble artifacts based on the indexed metadata.

# Configuration

| Parameter | Description | Default |
|---|---|---|
| `bindex.genai` | AI provider/model identifier used for indexing (for example `OpenAI:gpt-5`). | (required) |
| `bindex.register.url` | Registry endpoint URL used by the `register` goal. | (none) |
| `update` | When `true`, `register` performs an update while registering. | `true` |

Example configuring and running registration:

```cmd
mvn org.machanism.machai:bindex-maven-plugin:register -Dbindex.genai=OpenAI:gpt-5 -Dbindex.register.url=http://localhost:8080
```

# Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin
- Project site: https://machai.machanism.org/bindex-maven-plugin
- Source repository: https://github.com/machanism-org/machai
- Issue tracker: https://github.com/machanism-org/machai/issues
