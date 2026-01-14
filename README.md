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

Machai is a modular toolkit for GenAI-enabled developer automation, including semantic metadata indexing (bindex), project assembly, and documentation lifecycle management.

Key capabilities:

- GenAI provider abstraction for prompt/response flows, tool/function calling, file context, and embeddings.
- Bindex metadata plus semantic search to describe, publish, discover, and pick libraries.
- Automation tooling via a CLI and Maven plugins for generating metadata, assembling projects, and generating or updating documentation.

## Modules

| Name | Description |
|---|---|
| [GenAI Client](genai-client/) | Java library that provides a single `GenAIProvider` abstraction for integrating with multiple Generative AI backends. It serves as the shared foundation for prompt execution, file-based context, tool/function calling, and embeddings across the Machai toolchain. |
| [Bindex Core](bindex-core/) | Foundational library for working with bindex metadata: generating metadata from projects, supporting library discovery and selection, and providing core assembly and dependency-resolution capabilities used by the CLI and Maven plugins. |
| [Machai CLI](cli/) | Spring Shell-based command-line tool for running Machai workflows from the terminal: generate or update `bindex.json`, register metadata to a database, pick libraries via semantic search, assemble projects from picked libraries, process documents or files with GenAI, and clean `.machai` workspace folders. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and maintains `bindex.json` for Maven projects and can optionally register or publish metadata for discovery workflows, enabling metadata-driven automation (and, when configured, GenAI-assisted analysis). |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Maven plugin for metadata-driven (and optionally GenAI-assisted) project assembly. It selects libraries using bindex metadata and helps bootstrap a project by wiring dependencies and configuration based on the assembly requirements. |
| [Ghostwriter](ghostwriter/) | Documentation automation engine that scans project documents for embedded guidance tags and assembles consistent, up-to-date documentation using AI-powered synthesis, designed to integrate into Maven-based toolchains. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that runs Ghostwriter (typically as part of the Maven Site lifecycle) to scan, analyze, and generate or update documentation based on embedded guidance tags across modules. |

## Installation Instructions

### Prerequisites

- Java 9+ (root build defaults to Java 9; some modules require newer, for example the CLI uses Java 17)
- Maven 3.6+

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

Then use `help` to see available commands.

### Typical workflows

Generate `bindex.json` for a project directory:

```bash
java -jar cli/target/machai.jar bindex --dir /path/to/project
```

Pick libraries from a registry using a prompt:

```bash
java -jar cli/target/machai.jar pick "Build a REST API for user login via Commercetools" --score 0.90
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

Licensed under the Apache License, Version 2.0. See [LICENSE.txt](LICENSE.txt).

## Contact and Support

- Website: <https://machai.machanism.org>
- Issues: <https://github.com/machanism-org/machai/issues>
- Maintainer: Viktor Tovstyi (<viktor.tovstyi@gmail.com>)
