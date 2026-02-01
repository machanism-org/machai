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

# Machai

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

Machai is a modular toolkit for GenAI-enabled developer automation. It provides Java libraries, a command line application, and Maven plugins to integrate multiple GenAI providers through a single abstraction, generate and consume bindex metadata for library discovery and reuse, assemble projects using metadata-driven updates, and automate documentation updates from embedded guidance using Ghostwriter.

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | Java library for detecting and describing a repository on-disk layout across ecosystems (Maven, Node workspaces, Python), including main/test/resource roots and child-module discovery. |
| [GenAI Client](genai-client/) | Java provider abstraction for interacting with multiple Generative AI backends via a single small API, including prompt composition, optional file context, tool calling, embeddings, and input logging. |
| [Bindex Core](bindex-core/) | Core library for bindex metadata: canonical model, generation, validation, and merge/aggregation utilities used by plugins and tooling to enable metadata-driven discovery and assembly workflows. |
| [CLI](cli/) | Command line tool to generate and update bindex.json, register metadata, pick libraries using semantic search, assemble projects from picked results, and run Ghostwriter workflows from the terminal. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and maintains bindex.json for the current module, producing standardized metadata for downstream discovery and automation workflows. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Maven plugin that assembles and evolves projects by applying local, reviewable updates driven by bindex metadata and optionally GenAI-assisted library selection. |
| [Ghostwriter](ghostwriter/) | Documentation automation engine that scans a project and updates documentation artifacts based on embedded guidance blocks, with optional GenAI provider selection and optional multi-threaded processing. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that runs Ghostwriter-style documentation automation during the build by scanning for embedded guidance directives and generating or refreshing Maven Site Markdown pages. |

## Installation Instructions

### Prerequisites

- Git
- Maven 3.6.0 or later
- Java 17 (recommended to build all modules)

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
mvn org.machanism.machai:gw-maven-plugin:0.0.7-SNAPSHOT:gw
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
