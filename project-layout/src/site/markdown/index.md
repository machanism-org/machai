<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
# Page Structure: 
1. Header
   - Project Title: need to use from pom.xml
   - Maven Central Badge [![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
   - Bindex Badge [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/project-layout/bindex.json)
# Introduction
   - Full description of purpose and benefits.
# Overview
   - Explanation of the project function and value proposition.
   - Use the project structure diagram by the path: `./images/c4-diagram.png` (`src/site/puml/c4-diagram.puml`).
# Key Features
   - Bulleted list highlighting the primary capabilities of the project.
# Getting Started
   - Prerequisites: List of required software and services.
   - Basic Usage: Example command to run the plugin.
   - Typical Workflow: Step-by-step outline of how to use the project artifacts.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# Project Layout

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/project-layout.svg)](https://central.sonatype.com/artifact/org.machanism.machai/project-layout)
[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/project-layout/bindex.json)

## Introduction

Project Layout is a Java utility library for describing, detecting, and working with conventional project directory layouts in a consistent way. It gives build tooling, scanners, generators, validation utilities, and plugins a shared model for locating well-known folders such as production sources, test sources, resources, and documentation directories.

Instead of duplicating path conventions throughout each tool, Project Layout centralizes these rules behind reusable layout implementations. This improves maintainability, reduces configuration drift, and makes project-structure discovery easier to adapt across different technology stacks and repository styles.

## Overview

Project Layout provides abstractions and concrete layout strategies for common project ecosystems. It includes support for Maven, Gradle, JavaScript, Python, and default fallback project structures, along with utilities for reading project metadata and coordinating layout selection from a project root.

The library is especially useful for tools that must operate over heterogeneous repositories or Maven multi-module builds. By resolving important directories through a common API, downstream tools can focus on analysis, generation, documentation, validation, or indexing without hard-coding ecosystem-specific folder rules.

![Project Layout C4 Diagram](./images/c4-diagram.png)

## Key Features

- Standardized representation of conventional source, test, resource, and documentation directories
- Built-in layout implementations for Maven, Gradle, JavaScript, Python, and default project structures
- Project-root-relative path resolution for reliable tool integration
- Maven metadata support through `PomReader` for reading `pom.xml` project information
- Layout coordination through `ProjectLayoutManager` and project processing support through `ProjectProcessor`
- Suitable for build plugins, repository scanners, code generators, documentation tooling, and validation workflows
- Lightweight Java library designed for reusable integration in other Machai components and external tools

## Getting Started

### Prerequisites

- Java 8 or later
- Maven 3.x or later for building and consuming the library
- Access to Maven Central or another repository containing `org.machanism.machai:project-layout`
- A project directory whose structure needs to be resolved or analyzed

### Basic Usage

Add Project Layout to a Maven project that needs project-structure resolution:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>project-layout</artifactId>
  <version>${project.version}</version>
</dependency>
```

Build or verify the library from the project root with Maven:

```bash
mvn clean verify
```

### Typical Workflow

1. Add `project-layout` as a dependency to the plugin, scanner, generator, or build tool that needs to inspect project structure.
2. Identify the target project root directory that should be analyzed.
3. Select the appropriate layout implementation, such as `MavenProjectLayout`, `GragleProjectLayout`, `JScriptProjectLayout`, `PythonProjectLayout`, or `DefaultProjectLayout`, or delegate layout coordination to `ProjectLayoutManager`.
4. Resolve the relevant source, test, resource, and documentation paths through the selected layout abstraction.
5. Use the resolved paths to drive compilation support, static analysis, code generation, documentation publishing, validation, or project indexing.
6. Reuse the same layout model across tools to keep project-structure handling consistent and maintainable.

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/project-layout
- Bindex Metadata: https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/project-layout/bindex.json
- GitHub Repository: https://github.com/machanism-org/machai
- Source Repository: https://github.com/machanism-org/machai.git
- Issue Tracker: https://github.com/machanism-org/machai/issues
