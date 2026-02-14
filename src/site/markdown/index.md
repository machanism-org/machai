<!-- @guidance:
Generate a content:
1. **Project Title and Overview:**  
   - Provide the project name and a brief description of its purpose and main features.
2. **Module List:**  
   Generate a table listing all modules in the project with the following columns:
   **Name**: Display the module name as a clickable link in the format `[name]([artifactId]/)`, the [name] and [artifactId] values should be obtained from the module pom.xml file.
   **Description**: Provide a comprehensive description for each module, using the content from `[module_dir]/src/site/markdown/index.md`.
3. **Installation Instructions:**  
   - Describe how to clone the repository and build the project using Maven).
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
- Do not use UTF symbols in the content.
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

# Machai Project

Machai is a modular toolkit for GenAI-enabled developer automation. It provides Java libraries, a command line application, and Maven plugins to unify access to multiple GenAI providers, generate and consume bindex metadata for library discovery and reuse, assemble projects using metadata-driven updates, and automate documentation updates from embedded guidance using Ghostwriter.

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | Utility library for describing and resolving conventional project directory layouts (sources, resources, tests, docs) in a consistent way. It centralizes folder conventions so build tools and plugins can avoid hard-coded paths and duplicated logic, making layout-dependent automation more predictable across projects. |
| [GenAI Client](genai-client/) | Java library for integrating with multiple Generative AI providers behind a stable `GenAIProvider` abstraction. Provides provider resolution, prompt composition (including file prompts), optional attachments, optional tool/function calling, embeddings, and input logging to support semantic search and automation across Machai modules. |
| [Bindex Core](bindex-core/) | Foundational library for bindex metadata. Defines the canonical model and utilities to generate, read, validate, merge, and aggregate bindex metadata across modules and dependencies, enabling library discovery, registration, selection, and metadata-driven assembly workflows. |
| [Machai CLI](machai-cli/) | Command line application for bindex and Ghostwriter workflows. Generates and registers `bindex.json`, performs semantic search to pick libraries, assembles projects from picks, runs Ghostwriter guided file-processing tasks, and cleans workspace artifacts. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and maintains `bindex.json` for the current Maven module. Can run directly or be bound to the Maven lifecycle to produce consistent, machine-readable metadata used by downstream discovery, indexing, and assembly tooling. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Maven plugin that assembles and evolves Maven projects by applying structured, reviewable updates to the local working tree. Integrates libraries using bindex metadata and can optionally use GenAI-powered semantic search to help identify suitable libraries while keeping changes local for inspection and commit. |
| [Ghostwriter](ghostwriter/) | Documentation engine and CLI that scans a project, applies embedded `@guidance` directives, and uses GenAI to generate or update project files across source code, documentation, and site content. Enables repeatable runs to keep documentation aligned with in-repo guidance. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that integrates Ghostwriter guided file processing into Maven builds. Scans files for embedded `@guidance:` directives and generates or refreshes artifacts (including Maven Site pages) so documentation automation is repeatable locally and in CI, with both aggregator and reactor ordering options. |

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
mvn org.machanism.machai:gw-maven-plugin:0.0.10-SNAPSHOT:gw
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
