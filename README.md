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

Machai is a multi-module Java toolkit for GenAI-enabled developer automation. It provides provider-neutral GenAI access, embedding support, Bindex library discovery, an MCP server, Maven integrations, and Ghostwriter workflows for repeatable automation across source code, tests, documentation, site content, configuration, diagrams, and other project files.

## Modules

| Module | Description |
| --- | --- |
| [Project Layout](project-layout/) | Utility library for describing and resolving conventional project directories, including production sources, tests, resources, and documentation, across common project layouts. |
| [GenAI Client](genai-client/) | Provider-neutral Java library for generative AI integrations, including prompts, embeddings, provider resolution, usage tracking, web search, MCP servers, and AI-callable Java tools. |
| [Machai MCP Server](machai-mcp-server/) | Java 17 Model Context Protocol server that exposes Machai tools and prompts through STDIO or HTTP transport, including stateless and streamable HTTP modes. |
| [MCP Server Maven Plugin](mcp-server-maven-plugin/) | Maven plugin that launches the Machai MCP Server over HTTP and supplies project metadata, parameters, tools, and the project directory to the server. |
| [Bindex Core](bindex-core/) | Core services for Bindex metadata retrieval and registration, semantic library recommendation, classification, embeddings, and MongoDB-backed persistence for AI-assisted project assembly. |
| [Ghostwriter](ghostwriter/) | AI-assisted documentation engine and command-line processor that scans and updates project content using guidance tags and reusable Acts across source code, tests, documentation, site pages, configuration, diagrams, and other governed files. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven adapter for Ghostwriter that provides guidance-tag processing and Act execution, supports multi-module and parallel builds, and supplies Maven project context to repository automation. |

## Installation

### Prerequisites

- Git and network access for cloning the repository and downloading dependencies.
- Apache Maven 3.8.1 or newer.
- Java 17 or newer for the complete reactor, including the MCP server, MCP server Maven plugin, and Bindex Core.
- A Java 8-compatible runtime and compiler can be used for Project Layout, GenAI Client, Ghostwriter, and GW Maven Plugin modules.

### Clone and build

```bash
git clone https://github.com/machanism-org/machai.git
cd machai
mvn clean verify
```

To build and stage the Maven site:

```bash
mvn clean install site site:stage
```

Set `MACHANISM_PACK_DIR` before running `mvn -Ppack install` when a packaged CLI or server distribution is required.

## Usage

### Build or consume a module

Build one module and its required dependencies:

```bash
mvn -pl ghostwriter -am clean install
```

Add a published module as a Maven dependency, for example:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>genai-client</artifactId>
  <version>RELEASE</version>
</dependency>
```

### Run the MCP server

Build the server with its packaging profile, add functional tool libraries to the runtime classpath, and start STDIO mode:

```bash
mvn -pl machai-mcp-server -Ppack install
java -cp path/to/machai-mcp-server.jar:path/to/functional-tools.jar org.machanism.machai.mcp.server.McpServer
```

Start HTTP mode on port 45000 with `--port 45000`.

### Run Ghostwriter through Maven

Process files containing guidance tags:

```bash
mvn org.machanism.machai:gw-maven-plugin:gw
```

Run an Act against a selected path:

```bash
mvn org.machanism.machai:gw-maven-plugin:act -Dgw.act="review Focus on public APIs" -Dgw.path=src/main/java
```

Configure the required AI provider, model, credentials, Bindex repository, and tool-specific settings before running AI-backed workflows. See the individual module pages for API and configuration details.

## Contributing

1. Open a [GitHub issue](https://github.com/machanism-org/machai/issues) for a bug, documentation gap, feature request, or design question.
2. Create a focused branch and keep changes limited to the stated problem.
3. Follow the existing Java and Markdown style, preserve guidance comments, and update relevant module documentation when behavior changes.
4. Add or update tests for code changes and run `mvn clean verify` before submitting.
5. Submit a pull request with a clear summary, testing details, and configuration or compatibility impact, then respond to review feedback.

## License

Machai is distributed under the [Apache License, Version 2.0](LICENSE.txt). The project POM declares this license for all modules. The canonical license text is also available from the [Apache Software Foundation](https://www.apache.org/licenses/LICENSE-2.0.txt).

## Contact and Support

- [Machai project site](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai)
- [GitHub issue tracker](https://github.com/machanism-org/machai/issues)
- [Machanism organization](https://machanism.org/)
- Maintainer: Viktor Tovstyi, [viktor.tovstyi@gmail.com](mailto:viktor.tovstyi@gmail.com)
