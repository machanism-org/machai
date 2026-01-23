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

The **Bindex Maven Plugin** enables automated generation and optional registration of `bindex.json` metadata for Maven projects in the Machanism/Machai ecosystem. By producing a consistent, structured descriptor for each module, it improves artifact discovery, integration, and assembly workflows and supports GenAI-powered semantic search scenarios that rely on rich, standardized metadata.

## Overview

This plugin runs during your Maven build (or on-demand) to create or maintain a `bindex.json` descriptor for the current module.

- If `bindex.json` does not exist, it is generated.
- If it already exists, it can be updated in-place to stay in sync with the project.
- When registry settings are configured, the descriptor can be registered/published as part of an automation pipeline.

## Key Features

- Generates a `bindex.json` descriptor for the current Maven module.
- Updates an existing descriptor to keep metadata synchronized with the project.
- Optionally registers/publishes the descriptor when configured for a registry.

## Getting Started

### Prerequisites

- Java 9+
- Maven 3.6+

### Environment Variables

Registration/publishing may require credentials depending on your registry configuration.

| Variable name | Description |
|---|---|
| `BINDEX_REG_PASSWORD` | Password/token used when writing to the registration database/registry (only required if registration is enabled). |

### Basic Usage

Generate (or update) `bindex.json`:

```bash
mvn org.machanism.machai:bindex-maven-plugin:bindex
```

Register/publish the descriptor (optional):

```bash
mvn org.machanism.machai:bindex-maven-plugin:register
```

### Typical Workflow

1. Configure the plugin in your project `pom.xml` (optionally bind it to a lifecycle phase).
2. Run `mvn org.machanism.machai:bindex-maven-plugin:bindex` to generate or update `bindex.json`.
3. Commit `bindex.json` if your repository policy expects generated metadata to be versioned.
4. If you use a registry, configure credentials/settings and run `mvn org.machanism.machai:bindex-maven-plugin:register` (or run it in CI).

## Configuration

Add the plugin to your `pom.xml`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.machanism.machai</groupId>
      <artifactId>bindex-maven-plugin</artifactId>
      <version>${bindex-maven-plugin.version}</version>
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

### Common Parameters

| Parameter | Description | Default |
|---|---|---|
| `update` | Whether to update an existing `bindex.json` if present. | `true` |

Example (system property):

```bash
mvn org.machanism.machai:bindex-maven-plugin:bindex -Dupdate=false
```

## Resources

- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/bindex-maven-plugin)
- [Machai (GitHub)](https://github.com/machanism-org/machai)
- [Maven](https://maven.apache.org)
