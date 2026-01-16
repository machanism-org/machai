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

Machai is a modular toolkit for GenAI-enabled developer automation. It combines a Java GenAI provider abstraction (GenAI Client), metadata indexing and semantic search (bindex), metadata-driven project assembly, and documentation lifecycle automation (Ghostwriter).

Key capabilities:

- GenAI provider abstraction for prompt and response workflows, tool or function calling, file-based context, and embeddings.
- Bindex metadata generation, registration, and semantic search to describe, publish, discover, and select libraries.
- Automation tooling via a CLI and Maven plugins for generating metadata, assembling projects, and generating or updating documentation.

## Modules

| Name | Description |
|---|---|
| [GenAI Client](genai-client/) | Java abstraction layer over multiple Generative AI backends exposed via a single provider interface. Designed for integrating prompt and response flows, tool (function) calling, file-based context, and embeddings into Java applications and automation. Includes provider implementations for OpenAI, web-driven integrations, and a None provider for offline or disabled environments. |
| [Bindex Core](bindex-core/) | Foundational library for producing and consuming bindex metadata. Provides APIs and reference implementations to generate metadata for Java artifacts, read and merge metadata across modules and dependencies, and integrate with Maven-oriented models and plugins. |
| [Machai CLI](machai-cli/) | Spring Boot and Spring Shell command line tool for generating or updating `bindex.json`, registering metadata, picking libraries from a metadata database using a natural-language prompt, assembling projects from picked libraries, running Ghostwriter file-processing workflows, and cleaning `.machai` temporary folders. It also supports configuring default working directory, GenAI model, and similarity score. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and maintains a `bindex.json` descriptor for a Maven module, with optional registration and publishing. Enables metadata management and discovery workflows used by Machai tooling. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Maven plugin for bootstrapping and evolving Maven-based Java projects by automating common setup tasks and assisting with dependency selection. Can integrate with Machai metadata (for example `bindex.json`) and GenAI-powered semantic search, producing reviewable and reproducible project structure and configuration updates. |
| [Ghostwriter](ghostwriter/) | Maven-friendly foundation for document automation and intelligent code generation. Supports template-driven, repeatable workflows that fit standard Maven builds and Maven Site layouts, helping teams keep generated artifacts consistent and maintainable. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that generates and updates Maven Site documentation using embedded `@guidance:` comments found across a project, keeping documentation synchronized with code and requirements as a repeatable part of the build. |

## Installation Instructions

### Prerequisites

- Java 9 or later for the multi-module build (root build defaults to Java 9; some modules such as the CLI require Java 17)
- Maven 3.6.0 or later
- Git

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
mvn org.machanism.machai:gw-maven-plugin:0.0.2-SNAPSHOT:gw
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
