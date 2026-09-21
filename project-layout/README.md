<!-- @guidance: >>> ${guidances}/readme-content.md -->

# Project Layout

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/project-layout.svg)](https://central.sonatype.com/artifact/org.machanism.machai/project-layout) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/project-layout/refs/heads/main/bindex.json)

Project Layout is a Java utility library for describing, detecting, and working with conventional project directory layouts. It gives build tooling, repository scanners, generators, validators, documentation tooling, and indexers a shared way to locate sources, tests, resources, documentation, and modules across project ecosystems.

## Project Structure

The component design centers on a shared layout contract that exposes project roots, modules, source, test, and documentation roots, relative paths, exclusions, and temporary storage. A layout manager selects the first matching Maven, Gradle, JavaScript/TypeScript, Python, or fallback implementation from project markers, while a project processor detects layouts, recursively scans their modules, and delegates processing of leaf projects.

Specialized layouts preserve each ecosystem's discovery rules: Maven metadata is read through a dedicated model reader, Gradle child projects are loaded through the Tooling API, and JavaScript and Python metadata are inspected through JSON and TOML parsers. All layouts work with the project file system and emit diagnostics through SLF4J, so consumers can use one API across project ecosystems.

## Introduction

Build tooling often needs to locate sources, tests, resources, documentation, and modules, but hard-coding these conventions couples each tool to a particular ecosystem. Project Layout centralizes these conventions behind reusable layout implementations so tools can inspect diverse repositories through one API.

This approach reduces duplicated path-handling logic, configuration drift, and maintenance effort. It is suited to build plugins, repository scanners, code generators, documentation tooling, validation workflows, and indexers that must reliably work with different project structures.

## Overview

The library provides concrete strategies for Maven, Gradle, JavaScript, Python, and a default fallback project structure. `ProjectLayoutManager` detects and configures the first matching layout for a project root, while `ProjectProcessor` supports recursive processing of discovered modules.

Each implementation exposes project-relative paths through the common `ProjectLayout` abstraction. Maven layouts read Maven-model metadata, Gradle layouts use the Gradle Tooling API, JavaScript layouts read workspace metadata, and Python layouts recognize eligible Python project metadata. Tools can therefore focus on their own analysis or generation work rather than on ecosystem-specific directory rules.

## Key Features

- Common API for project roots, modules, source roots, test roots, resource roots, and documentation roots
- Layout detection for Maven, Gradle, JavaScript/TypeScript, and Python projects
- Filesystem-based default fallback for projects without a supported descriptor
- Maven module and metadata support through a dedicated model reader
- Gradle child-project discovery through the Tooling API
- JavaScript workspace and Python project metadata support
- Recursive module processing for scanners and other repository tooling

## Usage

### Prerequisites

- Java 8 or later
- Maven 3.x or later to build the library or consume it from a Maven project
- Access to Maven Central or another repository containing `org.machanism.machai:project-layout`
- A project directory whose structure needs to be resolved or analyzed

### Add the Dependency

Add Project Layout to the plugin, scanner, generator, or application that needs project-structure resolution:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>project-layout</artifactId>
  <version>1.4.2-SNAPSHOT</version>
</dependency>
```

Build and verify the library from the project root with Maven:

```bash
mvn clean verify
```

### Detect and Use a Layout

Configure a target repository directory and let the layout manager select the appropriate implementation:

```java
import java.io.File;
import java.io.FileNotFoundException;

import org.machanism.machai.project.ProjectLayoutManager;
import org.machanism.machai.project.layout.ProjectLayout;

File projectDirectory = new File("path/to/project");
try {
    ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDirectory);
    for (String sourceRoot : layout.getSources()) {
        File sourceDirectory = new File(layout.getProjectDir(), sourceRoot);
        System.out.println(sourceDirectory);
    }
} catch (FileNotFoundException e) {
    // Handle a project directory that does not exist.
}
```

### Typical Workflow

1. Add `project-layout` to the tool that needs to inspect a repository.
2. Identify the target project root.
3. Detect its layout with `ProjectLayoutManager`, or choose a specific layout implementation when appropriate.
4. Obtain module, source, test, resource, and documentation roots from the layout.
5. Resolve the returned paths against the configured project root and use them for analysis, generation, validation, or indexing.
6. For multi-module projects, detect and process each module layout separately.

## Resources

- [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/project-layout)
- [Bindex metadata](https://raw.githubusercontent.com/machanism-org/project-layout/refs/heads/main/bindex.json)
- [Machai Project Page](https://machai.machanism.org/project-layout)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Source repository](https://github.com/machanism-org/machai.git)
- [Issue tracker](https://github.com/machanism-org/machai/issues)
