<!--
@guidance:
**ADD FOLLOWING SECTIONS TO THIS README FILE:**
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src\site\markdown\index.md` content summary.
   - Add `![](src/site/resources/images/machai-logo.png)` before the title.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)` after the title as a new paragraph.
2. **Module List:**  
   - List all modules in the project.
   - For each module, include its name, a short description, and a link to its module
3. **Installation Instructions:**  
   - Describe how to clone the repository and build the project (e.g., using Maven or Gradle).
   - Include prerequisites such as Java version and build tools.
4. **Usage:**  
   - Explain how to run or use the project and its modules.
   - Provide example commands or code snippets if applicable.
5. **Contributing:**  
   - Outline guidelines for contributing to the project, including code style, pull request process, and issue reporting.
6. **License:**  
   - State the project's license and provide a link to the license file.
7. **Contact and Support:**  
   - Include contact information or links for support and further questions.
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

![](src/site/resources/images/machai-logo.png)

# Machai Project

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

Machai is a modular toolkit for GenAI-enabled developer automation. It provides Java libraries, a command line application, and Maven plugins to unify access to multiple GenAI providers, generate and consume bindex metadata for library discovery and reuse, assemble projects using metadata-driven updates, and automate documentation updates from embedded guidance using Ghostwriter.

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | Project Layout is a small utility library for describing and working with conventional project directory layouts (sources, resources, tests, docs, etc.) in a consistent way. It provides a centralized representation of common project folders and utilities to resolve them relative to a project base directory, helping tools and plugins avoid hard-coded paths, reduce duplicated logic, and behave consistently across projects. |
| [GenAI Client](genai-client/) | GenAI Client is a Java library for integrating with Generative AI providers through a small, stable API (`GenAIProvider`). It provides prompt and instruction management, optional tool/function calling, optional file context, and provider-dependent embeddings. Provider implementations are resolved via `GenAIProviderManager`, enabling portability across backends by changing configuration rather than application code. |
| [Bindex Core](bindex-core/) | Bindex Core (`bindex-core`) is the foundational Java library for working with bindex metadata. It defines the canonical model and provides APIs to generate, read, validate, merge, and aggregate metadata across modules and dependencies. This standardized handling enables consistent discovery, integration, and assembly workflows in downstream tooling. |
| [Machai CLI](cli/) | Machai CLI is a Spring Boot and Spring Shell command line application for bindex and Ghostwriter workflows. It can generate and update `bindex.json`, register metadata into a database, perform semantic search to pick libraries, assemble projects from picks, run Ghostwriter guided file-processing tasks, and clean local workspace artifacts. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Bindex Maven Plugin generates and maintains `bindex.json` for the current Maven module. It can be run on demand or bound to the Maven lifecycle to keep metadata synchronized with the module, standardizing bindex metadata production for discovery, indexing, and automation workflows. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Assembly Maven Plugin assembles and evolves Maven projects by applying structured, reviewable updates directly to your local working tree. It integrates libraries using bindex metadata (for example, `bindex.json`) and can optionally use GenAI-powered semantic search to help identify and select suitable libraries, while keeping changes local for inspection and commit. |
| [Ghostwriter](ghostwriter/) | Ghostwriter is a documentation engine that scans a project, applies embedded `@guidance` constraints, and uses GenAI to generate or update project files. It supports working across source code, documentation, site content, and other project assets, enabling repeatable runs to keep documentation aligned with in-repo guidance. |
| [GW Maven Plugin](gw-maven-plugin/) | GW Maven Plugin runs Ghostwriter-style documentation automation as part of a Maven build. It scans for embedded `@guidance:` directives and generates or refreshes Maven Site Markdown pages so documentation updates are repeatable locally and in CI. |

## Installation Instructions

### Prerequisites

- Git
- Java 17 (recommended for building the full multi-module project)
- Maven 3.6.0 or later

### Clone and build

```bat
git clone https://github.com/machanism-org/machai.git
cd machai
mvn -U clean install
```

To build and stage the Maven Site:

```bat
mvn clean install site site:stage
```

## Usage

### Build a specific module

```bat
mvn -pl genai-client clean install
```

### Run the Machai CLI (packaged)

```bat
cd cli
mvn -Ppack package
java -jar target\machai.jar
```

Inside the shell, list available commands:

```text
help
```

### Run Ghostwriter (packaged)

```bat
cd ghostwriter
mvn -Ppack package
java -jar target\gw.jar --root ..
```

### Run Maven plugins

Run a plugin goal directly (example):

```bat
mvn org.machanism.machai:gw-maven-plugin:0.0.9-SNAPSHOT:gw
```

## Contributing

- Follow the existing code style and conventions used in the repository.
- Keep changes focused and include tests where applicable.
- Use GitHub Issues for bug reports and feature requests: https://github.com/machanism-org/machai/issues
- Submit pull requests with a clear description, rationale, and (for fixes) reproduction steps.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE.txt](LICENSE.txt).

## Contact and Support

- Website: https://machai.machanism.org
- Issues: https://github.com/machanism-org/machai/issues
- Maintainer: Viktor Tovstyi (viktor.tovstyi@gmail.com)
