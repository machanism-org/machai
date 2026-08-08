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
- If you use a relative path, make sure it will be valid after generating the project site.
-->
canonical: https://machai.machanism.org/index.html
---

# Machai Project

Machai is a multi-module Java toolkit for GenAI-enabled developer automation. It provides provider-neutral GenAI access, embedding support, Bindex library discovery, an MCP server, Maven integrations, and Ghostwriter workflows that can process source code, documentation, site content, configuration, diagrams, and other project files.

The project is designed to make AI-assisted development repeatable and maintainable. Applications can use the GenAI client directly, expose tools through MCP, discover reusable libraries through Bindex, or automate repository-wide updates through the Ghostwriter command line and Maven plugin.

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](project-layout/) | A utility library for describing and resolving conventional project directories such as production sources, test sources, resources, and documentation. It gives build tools and plugins a consistent project-structure model across Maven, Gradle, JavaScript, Python, and fallback layouts. |
| [GenAI Client](genai-client/) | A provider-neutral Java library for generative AI integrations. It supports prompt execution, embeddings, provider resolution, usage tracking, web search, MCP servers, and registration of Java methods as AI-callable tools and prompts. |
| [Machai MCP Server](machai-mcp-server/) | A Java 17 Model Context Protocol server that exposes Machai functional tools and prompts through STDIO or HTTP transport. It supports stateless and streamable HTTP modes while keeping domain-specific tools in separate runtime libraries. |
| [MCP Server Maven Plugin](mcp-server-maven-plugin/) | A Maven plugin that launches the Machai MCP Server for a Maven project over HTTP. Its stateless and streamable goals supply project metadata, parameters, tools, and the project directory to the server. |
| [Bindex Core](bindex-core/) | Core services for Bindex metadata retrieval, registration, semantic library recommendation, classification, embeddings, and MongoDB-backed persistence. It supports Ghostwriter, Maven plugins, MCP workflows, and AI-assisted project assembly. |
| [Ghostwriter](ghostwriter/) | An AI-assisted documentation engine and command-line processor that scans and updates project-wide content using guidance tags and reusable Acts. It supports source code, tests, documentation, site pages, configuration, diagrams, and other governed files. |
| [GW Maven Plugin](gw-maven-plugin/) | The primary Maven adapter for Ghostwriter. It provides goals for guidance-tag processing and Act execution, supports multi-module and parallel builds, and supplies Maven project context to repeatable repository automation. |

## Project Structure

The project is organized as a Maven parent and seven cooperating modules. The parent coordinates the build and module lifecycle. Project Layout supplies shared directory resolution; GenAI Client supplies provider and tool abstractions; and the MCP server builds on that client to publish runtime capabilities. The MCP Maven plugin launches the server from Maven. Bindex Core adds metadata and semantic library discovery. Ghostwriter combines project layout and GenAI processing for guided repository automation, while the GW Maven Plugin adapts that automation to Maven projects.

![Machai project structure](./images/project-structure.png)

## Installation

### Prerequisites

- Java 17 or newer for the MCP server, MCP server Maven plugin, and Bindex Core modules.
- Java 8 compatible runtime and compiler support for the Project Layout, GenAI Client, Ghostwriter, and GW Maven Plugin modules. A Java 17 JDK is a practical choice for building the complete reactor.
- Apache Maven 3.8.1 or newer.
- Git and network access to clone the repository and download dependencies.
- Provider credentials and service configuration when using GenAI, Bindex, or custom functional tools.

### Build from source

Clone the repository and build all modules:

```bash
git clone https://github.com/machanism-org/machai.git
cd machai
mvn clean verify
```

To build the site and stage its generated pages, use:

```bash
mvn clean install site site:stage
```

Some packaging profiles use `MACHANISM_PACK_DIR` for delivery artifacts. Set that environment variable before running `mvn -Ppack install` when a packaged CLI or server distribution is required.

## Usage

### Use a library

Add a published module as a Maven dependency. For example:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>genai-client</artifactId>
  <version>1.3.0-SNAPSHOT</version>
</dependency>
```

Use the module pages for provider configuration, API details, and workflow-specific examples.

### Run the MCP server

Build the server with its packaging profile, add functional tool libraries to the runtime classpath, and start STDIO mode:

```bash
mvn -pl machai-mcp-server -Ppack install
java -cp path/to/machai-mcp-server.jar:path/to/functional-tools.jar org.machanism.machai.mcp.server.McpServer
```

Start HTTP mode on port 45000:

```bash
java -cp path/to/machai-mcp-server.jar:path/to/functional-tools.jar org.machanism.machai.mcp.server.McpServer --port 45000
```

### Run Ghostwriter through Maven

Process files containing guidance tags:

```bash
mvn org.machanism.machai:gw-maven-plugin:gw
```

Run an Act or direct prompt against a selected path:

```bash
mvn org.machanism.machai:gw-maven-plugin:act -Dgw.act="review Focus on public APIs" -Dgw.path=src/main/java
```

Configure the required AI provider, model, credentials, Bindex repository, and tool-specific settings before running AI-backed workflows.

## Contributing

1. Open an issue to describe a bug, documentation gap, proposed feature, or design question.
2. Create a focused branch from the main branch and keep changes limited to the stated problem.
3. Follow the existing Java and Markdown style, preserve guidance comments, and update relevant module documentation when behavior changes.
4. Add or update tests for code changes and run `mvn clean verify` before submitting.
5. Submit a pull request with a clear summary, testing details, and any configuration or compatibility impact.
6. Respond to review feedback and keep the branch synchronized with the target branch.

## License

Machai is distributed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt). The project POM declares this license for all modules.

## Contact and Support

- [Machai project site](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai)
- [GitHub issue tracker](https://github.com/machanism-org/machai/issues)
- [Machanism organization](https://machanism.org/)
- Maintainer: Viktor Tovstyi, [viktor.tovstyi@gmail.com](mailto:viktor.tovstyi@gmail.com)
