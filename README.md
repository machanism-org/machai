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

Machai is a modular toolkit for GenAI-enabled developer automation. It provides Java libraries, a command line application, and Maven plugins to integrate multiple GenAI providers through a single abstraction, generate and consume bindex metadata for library discovery and reuse, assemble projects using metadata-driven updates, and automate documentation updates from embedded guidance using Ghostwriter.

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | Utility library for describing and working with conventional project directory layouts (sources, resources, tests, docs, etc.) in a consistent way. It centralizes Maven-style folder conventions and provides utilities to resolve those paths relative to a project base directory. |
| [GenAI Client](genai-client/) | Java library for integrating Generative AI providers through a stable `GenAIProvider` abstraction, with prompt composition, optional file context, tool/function calling, and (provider-dependent) embeddings. |
| [Bindex Core](bindex-core/) | Foundational library and canonical model for bindex metadata. It supports generating, reading, validating, merging, and consuming bindex descriptors for library discovery and metadata-driven workflows. |
| [Machai CLI](cli/) | Spring Boot and Spring Shell command line application for bindex and Ghostwriter workflows (generate/update `bindex.json`, register metadata, semantic search, assemble projects, run guided doc tasks). |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and maintains `bindex.json` for the current Maven module, on demand or bound to the build lifecycle. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Maven plugin that applies structured, metadata-driven updates to assemble and evolve local Maven projects from bindex metadata, optionally assisted by GenAI semantic search. |
| [Ghostwriter](ghostwriter/) | Documentation automation engine that updates files based on embedded `@guidance` blocks plus optional instructions, with pluggable GenAI providers and CI-friendly execution. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that runs Ghostwriter-style documentation automation during a Maven build, producing or refreshing Maven Site Markdown pages from embedded `@guidance:` directives. |

## Installation Instructions

### Prerequisites

- Git
- Maven 3.6.0 or later
- Java 17

### Clone and build

```bash
git clone https://github.com/machanism-org/machai.git
cd machai
mvn -U clean install
```

To build and stage the Maven Site:

```bash
mvn clean install site site:stage
```

## Usage

### Build a specific module

```bash
mvn -pl genai-client clean install
```

### Run the Machai CLI

```bash
cd cli
mvn -Ppack package
java -jar target/machai.jar
```

Inside the shell, list available commands:

```text
help
```

### Run Ghostwriter (packaged)

```bash
cd ghostwriter
mvn -Ppack package
java -jar target/gw.jar --root ..
```

### Run Maven plugins

Run a plugin goal directly (example):

```bash
mvn org.machanism.machai:gw-maven-plugin:0.0.8-SNAPSHOT:gw
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
