<!-- @guidance: >>> ${guidances}/readme-content.md -->

# Project Layout

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/project-layout.svg)](https://central.sonatype.com/artifact/org.machanism.machai/project-layout) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/project-layout/refs/heads/main/bindex.json)

Project Layout is a Java utility library that gives build tools, repository scanners, generators, validators, and documentation tooling a consistent way to detect and describe conventional project directories. It supports Maven, Gradle, JavaScript/TypeScript, Python, and a filesystem-based fallback layout.

## Project Structure

Project Layout is organized around a common layout contract, ecosystem-specific layout implementations, layout detection, and project processing. The layout manager selects the appropriate implementation from project markers; consumers then use the common API to resolve module, source, test, and documentation roots. Supporting integrations read Maven, Gradle, JSON, and TOML metadata as needed.

![Project structure diagram](./images/project-structure.png)

## Introduction

Build tooling often needs to locate sources, tests, resources, documentation, and modules, but hard-coding those conventions couples each tool to a particular ecosystem. Project Layout centralizes these conventions behind reusable layout implementations so that tools can inspect diverse repositories through one API.

This approach reduces duplicated path-handling logic, configuration drift, and maintenance effort. It is suited to build plugins, repository scanners, code generators, documentation tooling, validation workflows, and indexers that must reliably work with different project structures.

## Overview

The library provides concrete strategies for Maven, Gradle, JavaScript, Python, and a default fallback project structure. `ProjectLayoutManager` detects and configures the first matching layout for a project root, while `ProjectProcessor` supports recursive processing of discovered modules.

Each implementation exposes project-relative paths through the common `ProjectLayout` abstraction. Maven layouts obtain metadata from Maven models, Gradle layouts use the Gradle Tooling API, JavaScript layouts read workspace metadata, and Python layouts recognize eligible Python project metadata. Tools can therefore focus on their own analysis or generation work rather than on ecosystem-specific directory rules.

## Key Features

- Common API for project roots, modules, source roots, test roots, and documentation roots
- Layout detection for Maven, Gradle, JavaScript/TypeScript, and Python projects
- Filesystem-based default fallback for projects without a supported descriptor
- Maven module and metadata support
- Gradle child-project discovery through the Tooling API
- JavaScript workspace and Python project metadata support
- Recursive module processing for scanners and other repository tooling

## Usage

### Prerequisites

- Java 8 or later
- Maven 3.x or later to build the library or consume it from a Maven project
- Access to a Maven repository containing `org.machanism.machai:project-layout`, or a local build of this project

### Add the Dependency

Add Project Layout to the plugin, scanner, generator, or application that needs project-structure resolution:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>project-layout</artifactId>
  <version>1.4.1-SNAPSHOT</version>
</dependency>
```

For the snapshot version, publish it to an accessible snapshot repository or install it locally:

```bash
mvn clean install
```

### Detect and Use a Layout

Configure a target repository directory and let the layout manager select the appropriate implementation:

```java
File projectDirectory = new File("path/to/project");
ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDirectory);

for (String sourceRoot : layout.getSources()) {
    File sourceDirectory = new File(layout.getProjectDir(), sourceRoot);
    System.out.println(sourceDirectory);
}
```

### Typical Workflow

1. Add `project-layout` to the tool that needs to inspect a repository.
2. Identify the target project root.
3. Detect its layout with `ProjectLayoutManager`, or choose a specific layout implementation when appropriate.
4. Obtain module, source, test, and documentation roots from the layout.
5. Resolve the returned paths against the configured project root and use them for analysis, generation, validation, or indexing.
6. For multi-module projects, detect and process each module layout separately.

## Resources

- [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/project-layout)
- [Bindex metadata](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/project-layout/bindex.json)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Issue tracker](https://github.com/machanism-org/machai/issues)
