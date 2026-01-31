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

Machai is a modular toolkit for GenAI-enabled developer automation. It provides Java libraries, command line tools, and Maven plugins to:

- Integrate with multiple GenAI providers through a single Java abstraction.
- Generate, register, and search structured library metadata (bindex) for discovery and reuse.
- Assemble Maven-based projects using metadata-driven, reviewable updates.
- Automate documentation updates from embedded guidance using Ghostwriter.

## Modules

| Name | Description |
|---|---|
| [Project Layout](project-layout/) | Shared build and project-layout conventions used across the multi-module repository to keep module structure consistent and maintainable. |
| [GenAI Client](genai-client/) | Java library for integrating with Generative AI providers through a provider-agnostic API. It supports prompt and instruction management, optional file context, tool/function calling, and embeddings (provider-dependent), enabling AI-powered workflows such as semantic search, automated content generation, and intelligent project assembly while avoiding hard coupling to a single vendor. |
| [Bindex Core](bindex-core/) | Foundational library for producing and consuming bindex metadata in the Machanism ecosystem. It provides a stable data model and supporting utility APIs to generate, publish, discover, validate, and assemble metadata so build tools and integrations can automate dependency discovery and library assembly decisions. |
| [CLI](cli/) | Spring Boot + Spring Shell command line tool for end-to-end metadata and automation workflows. It can generate and update `bindex.json`, register metadata in a database, pick libraries using natural-language semantic search, assemble projects from picked results, run Ghostwriter file-processing workflows, and clean `.machai` workspace folders. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and maintains a `bindex.json` descriptor for a Maven module and can optionally register or publish that metadata. It keeps structured library metadata in sync with the build so it can be used later for discovery, GenAI-powered semantic search, and downstream assembly workflows. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Maven plugin that applies structured, reviewable updates to a local Maven project to help bootstrap or evolve it. It can use bindex metadata (for example, `bindex.json`) and GenAI-assisted semantic discovery to recommend and integrate dependencies, accelerating setup while keeping changes inspectable and reproducible. |
| [Ghostwriter](ghostwriter/) | Documentation automation engine (also available as a runnable CLI JAR) that scans a project for embedded guidance and applies language- and format-specific reviewers (Java, Markdown, Python, TypeScript, HTML, text) to synthesize improved documentation with a configured GenAI provider. It supports local and CI usage, optional multi-threaded processing, and repeatable, script-friendly execution. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that integrates Ghostwriter into Maven builds to generate and update Maven Site documentation from embedded `@guidance:` directives. It enables repeatable documentation generation as part of the Maven lifecycle (for example, `site`), reducing documentation drift and keeping module documentation aligned with code and requirements. |

## Installation Instructions

### Prerequisites

- Git
- Maven 3.6.0 or later
- Java 11 or later for most modules; the CLI module requires Java 17

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
