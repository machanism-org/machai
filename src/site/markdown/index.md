<!-- @guidance:
Generate a content:
1. **Project Title and Overview:**  
   - Provide the project name and a brief description of its purpose and main features.
2. **Module List:**  
   Generate a table listing all modules in the project with the following columns:
   **Name**: Display the module name as a clickable link in the format `[name]([artifactId]/)`, the [name] and [artifactId] values should be obtained from the module pom.xml file.
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

Machai is a modular toolkit for GenAI-enabled developer automation, including semantic metadata indexing (*bindex*), project assembly, and documentation lifecycle management.

Key capabilities:

- **GenAI provider abstraction** for prompt/response flows, tool/function calling, file context, and embeddings.
- **Bindex metadata + semantic search** to describe, publish, discover, and pick libraries.
- **Automation tooling** via a CLI and Maven plugins for generating metadata, assembling projects, and generating/updating documentation.

## Modules

| Name | Description |
|---|---|
| [GenAI Client](genai-client/) | Java library that provides a single `GenAIProvider` abstraction for integrating with multiple Generative AI backends. It serves as the shared foundation for prompt execution, file-based context, tool/function calling, and embeddings across the Machai toolchain. |
| [Bindex Core](bindex-core/) | Foundational library for working with *bindex* metadata: generating metadata from projects, supporting library discovery and selection, and providing core assembly/dependency-resolution capabilities used by the CLI and Maven plugins. |
| [Machai CLI](machai-cli/) | Spring Shell-based command-line tool for running Machai workflows from the terminal: generate/update `bindex.json`, register metadata to a database, pick libraries via semantic search, assemble projects from picked libraries, process documents/files with GenAI, and clean `.machai` workspace folders. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and maintains `bindex.json` for Maven projects and can optionally register/publish metadata for discovery workflows, enabling metadata-driven automation (and, when configured, GenAI-assisted analysis). |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Maven plugin for metadata-driven (and optionally GenAI-assisted) project assembly. It selects libraries using bindex metadata and helps bootstrap a project by wiring dependencies and configuration based on the assembly requirements. |
| [Ghostwriter](ghostwriter/) | Documentation automation engine that scans project documents for embedded guidance tags and assembles consistent, up-to-date documentation using AI-powered synthesis, designed to integrate into Maven-based toolchains. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that runs Ghostwriter (typically as part of the Maven Site lifecycle) to scan, analyze, and generate/update documentation based on embedded guidance tags across modules. |

## Installation Instructions

### Prerequisites

- **Java 9+** (root build defaults to Java 9; some modules require newerfor example, the CLI uses Java 17)
- **Maven 3.6+**

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

### Run the CLI

Build and run the packaged CLI:

```bash
cd cli
mvn -Ppack package
java -jar target/machai.jar
```

Inside the shell, run:

```text
help
```

### Typical workflows

Generate `bindex.json` for a project directory:

```bash
java -jar cli/target/machai.jar bindex --dir /path/to/project
```

Pick libraries from a registry using a prompt:

```bash
java -jar cli/target/machai.jar pick "Build a REST API with PostgreSQL and Flyway" --score 0.80
```

Assemble a project from the picked results:

```bash
java -jar cli/target/machai.jar assembly --dir /path/to/output --score 0.80
```

### Using Maven plugins

Run a plugin goal directly (example):

```bash
mvn org.machanism.machai:gw-maven-plugin:0.0.2-SNAPSHOT:process
```

## Contributing

- Follow existing code style and conventions used in the repository.
- Keep changes focused and include tests where applicable.
- Use GitHub Issues for bug reports and feature requests: <https://github.com/machanism-org/machai/issues>
- Submit pull requests with a clear description, rationale, and (for fixes) reproduction steps.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE.txt](../../../LICENSE.txt).

## Contact and Support

- Website: <https://machai.machanism.org>
- Issues: <https://github.com/machanism-org/machai/issues>
- Maintainer: Viktor Tovstyi (<viktor.tovstyi@gmail.com>)
