<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
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

# Machai Project

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai)

Machai is a multi-module Java toolkit for GenAI-enabled developer automation. It provides provider-neutral GenAI access, embeddings, Bindex library discovery, an MCP server, Maven integrations, and Ghostwriter workflows for repeatable, maintainable AI-assisted development across source code, tests, documentation, site content, configuration, diagrams, and other project files.

Key capabilities include:

- Provider-neutral prompt execution, embeddings, usage tracking, web search, MCP server definitions, and AI-callable Java tools.
- Bindex metadata retrieval and registration, semantic library recommendation, classification, embeddings, and persistence.
- Guidance-driven automation across code, documentation, project site content, configuration, diagrams, and other repository assets.
- Maven-native execution for guided, act-based, and MCP server workflows in single-module and multi-module builds.
- MCP-compatible tool exposure through a standalone server with STDIO and HTTP transports.

## Modules

| Module | Description |
| --- | --- |
| [Project Layout](project-layout/) | Utility library for describing and resolving conventional project directories, including production and test sources, resources, and documentation, across Maven, Gradle, JavaScript, Python, and fallback layouts. |
| [GenAI Client](genai-client/) | Provider-neutral Java library for generative AI integrations, including prompt execution, embeddings, provider resolution, usage tracking, web search, MCP servers, and registration of Java methods as AI-callable tools and prompts. |
| [Machai MCP Server](machai-mcp-server/) | Java 17 Model Context Protocol server that exposes Machai functional tools and prompts over STDIO or HTTP, including stateless and streamable HTTP modes. |
| [MCP Server Maven Plugin](mcp-server-maven-plugin/) | Maven plugin that launches the Machai MCP Server for a Maven project over HTTP; its stateless and streamable goals provide project metadata, parameters, tools, and the project directory to the server. |
| [Bindex Core](bindex-core/) | Core services for Bindex metadata retrieval, registration, semantic library recommendation, classification, embeddings, and MongoDB-backed persistence for Ghostwriter, Maven, MCP, and AI-assisted project-assembly workflows. |
| [Ghostwriter](ghostwriter/) | Documentation engine and command-line processor that scans, analyzes, and assembles project documentation with embedded guidance tags and AI-powered synthesis; its pack profile also provides Bindex integration for repository-wide workflows. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven plugin for Ghostwriter automation that scans and updates project documentation using guidance tags and AI-powered synthesis, helping keep documentation consistent across Maven modules. |

## Installation

### Prerequisites

- Git
- JDK 17 or later to build the complete reactor. Project Layout, GenAI Client, Ghostwriter, and GW Maven Plugin target Java 8 bytecode, while the MCP server, MCP Server Maven Plugin, and Bindex Core require Java 17.
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

Build a module and its required dependencies:

```bat
mvn -pl ghostwriter -am clean install
```

### Run Ghostwriter CLI

```bat
cd ghostwriter
mvn -Ppack package
java -jar target\gw.jar src\site\markdown
```

### Run Ghostwriter Maven plugin goals

Guided processing:

```bat
mvn org.machanism.machai:gw-maven-plugin:1.4.1-SNAPSHOT:gw -Dgw.path=src\site
```

Act mode:

```bat
mvn org.machanism.machai:gw-maven-plugin:1.4.1-SNAPSHOT:act -Dgw.act="Rewrite headings for clarity" -Dgw.path=src\site
```

### Run MCP server Maven plugin goals

Start the Machai MCP Server from Maven when a build or integration workflow needs an MCP endpoint:

```bat
mvn org.machanism.machai:mcp-server-maven-plugin:1.4.1-SNAPSHOT:start
```

Stop the managed server when the workflow is complete:

```bat
mvn org.machanism.machai:mcp-server-maven-plugin:1.4.1-SNAPSHOT:stop
```

### Use the libraries in Java projects

You can consume individual modules as Maven dependencies for project layout handling, GenAI integration, Bindex workflows, MCP tool serving, or repository-wide guidance-driven automation.

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>genai-client</artifactId>
  <version>1.4.1-SNAPSHOT</version>
</dependency>
```

## Contributing

- Follow the existing repository structure, naming conventions, and code style.
- Keep changes focused and add or update tests where applicable.
- Use GitHub Issues for bug reports and feature requests: https://github.com/machanism-org/machai/issues
- Submit pull requests with a clear summary, rationale, and reproduction details for fixes.
- Review generated documentation and site changes before opening a pull request.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE.txt](LICENSE.txt).

## Contact and Support

- Website: https://machai.machanism.org
- Source repository: https://github.com/machanism-org/machai
- Issue tracker: https://github.com/machanism-org/machai/issues
- Maintainer: Viktor Tovstyi (viktor.tovstyi@gmail.com)
