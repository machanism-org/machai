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
| [Project Layout](project-layout/) | Java API for detecting and describing a repository on-disk layout across ecosystems (Maven, Node workspaces, Python), including main/test/resources roots and child-module discovery, so higher-level tools can avoid hard-coded conventions. |
| [GenAI Client](genai-client/) | Java provider abstraction for interacting with multiple Generative AI backends via a single small API. Supports prompt composition, optional file context and tool calling, provider selection through a manager, and provider-dependent embeddings and logging for use across Machai modules. |
| [Bindex Core](bindex-core/) | Core library for bindex metadata: canonical model, generation, validation, and merge/aggregation utilities used by plugins and tooling. Enables metadata-driven discovery, selection, and assembly workflows across multi-module and dependency scenarios. |
| [Machai CLI](machai-cli/) | Spring Shell-based command line tool to generate and update bindex.json, register metadata, pick libraries using natural-language prompts and semantic search, assemble projects from picked results, and run Ghostwriter file-processing workflows from the terminal. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and maintains bindex.json for the current module (and optionally updates it), producing standardized metadata for downstream discovery and automation workflows. |
| [Assembly Maven Plugin](assembly-maven-plugin/) | Maven plugin that assembles and evolves projects by applying local, reviewable updates driven by bindex metadata (and optionally GenAI semantic search), speeding project bootstrapping while keeping changes under developer control. |
| [Ghostwriter](ghostwriter/) | CLI documentation engine that scans a project and updates documentation artifacts based on embedded @guidance blocks, with optional GenAI provider selection and multi-threaded processing for repeatable documentation maintenance in scripts and CI. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin wrapper around Ghostwriter-style documentation automation: scans for embedded @guidance directives and generates or refreshes Maven Site Markdown pages as part of the build, keeping docs consistent and aligned with the codebase. |

## Installation Instructions

### Prerequisites

- Git
- Maven 3.6.0 or later
- Java 17 (recommended for building all modules; Machai CLI requires Java 17)

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

### Typical workflows

Generate bindex metadata for a project directory:

```text
shell:> bindex --root /path/to/project
```

Pick libraries from a registry using a prompt:

```text
shell:> pick "Build a REST API for user login" --score 0.90
```

Assemble a project from the picked results:

```text
shell:> assembly --root /path/to/output --score 0.80
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
