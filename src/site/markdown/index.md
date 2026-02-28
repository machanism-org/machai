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
    - Use the project structure diagram by the path: `./images/project-structure.png`.
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

Machai is a modular toolkit for GenAI-enabled developer automation. It provides Java libraries, a command line application, and Maven plugins that unify access to multiple GenAI providers, generate and consume bindex metadata for library discovery and reuse, assemble projects using metadata-driven updates, and automate documentation updates from embedded guidance using Ghostwriter.

Key capabilities include:

- Provider-agnostic GenAI access via a shared Java client.
- Bindex metadata generation, aggregation, and registration for library discovery.
- Prompt-driven project assembly using bindex-informed semantic picking.
- Guidance-driven, repository-scale documentation and transformation using Ghostwriter.

## Project structure

![](./images/project-structure.png)

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | Utility Java library for describing and working with conventional project directory layouts (sources, resources, tests, docs, and site content) in a consistent way. It centralizes folder conventions so build tools and plugins can resolve well-known paths reliably across projects and avoid hard-coded directories and duplicated logic. |
| [GenAI Client](genai-client/) | Java library that provides a single, consistent API for integrating with multiple Generative AI providers. It offers centralized provider selection, prompt and instruction management, optional tool (function) calling, optional file attachments, and provider-dependent embeddings, and is used as the GenAI abstraction across Machai modules. |
| [Bindex Core](bindex-core/) | Foundational Java library for working with bindex metadata in the Machanism ecosystem. It defines the canonical metadata model and provides utilities for generating, reading, validating, merging, and aggregating bindex descriptors to support discovery, selection, registration, and metadata-driven project assembly workflows. |
| [Machai CLI](machai-cli/) | Spring Boot and Spring Shell command-line application that orchestrates bindex and Ghostwriter workflows. It can generate and register bindex metadata, perform semantic library picking, assemble project skeletons from selected libraries, run Ghostwriter-guided processing over project files, and clean temporary `.machai` folders. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and optionally registers bindex metadata during the build. It derives consistent machine-readable descriptors from the Maven project model so artifacts can be indexed and discovered, and provides goals for creating, updating, cleaning, and registering bindex metadata. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Maven plugin that performs prompt-driven, metadata-assisted project assembly. It reads a prompt (default `project.txt`), uses GenAI-backed semantic picking over bindex entries to recommend libraries above a score threshold, and applies resulting changes directly to the Maven execution base directory (`${basedir}`) as part of a repeatable goal. |
| [Ghostwriter](ghostwriter/) | Guidance-driven documentation and transformation engine (CLI and library) that scans project files (source, docs, site content, and other artifacts), extracts embedded `@guidance:` directives, and uses a configured GenAI provider to synthesize and apply updates. It supports Maven multi-module traversal, directory and pattern-based scanning, optional multi-threading, and optional request input logging. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that integrates Ghostwriter Guided File Processing into Maven builds. It scans project files for embedded `@guidance:` directives and runs the Ghostwriter pipeline as a Maven goal, supporting reactor-aware processing, optional multi-threading, excludes, optional input logging, and optional credential loading from Maven `settings.xml`. |

## Installation

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

### Run the Machai CLI

```bat
cd cli
mvn -Ppack package
java -jar target\machai.jar
```

Inside the shell, list available commands:

```text
help
```

### Run Ghostwriter

```bat
cd ghostwriter
mvn -Ppack package
java -jar target\gw.jar src\site\markdown
```

### Run Maven plugins

Run the Ghostwriter Maven plugin goal:

```bat
mvn org.machanism.machai:gw-maven-plugin:0.0.13-SNAPSHOT:gw
```

Run the Assembly Maven plugin goal:

```bat
mvn org.machanism.machai:assembly-maven-plugin:0.0.13-SNAPSHOT:assembly
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
