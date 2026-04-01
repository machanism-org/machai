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

Machai is a multi-module toolkit for GenAI-enabled developer automation. It provides Java libraries and Maven tooling that unify access to multiple GenAI providers, generate and consume Bindex metadata for library discovery and reuse, and automate repository-scale documentation updates from embedded guidance using Ghostwriter.

Key capabilities include:

- Provider-agnostic GenAI access via a shared Java client.
- Bindex metadata generation, registration, semantic picking, and context assembly for library discovery.
- Guidance-driven, repository-scale documentation and transformation using Ghostwriter.

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | Utility library for describing and working with conventional project directory layouts (sources, resources, tests, docs) in a consistent way. It centralizes layout conventions so tools and plugins can resolve well-known folders reliably, avoid hard-coded paths, and reduce duplicated path logic. |
| [GenAI Client](genai-client/) | Java library for integrating with Generative AI providers using a consistent, provider-agnostic API. It provides prompt composition, optional file inputs, optional tool/function calling, and text embeddings to enable summarization, semantic search, and automation workflows while keeping calling code insulated from provider-specific SDK differences. |
| [Bindex Core](bindex-core/) | Core Java library for Bindex metadata workflows: generate and update bindex records, register Bindexes into a MongoDB-backed registry (optionally with embeddings), classify and semantically pick relevant libraries for a free-text query, expand results via declared dependencies, and assemble selected Bindexes into prompt-ready context. |
| [Ghostwriter](ghostwriter/) | Guidance-driven documentation and transformation engine (CLI and library) that scans project files, extracts embedded @guidance: directives, composes provider inputs with project context and optional system instructions, and applies AI-synthesized updates back to disk. It supports directory and pattern-based scanning (glob: and regex:), exclusions, optional multi-threading, Act mode for reusable prompt templates, and optional request input logging. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven adapter for Ghostwriter that integrates guidance-driven processing into Maven builds. It provides aggregator-style and reactor-ordered goals for guided processing and Act mode, configurable scan roots and excludes, optional multi-threading and input logging, and optional credential loading from Maven settings.xml for consistent local and CI automation. |

## Project structure

The diagram shows the Machai multi-module layout and the main dependency relationships between modules.

![](./images/project-structure.png)

At a high level:

- project-layout defines a reusable model for conventional project directory layouts.
- genai-client provides a provider-agnostic GenAI interface used by other components.
- bindex-core builds on project-layout and genai-client to manage Bindex metadata and semantic discovery.
- ghostwriter applies guidance-driven processing across repository files.
- gw-maven-plugin integrates Ghostwriter into Maven builds.

## Installation

### Prerequisites

- Git
- Java 17 recommended (some modules compile to Java 8 bytecode)
- Maven 3.6.0 or later

### Clone and build

```bat
git clone https://github.com/machanism-org/machai.git
cd machai
mvn -U clean install
```

To build and stage the Maven Site:

```bat
mvn clean install site site:stage
```

## Usage

### Build a specific module

```bat
mvn -pl genai-client clean install
```

### Run Ghostwriter

```bat
cd ghostwriter
mvn -Ppack package
java -jar target\\gw.jar src\\site\\markdown
```

### Run Maven plugin goals

Run guided processing using the GW Maven Plugin:

```bat
mvn org.machanism.machai:gw-maven-plugin:1.1.0-SNAPSHOT:gw
```

Run Act mode:

```bat
mvn org.machanism.machai:gw-maven-plugin:1.1.0-SNAPSHOT:act -Dgw.act="Rewrite headings for clarity" -Dgw.scanDir=src\\site
```

## Contributing

- Follow the existing code style and conventions used in the repository.
- Keep changes focused and include tests where applicable.
- Use GitHub Issues for bug reports and feature requests: https://github.com/machanism-org/machai/issues
- Submit pull requests with a clear description, rationale, and (for fixes) reproduction steps.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE.txt](../../LICENSE.txt).

## Contact and support

- Website: https://machai.machanism.org
- Source repository: https://github.com/machanism-org/machai
- Issue tracker: https://github.com/machanism-org/machai/issues
- Maintainer: Viktor Tovstyi (viktor.tovstyi@gmail.com)
