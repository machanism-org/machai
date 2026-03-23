---
canonical: https://machai.machanism.org/index.html
---

<!-- @guidance:
Generate a content:
1. **Project Title and Overview:**  
   - Provide the project name and a brief description of its purpose and main features.
2. **Module List:**  
   Generate a table listing all modules in the project with the following columns:
   **Name**: Display the module name as a clickable link in the format `[name]([artifactId]/)`, the [name] and [artifactId] values should be obtained from the module pom.xml file.
   **Description**: Provide a comprehensive description for each module, using the content from `[module_dir]/src/site/markdown/index.md`.
   **Project structure**: 
   Describe the project with diagrams bellow:
     - Create a project structure overview based on the `.puml` files below.
     - Describe the project without including file names in the description.
     - Use the project structure diagram by the path: `./images/project-structure.png` (`src/site/puml/project-structure.puml`).
     - Use the project structure diagram by the path: `./images/machai-prj-c4-l2.png` (`src/site/puml/machai-prj-c4-l2.puml`).
     - Include this image in the section to visually represent the project structure.
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

Machai is a modular toolkit for GenAI-enabled developer automation. It provides Java libraries, a command line application, and Maven plugins that unify access to multiple GenAI providers, generate and consume Bindex metadata for library discovery and reuse, assemble projects using metadata-driven updates, and automate repository-scale documentation updates from embedded guidance using Ghostwriter.

Key capabilities include:

- Provider-agnostic GenAI access via a shared Java client.
- Bindex metadata generation, aggregation, and registration for library discovery.
- Prompt-driven project assembly using Bindex-informed semantic picking.
- Guidance-driven, repository-scale documentation and transformation using Ghostwriter.

## Project structure

These diagrams show the Machai multi-module layout and a higher-level view of how the main components relate.

![](./images/project-structure.png)

![](./images/machai-prj-c4-l2.png)

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | Project Layout is a Java utility library for describing and working with conventional project directory layouts (sources, resources, tests, docs) in a consistent way. It provides a model of a project layout and utilities for resolving layout paths relative to a project base directory, helping build tools and plugins avoid hard-coded paths, reduce duplicated logic, and behave consistently across projects. |
| [GenAI Client](genai-client/) | GenAI Client is a Java library that provides a single, consistent API for integrating with multiple Generative AI providers. It offers a provider abstraction, centralized provider selection, prompt/instruction composition, optional tool (function) calling, optional file attachments, provider-dependent embeddings, optional input logging, and working-directory awareness for automation-oriented workflows. |
| [Bindex Core](bindex-core/) | Bindex Core provides the core functionality for generating, registering, retrieving, and assembling Bindex documents, which are JSON descriptors for projects/libraries designed to be machine-readable and LLM-friendly. It supports creating and updating `bindex.json`, registering and searching Bindexes in a MongoDB-backed registry (including embeddings for semantic retrieval), semantically picking relevant libraries for a free-text query, expanding results via Bindex dependencies, and assembling selected Bindexes into prompt-ready context for downstream LLM workflows. |
| [Machai CLI](machai-cli/) | Machai CLI is a Spring Boot and Spring Shell command-line application that orchestrates Bindex and Ghostwriter workflows. It can generate and register Bindex metadata, perform semantic pick and project assembly, run guidance-driven Ghostwriter processing over a directory tree, execute reusable prompt templates (Acts), issue one-off prompts to a configured provider, manage persisted defaults via `machai.properties`, and clean temporary `.machai` folders. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Bindex Maven Plugin is a Maven plugin that generates, updates, and registers Bindex metadata for Maven projects. It provides goals to create/update a module Bindex, register it to an external registry endpoint for discovery, and clean temporary artifacts; it integrates with Machai GenAI configuration (provider/model) and can optionally load credentials from Maven `settings.xml`. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Assembly Maven Plugin is a Maven plugin that performs prompt-driven, metadata-assisted project assembly against the Maven execution base directory (`${basedir}`). It reads a natural-language prompt (default `project.txt` or interactive input), uses GenAI-backed semantic picking over Bindex metadata to recommend libraries above a score threshold, and runs an assembly phase to apply resulting project changes in-place using the selected Bindex entries. |
| [Ghostwriter](ghostwriter/) | Ghostwriter is a guidance-driven documentation and transformation engine (CLI and library) that scans project files, extracts embedded `@guidance:` directives, composes provider inputs with project structure context and optional system instructions, and applies AI-synthesized updates back to disk. It supports directory and pattern-based scanning (`glob:` and `regex:`), exclusions, optional multi-threading, default guidance when a file has no embedded directives, Act mode for reusable prompt templates, and optional request input logging. |
| [GW Maven Plugin](gw-maven-plugin/) | GW Maven Plugin is the primary Maven adapter for Ghostwriter. It integrates guidance-driven file processing into Maven builds by providing goals for aggregator-style and reactor-ordered execution, guided processing and Act mode, configurable scan roots and excludes, optional multi-threading and input logging, and optional credential loading from Maven `settings.xml` so Ghostwriter automation can run consistently in local and CI workflows. |

## Installation

### Prerequisites

- Git
- Java 17 recommended (some modules compile to Java 8)
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

### Run the Machai CLI

```bat
cd machai-cli
mvn -Ppack package
java -jar target\machai.jar
```

### Run Ghostwriter

```bat
cd ghostwriter
mvn -Ppack package
java -jar target\gw.jar src\site\markdown
```

### Run Maven plugins

Run the GW Maven Plugin goal:

```bat
mvn org.machanism.machai:gw-maven-plugin:1.0.2:gw
```

Run the Assembly Maven Plugin goal:

```bat
mvn org.machanism.machai:assembly-maven-plugin:1.0.2:assembly
```

## Contributing

- Follow the existing code style and conventions used in the repository.
- Keep changes focused and include tests where applicable.
- Use GitHub Issues for bug reports and feature requests: https://github.com/machanism-org/machai/issues
- Submit pull requests with a clear description, rationale, and (for fixes) reproduction steps.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE.txt](../../LICENSE.txt).

## Contact and support

- Website: https://machai.machanism.org
- Source repository: https://github.com/machanism-org/machai
- Issue tracker: https://github.com/machanism-org/machai/issues
- Maintainer: Viktor Tovstyi (viktor.tovstyi@gmail.com)
