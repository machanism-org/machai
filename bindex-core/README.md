<!-- @guidance: >>> ${guidances}/readme-content.md -->
# Bindex Core

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/bindex-core.svg)](https://central.sonatype.com/artifact/org.machanism.machai/bindex-core) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/bindex-core/refs/heads/main/bindex.json)

## Project Structure

Bindex Core separates AI-facing operations from the workflow that classifies requests, creates embeddings, and coordinates metadata registration and discovery. A repository abstraction isolates persistence concerns, while the MongoDB implementation stores Bindex records and performs filtered vector searches. External working directories provide descriptors, and configurable GenAI services provide classification and embedding capabilities.

![Bindex Core project structure](./images/project-structure.png)

## Introduction

Bindex Core is a Java 17 library for describing reusable software components as Bindex v2 metadata and making that metadata discoverable to developers, AI agents, and MCP integrations. It validates and registers Bindex JSON descriptors, retrieves complete or field-selected metadata, and recommends libraries from natural-language requirements.

The library combines schema-based metadata, generated embeddings, semantic vector search, classification filters, and a MongoDB-backed repository. Its AI-facing tools let an agent inspect metadata, register descriptors from JSON, files, or URLs, retrieve schema guidance, and select relevant libraries. This makes reusable capabilities easier to govern, find, and integrate across Maven projects.

## Overview

A Bindex record captures a library's coordinates, version, purpose, classification, integrations, dependencies, examples, and configuration guidance. The discovery workflow is:

1. Assemble project documentation and build metadata into a schema-compliant descriptor.
2. Convert its classification into an embedding and store it with searchable metadata.
3. Classify a development request and convert it into a query embedding.
4. Search semantically, narrow results by language and architectural layer, apply relevance thresholds, and select the most useful library versions.
5. Use the selected descriptors to guide implementation, assembly, or dependency resolution.

## Usage

Bindex Core is assembled for use with the [Bindex MCP Server](https://github.com/machanism-org/bindex-mcp-server) and is included by default in the [Ghostwriter CLI](https://machai.machanism.org/ghostwriter/index.html#Download).

When using `gw-maven-plugin`, add Bindex Core as a plugin dependency:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>gw-maven-plugin</artifactId>
  <version>RELEASE</version>
  <!-- other plugin configuration -->
  <dependencies>
    <dependency>
      <groupId>org.machanism.machai</groupId>
      <artifactId>bindex-core</artifactId>
      <version>RELEASE</version>
    </dependency>
  </dependencies>
</plugin>
```

For direct Maven use, declare the library in the consuming project:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>bindex-core</artifactId>
  <version>RELEASE</version>
</dependency>
```

Configure a GenAI provider and embedding provider through the host application, configure repository connectivity, register Bindex descriptors, and use the library-selection operation to find reusable components for new requirements.
