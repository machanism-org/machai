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
   - Basic Usage: Example command to run the plugin.
   - Typical Workflow: Step-by-step outline of how to use the project artifacts.
# Resources
   - List of relevant links (platform, GitHub, Maven).
-->

# Project Layout

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/project-layout.svg)](https://central.sonatype.com/artifact/org.machanism.machai/project-layout)

# Introduction

Project Layout is a small utility library for describing and working with conventional project directory layouts (sources, resources, tests, docs, etc.) in a consistent way. It is intended for build tooling and plugins that need to locate well-known folders reliably across different projects.

By centralizing these conventions in one place, tools and plugins can avoid hard-coded paths, reduce duplicated logic, and behave consistently across projects.

# Overview

The library provides a simple model for a project layout (for example, `src/main/java`, `src/test/resources`, `src/site`) and utilities to resolve these paths relative to a project base directory. Using a single, centralized definition of the layout reduces duplicated path logic, makes tooling more predictable, and improves maintainability.

# Key Features

- Standardized representation of common Maven-style project folders
- Resolve layout paths relative to a given project directory
- Designed to be embedded in other tools/plugins that need consistent path conventions

# Getting Started

## Prerequisites

- Java 8+
- Maven 3.x

## Basic Usage

Add the dependency:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>project-layout</artifactId>
  <version>0.0.11-SNAPSHOT</version>
</dependency>
```

## Typical Workflow

1. Use the default Maven-style layout or configure your own conventions.
2. Resolve the directories you need (main sources, test sources, resources, docs) against the project base directory.
3. Use the resolved paths in your build/tooling logic (scanners, generators, compilers, packagers).

# Resources

- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/project-layout
- GitHub: https://github.com/machanism-org/machai
- Issues: https://github.com/machanism-org/machai/issues
