<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/[artifactId].svg)](https://central.sonatype.com/artifact/org.machanism.machai/[artifactId])` after the title as a new paragraph.
   - Add [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/[artifactId]/bindex.json)
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

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/project-layout.svg)](https://central.sonatype.com/artifact/org.machanism.machai/project-layout)

[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/project-layout/bindex.json)

Project Layout is a Java utility library that detects and describes conventional repository directory structures. It provides build tools, scanners, generators, documentation tools, and validation utilities with a common API for locating source code, tests, resources, documentation, and modules.

## Introduction

Project Layout centralizes directory-layout conventions instead of requiring every tool to hard-code them. Its reusable layout implementations make project discovery consistent across repositories, reduce duplicated path logic, and help tooling remain maintainable as project structures vary.

The library supports Maven, Gradle, JavaScript/TypeScript, Python, and a filesystem-based default layout. Consumers can resolve important directories relative to a project root and focus on their analysis, generation, indexing, or validation work.

## Usage

### Prerequisites

- Java 8 or later
- Maven 3.x or later
- Access to Maven Central or another repository that provides `org.machanism.machai:project-layout`

### Add the dependency

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>project-layout</artifactId>
  <version>1.4.0</version>
</dependency>
```

Detect a layout from a project root, then use its root-relative paths:

```java
File projectDirectory = new File("path/to/project");
ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDirectory);

for (String sourceRoot : layout.getSources()) {
    File sourceDirectory = new File(layout.getProjectDir(), sourceRoot);
}
```

Build and verify this library from its project root:

```bash
mvn clean verify
```

### Typical workflow

1. Add `project-layout` to the tool that inspects repositories.
2. Pass the target repository root to `ProjectLayoutManager.detectProjectLayout`.
3. Use the detected layout's source, test, documentation, and module paths.
4. Resolve returned paths against `layout.getProjectDir()` before processing files.

## Resources

- [Project site](https://machai.machanism.org/project-layout/)
- [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/project-layout)
- [Bindex metadata](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/project-layout/bindex.json)
- [GitHub repository](https://github.com/machanism-org/machai)
