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
| [Project Layout](project-layout/) | Utility library for describing and working with conventional project directory layouts (sources, resources, tests, docs, etc.) in a consistent way, so tooling can locate well-known folders reliably. |
| [GenAI Client](genai-client/) | Java library that provides a stable GenAI provider abstraction and APIs for prompt composition (including file prompts), optional tool calling, optional attachments, embeddings, and optional input logging. |
| [Bindex Core](bindex-core/) | Core library for working with bindex metadata, including generating, reading, validating, merging, and aggregating descriptors to enable discovery and metadata-driven project assembly workflows. |
| [Machai CLI](cli/) | Spring Shell-based CLI for bindex and Ghostwriter workflows: generate/register bindex descriptors, semantic search for libraries, assemble projects, run guided file-processing tasks, and clean workspace artifacts. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and maintains bindex metadata (bindex.json) for a Maven module, producing machine-readable descriptors for indexing, discovery, and assembly tooling. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Maven plugin for assembling and evolving projects by applying structured, reviewable updates to the working tree using bindex metadata, with optional GenAI-powered semantic search to help select libraries. |
| [Ghostwriter](ghostwriter/) | Guidance-driven documentation engine and CLI that extracts embedded @guidance directives and uses a configured GenAI provider to synthesize and apply updates across repository artifacts. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that runs Ghostwriter guided processing as a Maven goal, supporting multi-module builds, exclude patterns, multi-threading, and optional input logging. |

## Installation Instructions

### Prerequisites

- Git
- Java 17 (recommended for building and running the full toolkit; some modules target Java 8)
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
java -jar target\gw.jar src\site\markdown
```

### Run Maven plugins

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
- Source repository: https://github.com/machanism-org/machai
- Issue tracker: https://github.com/machanism-org/machai/issues
- Maintainer: Viktor Tovstyi (viktor.tovstyi@gmail.com)
