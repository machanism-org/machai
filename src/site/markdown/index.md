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

Machai is a multi-module toolkit for GenAI-enabled developer automation. It provides Java libraries, a command-line application, and Maven integration for provider-neutral GenAI access, Bindex-based library discovery, and guidance-driven repository automation across source code, documentation, project site content, configuration, diagrams, and other governed project files.

Key capabilities include:

- Provider-agnostic GenAI access through a shared Java client.
- Bindex metadata generation, registration, semantic picking, and dependency-aware context assembly.
- Guidance-driven automation across code, documentation, project site content, configuration, and other repository assets.
- Maven-native execution for guided and act-based workflows in single-module and multi-module builds.

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | Utility library for describing and resolving conventional project directory layouts such as source roots, test folders, resources, and documentation areas. It helps build tools, scanners, generators, and plugins work with consistent path conventions across Maven, Gradle, JavaScript, Python, and fallback project structures. |
| [GenAI Client](genai-client/) | Java library that provides a provider-neutral API for working with Generative AI services. It standardizes prompt execution, tool calling, file-aware requests, web search integration, MCP server definitions, embeddings, and usage tracking across supported providers such as OpenAI, Anthropic Claude, and EPAM CodeMie. |
| [Bindex Core](bindex-core/) | Core runtime for Bindex metadata workflows. It manages structured library descriptors, semantic recommendation, metadata registration, repository access, and AI-callable tools that support library discovery, project assembly, and metadata-backed automation. |
| [Machai MCP Server](machai-mcp-server/) | Standalone MCP server that exposes Machai AI capabilities over the Model Context Protocol via STDIO and HTTP transports. It uses Java SPI to discover and load functional tool implementations from the classpath at runtime, allowing custom tools to be added without modifying the core server. Supports integration with MCP-compatible clients such as Claude Desktop. |
| [MCP Server Maven Plugin](mcp-server-maven-plugin/) | Maven plugin that embeds and manages the Machai MCP Server lifecycle within Maven builds. It allows projects to start and stop the MCP server as part of the build process, enabling integration testing and tooling workflows that depend on a running MCP endpoint. |
| [Ghostwriter](ghostwriter/) | Repository-wide AI automation and documentation engine that scans project content for embedded `@guidance` directives and applies guided updates across source code, documentation, project site content, configuration, diagrams, and other relevant files. It also supports reusable act-based workflows with controlled tool access. |
| [GW Maven Plugin](gw-maven-plugin/) | Primary Maven adapter for Ghostwriter. It integrates guided and act-based automation into Maven execution, supports execution-root and per-module workflows, reads optional credentials from Maven settings, and makes repository-scale maintenance easier to run locally and in CI/CD pipelines. |

## Project structure

The diagram summarizes how the main modules are organized across the Machai platform.

![](./images/project-structure.png)

At a high level:

- Project Layout provides reusable project-structure modeling and path-resolution utilities.
- GenAI Client supplies the shared abstraction for interacting with supported GenAI providers.
- Bindex Core builds on GenAI capabilities to support metadata registration, recommendation, and library selection workflows.
- Ghostwriter uses project-layout and GenAI services to perform guidance-driven repository automation and reusable act execution.
- GW Maven Plugin integrates Ghostwriter workflows into standard Maven execution for single-module and multi-module projects.
- Machai MCP Server exposes Machai AI tools over the Model Context Protocol with support for STDIO and HTTP transports and SPI-based tool discovery.
- MCP Server Maven Plugin embeds the MCP server lifecycle into Maven builds for integration testing and tool-dependent workflows.

## Installation

### Prerequisites

- Git
- Java 8 or later, with Java 17 recommended for the broader build and site-generation toolchain
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
mvn org.machanism.machai:gw-maven-plugin:1.2.0-SNAPSHOT:gw -Dgw.path=src\site
```

Act mode:

```bat
mvn org.machanism.machai:gw-maven-plugin:1.2.0-SNAPSHOT:act -Dgw.act="Rewrite headings for clarity" -Dgw.path=src\site
```

### Use the libraries in Java projects

You can consume individual modules as Maven dependencies for project layout handling, GenAI integration, Bindex workflows, or repository-wide guidance-driven automation.

## Contributing

- Follow the existing repository structure, naming conventions, and code style.
- Keep changes focused and add or update tests where applicable.
- Use GitHub Issues for bug reports and feature requests: https://github.com/machanism-org/machai/issues
- Submit pull requests with a clear summary, rationale, and reproduction details for fixes.
- Review generated documentation and site changes before opening a pull request.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE.txt](../../LICENSE.txt).

## Contact and support

- Website: https://machai.machanism.org
- Source repository: https://github.com/machanism-org/machai
- Issue tracker: https://github.com/machanism-org/machai/issues
- Maintainer: Viktor Tovstyi (viktor.tovstyi@gmail.com)
