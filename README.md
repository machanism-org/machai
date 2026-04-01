<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
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

Machai is a modular toolkit for GenAI-enabled developer automation. It provides Java libraries, a command line application, and Maven plugins that unify access to multiple GenAI providers, generate and consume Bindex metadata for library discovery and reuse, and automate repository-scale documentation updates from embedded guidance using Ghostwriter.

Key capabilities include:

- Provider-agnostic GenAI access via a shared Java client.
- Bindex metadata generation, registration, semantic picking, and context assembly for library discovery.
- Guidance-driven, repository-scale documentation and transformation using Ghostwriter.

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | Utility library for describing and working with conventional project directory layouts (sources, resources, tests, docs, etc.) in a consistent way. |
| [GenAI Client](genai-client/) | Java library that provides a provider-agnostic API for Generative AI integrations, including prompt composition, optional file inputs, optional tool/function calling, and provider-dependent embeddings. |
| [Bindex Core](bindex-core/) | Core Java library for Bindex metadata workflows: generate/update `bindex.json`, register Bindexes into a MongoDB-backed registry, semantically pick relevant libraries, expand results via dependencies, and assemble selected Bindexes into prompt-ready context. |
| [Machai CLI](machai-cli/) | Spring Boot and Spring Shell command-line application that orchestrates Bindex and Ghostwriter workflows: generate/register Bindexes, perform pick and assembly, run guidance-driven Ghostwriter processing, execute reusable prompt templates (Acts), manage defaults via `machai.properties`, and clean temporary `.machai` folders. |
| [Ghostwriter](ghostwriter/) | Guidance-driven documentation and transformation engine (CLI and library) that scans files, extracts embedded `@guidance:` directives, and applies AI-synthesized updates across repository artifacts. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that integrates Ghostwriter guided file processing into Maven builds, supporting reactor-aware processing, optional multi-threading, excludes, optional input logging, and optional credential loading from Maven `settings.xml`. |

## Installation Instructions

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
java -jar target\\machai.jar
```

Inside the shell, list available commands:

```text
help
```

### Run Ghostwriter

```bat
cd ghostwriter
mvn -Ppack package
java -jar target\\gw.jar src\\site\\markdown
```

### Run Maven plugins

Run the GW Maven Plugin goal:

```bat
mvn org.machanism.machai:gw-maven-plugin:1.0.4-SNAPSHOT:gw
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
