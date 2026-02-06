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
| [Project Layout](project-layout/) | Utility library for describing and working with conventional project directory layouts (sources, resources, tests, docs, etc.) in a consistent way. It centralizes Maven-style folder conventions and provides utilities to resolve those paths relative to a project base directory, helping build tools and plugins avoid hard-coded paths and duplicated path logic. |
| [GenAI Client](genai-client/) | Java library for integrating Generative AI providers through a stable `GenAIProvider` abstraction. It supports prompt composition from text and files, optional file context attachment, tool (function) calling, provider-dependent embeddings, optional request input logging, and working-directory aware execution so applications can swap or combine providers without vendor lock-in. |
| [Bindex Core](bindex-core/) | Provides the canonical model and core APIs for bindex metadata management. It supports generating, reading, validating, merging, and consuming bindex descriptors so downstream tools (CLI and Maven plugins) can discover libraries, register metadata, perform selection, and drive metadata-based project assembly workflows. |
| [CLI](cli/) | Spring Boot and Spring Shell command line application for bindex and Ghostwriter workflows. It can generate and update `bindex.json`, register metadata into a database, perform semantic search to pick libraries, assemble projects from picks, run Ghostwriter guided file-processing tasks, and clean local workspace artifacts. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Generates and maintains `bindex.json` for the current Maven module. It can be run on demand or bound to the Maven lifecycle to keep metadata aligned with the module, standardizing bindex metadata production for discovery, indexing, and automation tooling. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Automates assembling and evolving local Maven projects by applying structured, reviewable, metadata-driven updates to your working tree. It integrates libraries based on bindex metadata (for example, `bindex.json`) and can optionally use GenAI-powered semantic search to help identify suitable libraries while keeping changes local for inspection and commit. |
| [Ghostwriter](ghostwriter/) | CLI documentation engine that scans a project and updates documentation artifacts based on embedded `@guidance` blocks plus optional additional instructions. It supports pluggable GenAI providers, directory exclusions, optional multi-threaded processing, an optional final default guidance step, and is designed to run in scripts and CI. |
| [GW Maven Plugin](gw-maven-plugin/) | Runs Ghostwriter-style documentation automation as part of a Maven build. It scans for embedded `@guidance:` directives and generates or refreshes Maven Site Markdown pages so documentation updates are repeatable locally and in CI. |

## Installation Instructions

### Prerequisites

- Git
- Maven 3.6.0 or later
- Java 17

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

### Run the CLI

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
mvn org.machanism.machai:gw-maven-plugin:0.0.8-SNAPSHOT:gw
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
