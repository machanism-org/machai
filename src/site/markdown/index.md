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

Machai is a modular toolkit for GenAI-enabled developer automation. It provides Java libraries, command line tools, and Maven plugins to:

- Integrate with multiple GenAI providers through a single Java abstraction.
- Generate, register, and search structured library metadata (bindex) for discovery and reuse.
- Assemble Maven-based projects using metadata-driven, reviewable updates.
- Automate documentation updates from embedded guidance using Ghostwriter.

## Modules

| Name | Description |
|---|---|
| GenAI Client | Java library that provides a provider-agnostic `GenAIProvider` interface resolved by `GenAIProviderManager`. It supports prompts and instructions, optional tool (function) calling via Java handlers, provider-dependent file context, and embeddings. It includes multiple provider implementations (for example, OpenAI, CodeMie, Web UI automation, and a None provider) so applications and automation can switch backends without changing business logic. |
| Bindex Core | Core library for producing and consuming bindex metadata used across the Machai ecosystem. It provides APIs and utilities to generate metadata for Java artifacts, read and validate metadata, and merge metadata across modules and dependency graphs. It supports workflows such as library discovery, metadata publication, and metadata-informed selection and assembly driven by Maven-oriented tooling. |
| Machai CLI | Command line application (Spring Boot + Spring Shell) for end-to-end metadata and automation workflows. It can generate and update `bindex.json`, register metadata in a database, pick libraries using natural-language semantic search, assemble projects from picked results, run Ghostwriter file-processing workflows, and clean `.machai` workspace folders. It also supports configurable defaults such as working directory, GenAI model, and similarity score. |
| Bindex Maven Plugin | Maven plugin that generates and maintains a `bindex.json` descriptor for a Maven module and can optionally register or publish that metadata. It keeps structured library metadata in sync with the build so it can be used later for discovery, GenAI-powered semantic search, and downstream assembly workflows. |
| Assembly Maven Plugin | Maven plugin that applies structured, reviewable updates to a local Maven project to help bootstrap or evolve it. It can use bindex metadata (for example, `bindex.json`) and GenAI-assisted semantic discovery to recommend and integrate dependencies, accelerating setup while keeping changes inspectable and reproducible. |
| Ghostwriter | Documentation automation engine (also available as a runnable CLI JAR) that scans a project for embedded guidance and applies language- and format-specific reviewers (Java, Markdown, Python, TypeScript, HTML, text) to synthesize improved documentation with a configured GenAI provider. It supports local and CI usage, optional multi-threaded processing, and repeatable, script-friendly execution. |
| GW Maven Plugin | Maven plugin that integrates Ghostwriter into Maven builds to generate and update Maven Site documentation from embedded `@guidance:` directives. It enables repeatable documentation generation as part of the Maven lifecycle (for example, `site`), reducing documentation drift and keeping module documentation aligned with code and requirements. |

## Installation Instructions

### Prerequisites

- Git
- Maven 3.6.0 or later
- Java 9 or later for the multi-module build (some modules, such as the CLI, require Java 17)

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

### Build specific modules

Build a single module:

```bash
mvn -pl genai-client clean install
```

### Run the CLI

Build and run the packaged CLI:

```bash
cd cli
mvn -Ppack package
java -jar target/machai.jar
```

Inside the shell, list available commands:

```text
help
```

### Typical workflows

Generate `bindex.json` for a project directory:

```text
shell:> bindex --dir /path/to/project
```

Pick libraries from a registry using a prompt:

```text
shell:> pick "Build a REST API for user login" --score 0.90
```

Assemble a project from the picked results:

```text
shell:> assembly --dir /path/to/output --score 0.80
```

### Using Maven plugins

Run a plugin goal directly (example):

```bash
mvn org.machanism.machai:gw-maven-plugin:0.0.5-SNAPSHOT:gw
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
