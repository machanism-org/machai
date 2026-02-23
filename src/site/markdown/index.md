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

## Project structure

![](./images/project-structure.png)

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | Project Layout is a small utility library for describing and working with conventional project directory layouts (sources, resources, tests, docs, etc.) in a consistent way. It centralizes folder conventions so build tools and plugins can locate well-known directories reliably and avoid hard-coded paths or duplicated logic across projects. |
| [GenAI Client](genai-client/) | GenAI Client is a Java library for integrating applications with Generative AI providers via a single, consistent API. It provides prompt and instruction management, optional tool (function) calling, optional file attachments, and provider-dependent embeddings, enabling provider portability and a stable abstraction used across Machai modules. |
| [Bindex Core](bindex-core/) | Bindex Core is the foundational Java library for working with bindex metadata within the Machanism ecosystem. It defines the canonical metadata model and provides utilities and building blocks to generate, read, validate, merge, and aggregate bindex descriptors to support library discovery, selection, registration, and metadata-driven project assembly workflows. |
| [Machai CLI](machai-cli/) | Machai CLI is a Spring Shell-based command-line application for bindex and Ghostwriter workflows. It generates and registers bindex descriptors, performs semantic search to pick libraries, assembles project skeletons from selected libraries, runs Ghostwriter guided file-processing tasks, and cleans temporary `.machai` workspace artifacts. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Bindex Maven Plugin generates bindex metadata for Maven modules during the build lifecycle. It produces consistent machine-readable descriptors that can be indexed and searched, and it can optionally register or publish the generated metadata for use by discovery and assembly tooling in the Machanism ecosystem. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Assembly Maven Plugin automates project assembly by integrating libraries based on bindex metadata. It streamlines dependency selection and packaging by combining metadata-driven workflows with GenAI-powered semantic search, and it runs as a standard Maven plugin goal that can be invoked on demand or configured in `pom.xml`. |
| [Ghostwriter](ghostwriter/) | Ghostwriter is a guidance-driven documentation engine and CLI that scans project files (source code, docs, site content, and other artifacts), extracts embedded `@guidance:` directives, and uses a configured GenAI provider to synthesize and apply updates. It supports module-aware processing for Maven multi-module builds, pattern-based scanning, optional multi-threading, and optional logging of composed request inputs. |
| [GW Maven Plugin](gw-maven-plugin/) | GW Maven Plugin integrates Ghostwriter guided file processing into Maven builds. It scans project files for embedded `@guidance:` directives and runs the Ghostwriter pipeline as a Maven goal, supporting reactor-aware processing, optional multi-threading, exclude patterns, optional input logging, and optional credential loading from Maven `settings.xml`. |

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

```bat
mvn org.machanism.machai:gw-maven-plugin:0.0.11-SNAPSHOT:gw
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
