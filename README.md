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
| [Project Layout](project-layout/) | Project Layout is a small utility library for describing and working with conventional project directory layouts (sources, resources, tests, docs, etc.) in a consistent way. It centralizes folder conventions so build tools and plugins can locate well-known directories reliably and avoid hard-coded paths or duplicated logic across projects. |
| [GenAI Client](genai-client/) | GenAI Client is a Java library that provides a stable `GenAIProvider` abstraction for integrating with multiple Generative AI providers through a consistent API. It supports prompt composition (including file prompts), optional attachments, optional tool (function) calling, provider-dependent embeddings, and optional input logging to enable semantic search, automation, and GenAI-driven workflows across Machai modules. |
| [Bindex Core](bindex-core/) | Bindex Core is the foundational Java library for working with bindex metadata in the Machanism ecosystem. It defines the canonical metadata model and provides utilities to generate, read, validate, merge, and aggregate bindex descriptors, enabling library discovery, selection, publishing/registration, and metadata-driven project assembly workflows. |
| [Machai CLI](cli/) | Machai CLI is a Spring Boot and Spring Shell command line application for bindex and Ghostwriter workflows. It generates and registers `bindex.json`, performs semantic search to pick libraries, assembles projects from selected libraries, runs Ghostwriter guided file-processing tasks, and cleans workspace artifacts. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Bindex Maven Plugin generates and maintains `bindex.json` for the current Maven module. It can be run on-demand or bound to the Maven lifecycle, producing consistent machine-readable metadata that downstream indexing, discovery, and assembly tooling can consume. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Assembly Maven Plugin assembles and evolves Maven projects by applying structured, reviewable updates to the local working tree. It integrates libraries using bindex metadata and can optionally use GenAI-powered semantic search to help identify suitable libraries while keeping changes local for inspection and commit. |
| [Ghostwriter](ghostwriter/) | Ghostwriter is a guidance-driven documentation engine and CLI that scans project files, extracts embedded `@guidance` directives, and uses a configured GenAI provider to synthesize and apply updates across source code, documentation, site content, configuration files, and other repository artifacts. It supports pattern-based scanning, module-aware processing for multi-module builds, optional multi-threading, and optional logging of composed request inputs. |
| [GW Maven Plugin](gw-maven-plugin/) | GW Maven Plugin integrates Ghostwriter guided file processing into Maven builds. It scans project files for embedded `@guidance:` directives and generates or refreshes artifacts (including Maven Site pages) so documentation automation is repeatable locally and in CI, supporting both aggregator and reactor-ordered execution strategies. |

## Installation Instructions

### Prerequisites

- Git
- Java 17
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
mvn org.machanism.machai:gw-maven-plugin:0.0.11-SNAPSHOT:gw
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
