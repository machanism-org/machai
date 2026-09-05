<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/[artifactId].svg)](https://central.sonatype.com/artifact/org.machanism.machai/[artifactId])` and 
     [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/[artifactId]/bindex.json) in one line after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
- If used resources by uri: `src/site/resources/`, need to use project site location: `https://machai.machanism.org/[artifactId]/`.
-->

# Project Layout

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/project-layout.svg)](https://central.sonatype.com/artifact/org.machanism.machai/project-layout) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/project-layout/bindex.json)

Project Layout is a Java utility library that detects and describes conventional repository directory structures. It provides build tools, scanners, generators, documentation tools, and validation utilities with a common API for locating source code, tests, resources, documentation, and modules.

## Introduction

Project Layout is a Java utility library for describing, detecting, and working with conventional project directory layouts consistently. It gives build tooling, scanners, generators, validation utilities, and plugins a shared model for locating well-known folders such as production sources, test sources, resources, and documentation directories.

Instead of duplicating path conventions throughout each tool, Project Layout centralizes these rules behind reusable layout implementations. This improves maintainability, reduces configuration drift, and makes project-structure discovery easier to adapt across different technology stacks and repository styles.

The library supports Maven, Gradle, JavaScript, Python, and a filesystem-based default layout. Consumers can resolve important directories relative to a project root and focus on analysis, generation, indexing, or validation work.

## Usage

### Prerequisites

- Java 8 or later
- Maven 3.x or later for building and consuming the library
- Access to Maven Central or another repository that provides `org.machanism.machai:project-layout`
- A project directory whose structure needs to be resolved or analyzed

### Add the dependency

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>project-layout</artifactId>
  <version>1.4.0</version>
</dependency>
```

Project Layout is a library rather than an executable Maven plugin. Add it to a Maven plugin or another Maven project that needs project-structure resolution, then resolve a project layout through the common API:

```java
File projectDirectory = new File("path/to/project");
ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDirectory);
```

Project Layout does not provide a Maven goal of its own. Build and verify the library from its project root; a consuming plugin can then run its normal Maven goal:

```bash
mvn clean verify
```

### Typical workflow

1. Add `project-layout` as a dependency to the plugin, scanner, generator, or build tool that needs to inspect project structure.
2. Identify the target project root directory that should be analyzed.
3. Select an appropriate layout, such as `MavenProjectLayout`, `GradleProjectLayout`, `JScriptProjectLayout`, `PythonProjectLayout`, or `DefaultProjectLayout`, or delegate coordination to `ProjectLayoutManager`.
4. Resolve the relevant source, test, resource, and documentation paths through the selected layout abstraction.
5. Use the resolved paths for compilation support, static analysis, code generation, documentation publishing, validation, or project indexing.
6. Reuse the same layout model across tools to keep project-structure handling consistent and maintainable.

## Resources

- [Project site](https://machai.machanism.org/project-layout/)
- [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/project-layout)
- [Bindex metadata](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/project-layout/bindex.json)
- [GitHub repository](https://github.com/machanism-org/machai)
