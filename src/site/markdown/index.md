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
| [Project Layout](project-layout/) | Project Layout is a small utility library for describing and working with conventional project directory layouts (sources, resources, tests, docs, etc.) in a consistent way. It provides a standardized representation of common Maven-style folders and utilities to resolve those paths relative to a project base directory, helping build tooling avoid hard-coded paths and duplicated layout logic. |
| [GenAI Client](genai-client/) | GenAI Client is a Java library that provides a small, stable abstraction (`GenAIProvider`) for integrating with multiple Generative AI backends. It supports prompt composition, optional tool/function calling, optional file context, provider-dependent embeddings, and provider selection via `GenAIProviderManager` so applications can change providers by configuration rather than code. |
| [Bindex Core](bindex-core/) | Bindex Core (`bindex-core`) is the foundational Java library for bindex metadata. It defines the canonical model and provides APIs and utilities to generate, read, validate, merge, and aggregate metadata across modules and dependencies, enabling consistent library discovery, selection, and metadata-driven assembly workflows. |
| [Machai CLI](machai-cli/) | Machai CLI is a Spring Boot and Spring Shell command line application for bindex and Ghostwriter workflows. It can generate and update `bindex.json`, register metadata into a database, perform semantic search to pick libraries, assemble projects from picks, run Ghostwriter guided file-processing tasks, and clean local workspace artifacts. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Bindex Maven Plugin generates and maintains `bindex.json` for the current Maven module. It can be run on demand or bound to the Maven lifecycle to keep metadata synchronized with the module, standardizing bindex metadata production for discovery, indexing, and automation workflows. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Assembly Maven Plugin assembles and evolves Maven projects by applying structured, reviewable updates directly to your local working tree. It integrates libraries using bindex metadata (for example, `bindex.json`) and can optionally use GenAI-powered semantic search to help identify and select suitable libraries, while keeping changes local for inspection and commit. |
| [Ghostwriter](ghostwriter/) | Ghostwriter is a documentation engine that scans a project, applies embedded `@guidance` constraints, and uses GenAI to generate or update project files. It supports working across source code, documentation, site content, and other project assets, enabling repeatable runs to keep documentation aligned with in-repo guidance. |
| [GW Maven Plugin](gw-maven-plugin/) | GW Maven Plugin integrates Ghostwriter guided file processing into Maven builds. It scans project files for embedded `@guidance:` directives and generates or refreshes artifacts (including Maven Site Markdown pages) so documentation automation is repeatable locally and in CI, with both CLI-like reverse ordering and Maven reactor ordering options. |

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
