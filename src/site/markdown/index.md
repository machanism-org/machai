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

Machai is a modular toolkit for GenAI-enabled developer automation. It provides Java libraries, a command line application, and Maven plugins to integrate multiple GenAI providers through a single abstraction, generate and consume bindex metadata for library discovery and reuse, assemble projects using metadata-driven updates, and automate documentation updates from embedded guidance using Ghostwriter.

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | Java library for detecting and describing a software project on-disk layout (main/test sources, resources, and child modules) across ecosystems such as Maven, Node workspaces, and Python. It provides normalized layout detection so higher-level tools can avoid hard-coded conventions and reliably enumerate source roots and subprojects. |
| [GenAI Client](genai-client/) | Java library that provides a small, stable provider abstraction (`GenAIProvider`) for interacting with multiple Generative AI backends. It supports prompt composition, optional file context, tool (function) calling, embeddings (provider-dependent), working-directory awareness, and optional input logging, allowing applications and other Machai modules to swap providers without vendor lock-in. |
| [Bindex Core](bindex-core/) | Core Java library for bindex metadata. It defines the canonical model and utilities to generate, read, validate, merge, and publish metadata so tools can discover libraries, aggregate module metadata in multi-module builds, and drive metadata-informed selection and project assembly workflows. |
| [Machai CLI](machai-cli/) | Command line application (Spring Shell) for generating and updating `bindex.json`, registering metadata to a database, picking libraries via semantic search, assembling projects from picked results, and running Ghostwriter guided file-processing workflows from the terminal. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and maintains `bindex.json` for the current module. It standardizes bindex metadata generation (and optional update of existing descriptors) so downstream discovery, indexing, and automation workflows can consume consistent metadata. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Maven plugin that assembles and evolves local Maven projects by applying reviewable updates driven by bindex metadata (for example, `bindex.json`). It can optionally use GenAI-assisted semantic search to recommend libraries, while keeping outputs local for inspection and commit. |
| [Ghostwriter](ghostwriter/) | Documentation automation engine and CLI that scans a project and updates documentation artifacts based on embedded `@guidance` blocks. It supports pluggable GenAI providers/models, additional instruction sources, directory exclusions, optional multi-threaded processing, and an optional final default guidance step to keep docs consistent and up to date. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that runs Ghostwriter-style documentation automation as part of a Maven build. It scans for embedded `@guidance:` directives and generates or refreshes Maven Site Markdown pages, enabling repeatable documentation updates in local builds and CI. |

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
