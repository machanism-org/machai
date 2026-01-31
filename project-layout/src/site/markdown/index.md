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

# project-layout

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/project-layout.svg)](https://central.sonatype.com/artifact/org.machanism.machai/project-layout)

## Introduction

Project Layout is a small, focused Java API for detecting and describing a software project’s on-disk structure (main/test source roots, resource roots, and multi-module/workspace layouts). It enables tools to avoid hard-coded conventions by offering a consistent way to infer a project layout from the files present.

## Overview

This module models “project layout” as a set of directories and conventions that vary by ecosystem (for example Maven, Node workspaces, or Python projects). A `ProjectLayout` implementation can:

- Identify directories that contain main sources, tests, and resources.
- Detect subprojects/modules/workspaces.
- Provide a normalized view that higher-level tooling can consume.

## Key Features

- Maven layout detection (including `pom.xml` build source/resource configuration)
- JavaScript/Node workspace detection (via `package.json`)
- Python project layout detection (via `pyproject.toml`)
- Default/fallback layout when no known build descriptor is present
- API to enumerate child projects/modules

## Getting Started

### Prerequisites

- Java 8+ (library usage)
- Maven or Gradle (to build from source)

### Environment Variables

This project does not require any environment variables.

| Name | Required | Description | Default |
|---|---:|---|---|
| _None_ |  |  |  |

### Basic Usage

```java
import java.nio.file.Path;
import org.machanism.machai.project.ProjectLayoutManager;

var manager = new ProjectLayoutManager();
var layout = manager.detect(Path.of("."));

// Use layout to find source/test/resource directories and child projects
```

### Typical Workflow

1. Point the detector at a repository root (a `Path`).
2. Detect the layout (Maven/JS/Python/default).
3. Read main/test/resource roots from the resulting `ProjectLayout`.
4. If present, iterate child projects/modules and repeat.

## Configuration

The library is primarily convention-driven; most users only choose the root directory and optionally select/force a specific layout detector (if your integration exposes that option).

| Parameter | Description | Default |
|---|---|---|
| `projectRoot` | Repository or module root directory to inspect. | `.` |
| `layout` | Layout strategy (auto-detect vs. specific). | `auto` |

Example (pseudo CLI):

```bash
machai analyze --projectRoot . --layout auto
```

## Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/project-layout
- Source: https://github.com/machanism-org/machai
