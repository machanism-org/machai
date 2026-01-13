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
   - State the project’s license and provide a link to the license file.
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

Machai is a modular, AI-driven toolkit for automating software project setup, semantic indexing, build orchestration, and documentation lifecycle management. It provides:

- A Java GenAI provider abstraction (`genai-client`) used across tooling.
- A metadata and semantic indexing foundation (`bindex-core`) for describing and discovering libraries.
- Developer-facing automation via a CLI and Maven plugins for generating metadata, assembling projects, and generating/updating documentation.

## Modules

| Name | Description |
|---|---|
| [GenAI Client](genai-client/) | A Java abstraction layer over multiple Generative AI backends through a single `GenAIProvider` interface. It supports LLM workflows (prompt/response), tool/function calling, file-based context, and embeddings, and includes implementations such as `OpenAIProvider`, `NoneProvider` (no-op/logging), and `WebProvider` (UI/driver-based automation). |
| [Bindex Core](bindex-core/) | Core library for generating and managing *bindex* metadata, including metadata generation, assembly and dependency resolution, and integrations with Maven models and plugin APIs. It underpins library discovery and automated module organization in the Machanism ecosystem. |
| [Machai CLI](cli/) | A Spring Shell-based command-line tool to generate/update `bindex.json`, register metadata to a database, pick libraries via natural-language semantic search, assemble projects from picked libraries, process documents/files with GenAI, and clean `.machai` temporary folders. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | A Maven plugin that generates and maintains `bindex.json` for Maven projects, can update existing descriptors, and can optionally register/publish metadata for discovery workflows. It enables metadata-driven automation and (when enabled) GenAI-assisted analysis. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | A Maven plugin for AI-assisted assembly and initialization of Java projects. It analyzes requirements, recommends libraries via semantic search using bindex metadata, and automates project bootstrapping (dependencies and configuration) with customizable parameters. |
| [Ghostwriter](ghostwriter/) | A documentation automation and intelligent code/documentation generation engine that scans and assembles documentation using embedded guidance tags and AI-powered synthesis, designed to integrate into Maven-based toolchains. |
| [GW Maven Plugin](gw-maven-plugin/) | A Maven plugin that runs Ghostwriter as part of the Maven Site lifecycle to scan, analyze, and generate/update documentation based on embedded guidance tags, keeping project documentation consistent and up-to-date. |

## Installation Instructions

### Prerequisites

- Java 9+ (project default in the root `pom.xml`; some modules may require newer, for example `machai-cli` uses Java 17)
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
java -jar cli/target/machai.jar pick "Build a REST API with PostgreSQL and Flyway" --score 0.80
```

Assemble a project from the picked results:

```bash
java -jar cli/target/machai.jar assembly --dir /path/to/output --score 0.80
```

### Using Maven plugins

Add the required plugin to your `pom.xml` and run it by goal, for example:

```bash
mvn org.machanism.machai:gw-maven-plugin:0.0.2-SNAPSHOT:process
```

## Contributing

- Use the existing formatting and naming conventions found in the codebase.
- Keep changes focused and include tests where applicable.
- Open a pull request with a clear description, rationale, and reproduction steps for fixes.
- Report bugs and request features via GitHub Issues: <https://github.com/machanism-org/machai/issues>

## License

Licensed under the Apache License, Version 2.0. See [LICENSE.txt](LICENSE.txt).

## Contact and Support

- Website: <https://machai.machanism.org>
- Issues: <https://github.com/machanism-org/machai/issues>
- Maintainer: Viktor Tovstyi (<viktor.tovstyi@gmail.com>)
