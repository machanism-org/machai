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

Machai is a modular toolkit for GenAI-enabled developer automation. It provides a Java GenAI provider abstraction, metadata indexing and semantic search (bindex), metadata-driven project assembly, and documentation lifecycle automation.

Key capabilities:

- GenAI provider abstraction for prompt and response workflows, tool or function calling, file-based context, and embeddings.
- Bindex metadata generation, registration, and semantic search to describe, publish, discover, and select libraries.
- Automation tooling via a CLI and Maven plugins for generating metadata, assembling projects, and generating or updating documentation.

## Modules

| Name | Description |
|---|---|
| [GenAI Client](genai-client/) | Java library that abstracts multiple Generative AI backends behind a single `GenAIProvider` interface. It supports prompt and response workflows, tool or function calling, file-based context, and embeddings, with provider selection via `GenAIProviderManager`. |
| [Bindex Core](bindex-core/) | Core library for producing and consuming bindex metadata. It provides APIs and reference implementations to generate metadata for Java projects, read and merge metadata across modules and dependencies, and integrate with Maven-oriented models and plugins. |
| [Machai CLI](machai-cli/) | Spring Shell-based command line interface for generating or updating `bindex.json`, registering metadata, performing semantic search (pick) with natural-language prompts, assembling projects from picked libraries, running Ghostwriter document processing, and cleaning `.machai` workspace folders. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and maintains a `bindex.json` descriptor for a Maven module and can optionally register or publish the descriptor when configured. It is commonly used to power metadata management, discovery, and automation workflows. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Maven plugin for bootstrapping and evolving Maven-based Java projects. It automates common setup tasks and assists with dependency selection and project structure assembly, optionally using GenAI and bindex metadata to produce reviewable and reproducible output. |
| [Ghostwriter](ghostwriter/) | Maven-friendly foundation for documentation automation and intelligent code generation. It supports template-driven generation and processing of project documentation, helping keep generated artifacts consistent and maintainable in standard developer toolchains. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that generates or updates Maven Site documentation from embedded `@guidance:` tags in source and documentation files, enabling consistent documentation refreshes as part of the Maven build. |

## Installation Instructions

### Prerequisites

- Java 9 or later for the full multi-module build (the root build defaults to Java 9; some modules such as the CLI require Java 17)
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

Generate bindex.json for a project directory:

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
