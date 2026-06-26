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

Project Layout is a Java utility library for describing and working with conventional project directory layouts in a consistent, reusable way. It helps build tooling, scanners, generators, and plugins locate well-known folders such as main sources, test sources, resources, and documentation directories without relying on duplicated or hard-coded path rules.

By centralizing layout conventions in a dedicated library, projects can reduce maintenance overhead, improve consistency across tools, and make project structure resolution easier to adapt for different ecosystems.

## Overview

The library provides a common abstraction for project folder organization and includes concrete implementations for several project types. It supports conventional layouts used by Maven, Gradle, JavaScript, Python, and a default fallback layout, making it useful when tools need to operate across heterogeneous repositories.

Project Layout also includes components for reading project metadata, such as Maven `pom.xml` files, and for selecting or managing layout strategies programmatically. This gives downstream tooling a dependable way to discover source, test, and documentation locations from a project root.

## Key Features

- Standardized representation of conventional project directories
- Built-in layout implementations for Maven, Gradle, JavaScript, Python, and default projects
- Support for resolving paths relative to a project base directory
- Maven metadata integration through `PomReader`
- Layout coordination utilities such as `ProjectLayoutManager`
- Designed for reuse in build tools, plugins, scanners, and code generation workflows

## Getting Started

### Prerequisites

- Java 8 or later
- Maven 3.x for building the library
- A project directory whose layout needs to be analyzed or resolved

### Basic Usage

Add the dependency to your Maven project:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>project-layout</artifactId>
  <version>${project.version}</version>
</dependency>
```

### Typical Workflow

1. Add `project-layout` as a dependency in the tool or plugin that needs to inspect project structure.
2. Determine the target project type and choose the corresponding layout implementation, such as `MavenProjectLayout`, `GragleProjectLayout`, `JScriptProjectLayout`, `PythonProjectLayout`, or `DefaultProjectLayout`.
3. Provide the project root directory to the selected layout or to a coordinating utility such as `ProjectLayoutManager`.
4. Resolve the main source, test source, resource, and documentation directories needed by your tool.
5. Use the resolved paths to drive analysis, generation, validation, or other build-related workflows.

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/project-layout
- GitHub: https://github.com/machanism-org/machai
- Source Repository: https://github.com/machanism-org/machai.git
- Issue Tracker: https://github.com/machanism-org/machai/issues
