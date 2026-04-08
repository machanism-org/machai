---
<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.

1. **Project Title and Overview:**  
   - Provide the project name and a brief description of its purpose and main features.
   - If a title or overview already exists, update it to ensure accuracy and completeness.

2. **Module List:**  
   - Generate a table listing all modules in the project with the following columns:
     - **Name**: Display the module name as a clickable link in the format `[name]([artifactId]/)`. Obtain `[name]` and `[artifactId]` from the module's `pom.xml` file.
     - **Description**: Provide a comprehensive description for each module, using content from `[module_dir]/src/site/markdown/index.md`.
   - If a module list already exists, update it to reflect any new, removed, or changed modules and descriptions.

3. **Project Structure:**  
   - Create a project structure overview based on the `.puml` files provided.
   - Do not include file names in the description.
   - Use the project structure diagram at `./images/project-structure.png` (`src/site/puml/project-structure.puml`).
   - Include this image in the section to visually represent the project structure.
   - If a project structure section already exists, update it with the latest diagram and description.

4. **Installation Instructions:**  
   - Describe how to clone the repository and build the project using Maven.
   - Include prerequisites such as Java version and build tools.
   - If installation instructions already exist, update them for accuracy and completeness.

5. **Usage:**  
   - Explain how to run or use the project and its modules.
   - Provide example commands or code snippets if applicable.
   - If a usage section already exists, update it with the latest information and examples.

6. **Contributing:**  
   - Outline guidelines for contributing to the project, including code style, pull request process, and issue reporting.
   - If a contributing section already exists, update it to reflect current guidelines.

7. **License:**  
   - State the project's license and provide a link to the license file.
   - If a license section already exists, update it to ensure it matches the current license.

8. **Contact and Support:**  
   - Include contact information or links for support and further questions.
   - If a contact or support section already exists, update it as needed.

**Formatting Requirements:**
- Do not use UTF symbols in the content.
- Use Markdown syntax for headings, lists, code blocks, and links).
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->
canonical: https://machai.machanism.org/index.html
---

# Machai Project

Machai is a multi-module toolkit for GenAI-enabled developer automation. It provides Java libraries and Maven tooling that unify access to multiple GenAI providers, generate and consume Bindex metadata for library discovery and reuse, and automate repository-scale documentation and file updates from embedded guidance using Ghostwriter.

Key capabilities include:

- Provider-agnostic GenAI access through a shared Java client.
- Bindex metadata generation, registration, semantic picking, and dependency-aware context assembly.
- Guidance-driven, repository-scale automation across code, documentation, site content, and other project files.
- Maven-native execution for guided and act-based workflows in multi-module builds.

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | Utility library for describing and working with conventional project directory layouts such as sources, resources, tests, and documentation in a consistent way. It helps tools and plugins resolve well-known folders reliably, avoid hard-coded paths, and reduce duplicated path logic across different project types. |
| [GenAI Client](genai-client/) | Java library for integrating with Generative AI providers through a shared, provider-agnostic API. It supports prompt composition, optional file attachments, optional tool calling, and embeddings so higher-level automation can interact with AI services without being tightly coupled to provider-specific SDKs. |
| [Bindex Core](bindex-core/) | Core Java library for Bindex metadata workflows. It supports generating and registering Bindex records, classifying natural-language requests into structured metadata filters, performing semantic library selection, expanding dependency context, and exposing these capabilities as tools for AI-assisted automation. |
| [Ghostwriter](ghostwriter/) | Guidance-driven documentation and repository automation engine available as both a CLI and a library. It scans project files, extracts embedded `@guidance` directives, composes model inputs with project context and optional instructions, and applies AI-generated updates back to source code, documentation, site content, and other repository artifacts. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin that integrates Ghostwriter into Maven builds. It provides guided and act-based goals, supports both aggregator-style and reactor-ordered execution, accepts configurable scan roots and excludes, can load credentials from Maven settings, and makes repository automation easier to run locally and in CI pipelines. |

## Project structure

The diagram summarizes the main modules and the way responsibilities are layered across the Machai platform.

![](./images/project-structure.png)

At a high level:

- Project Layout provides reusable project layout modeling utilities.
- GenAI Client provides the shared abstraction for working with GenAI providers.
- Bindex Core builds on GenAI Client to support Bindex metadata and semantic library discovery.
- Ghostwriter uses Project Layout and GenAI Client for guidance-driven repository automation, and can optionally include Bindex Core in packaged CLI distributions.
- GW Maven Plugin integrates Ghostwriter workflows into standard Maven execution.

## Installation

### Prerequisites

- Git
- Java 8 or later to build the modules, with Java 17 recommended for the full project toolchain and site workflows
- Maven 3.8.1 or later

### Clone and build

```bat
git clone https://github.com/machanism-org/machai.git
cd machai
mvn -U clean install
```

To build and stage the Maven site:

```bat
mvn clean install site site:stage
```

## Usage

### Build a specific module

```bat
mvn -pl genai-client clean install
```

### Run Ghostwriter CLI

```bat
cd ghostwriter
mvn -Ppack package
java -jar target\gw.jar src\site\markdown
```

### Run Maven plugin goals

Guided processing:

```bat
mvn org.machanism.machai:gw-maven-plugin:1.1.1-SNAPSHOT:gw -Dgw.scanDir=src\site
```

Act mode:

```bat
mvn org.machanism.machai:gw-maven-plugin:1.1.1-SNAPSHOT:act -Dgw.act="Rewrite headings for clarity" -Dgw.scanDir=src\site
```

### Use the libraries in Java projects

You can consume individual modules as Maven dependencies for project layout handling, GenAI integration, Bindex workflows, or guidance-driven repository automation.

## Contributing

- Follow the existing code style and repository conventions.
- Keep changes focused and add or update tests where applicable.
- Use GitHub Issues for bug reports and feature requests: https://github.com/machanism-org/machai/issues
- Submit pull requests with a clear summary, rationale, and reproduction details for fixes.
- Review generated documentation and site changes before opening a pull request.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE.txt](LICENSE.txt).

## Contact and support

- Website: https://machai.machanism.org
- Source repository: https://github.com/machanism-org/machai
- Issue tracker: https://github.com/machanism-org/machai/issues
- Maintainer: Viktor Tovstyi (viktor.tovstyi@gmail.com)
