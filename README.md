<!-- @guidance: >>> ${guidances}/readme-content.md 
- Check the repository for any nested modules or directories.
- Determine whether each module is a Git submodule (by checking if it is tracked as a submodule via `.gitmodules` or Git index).
- If a module is a Git submodule, construct its URL pointing correctly to its remote repository (or specific commit/tree reference) rather than a relative path inside the parent tree.
-->
# Machai Project

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex.json)

Machai is a multi-module Java toolkit for GenAI-enabled developer automation. It provides provider-neutral generative-AI access, embedding support, Bindex library discovery, Model Context Protocol (MCP) servers, Maven integrations, and Ghostwriter workflows for repeatable, maintainable AI-assisted development.

## Cloning and Getting Started

To clone and set up this project locally, follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/machanism-org/machai.git
   cd machai
   ```
2. **Build the project using Maven:**
   ```bash
   mvn clean install
   ```

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](https://github.com/machanism-org/project-layout/tree/29b6ce722bd3840ee98ffb2c7acdb40d4691cadf) | Utility library for describing, detecting, and resolving conventional project directories. It gives build tools, scanners, generators, and plugins a consistent model for source, test, resource, and documentation locations across Maven, Gradle, JavaScript, Python, and fallback layouts. |
| [GenAI Client](https://github.com/machanism-org/genai-client/tree/5d8086068ae38881f9c054535700bc72fd2f5401) | Provider-neutral Java library for generative-AI integrations. It supports prompt execution, embeddings, provider resolution, usage tracking, web search, MCP servers, and registration of Java methods as AI-callable tools, prompts, and resources. |
| [Machai MCP Server](https://github.com/machanism-org/machai-mcp-server/tree/b4b00098fd8a620a681e2ad1be015849dfe56c39) | Java 17 Model Context Protocol server runtime that exposes functional tools and prompts through STDIO or HTTP, including stateless and streamable HTTP modes; domain-specific tools remain in separate runtime libraries. |
| [MCP Server Maven Plugin](https://github.com/machanism-org/mcp-server-maven-plugin/tree/462d368dd1e7b76e3eb444ee25692e276b73b3de) | Maven plugin that starts the Machai MCP Server for a Maven project over HTTP. Its aggregator goals provide stateless or streamable transport and supply project metadata, parameters, tools, and project-directory context. |
| [Bindex Core](https://github.com/machanism-org/bindex-core/tree/42a90af49d8883961aacd2c97b3f2d24d4bd57d7) | Core services for Bindex metadata retrieval, registration, semantic library recommendation, classification, embeddings, and MongoDB-backed persistence. It supports Ghostwriter, Maven plugins, MCP workflows, and AI-assisted project assembly. |
| [Ghostwriter](https://github.com/machanism-org/ghostwriter/tree/c25e310811de8d5d65cacabab7382459c3f73086) | AI-powered project-wide processing engine and command-line tool for source code, tests, documentation, site content, configuration, diagrams, and other project files. It uses embedded guidance and reusable Acts for repeatable AI-assisted automation. |
| [Ghostwriter-Py](https://github.com/machanism-org/ghostwriter-py/tree/90403f88e194e12becb6de389d110ae60d54e03b) | Python wrapper around the Ghostwriter command-line processor. It embeds the Java runtime in Python and exposes Ghostwriter processing through the `machai.gw` package and `gw` function. |
| [GW Maven Plugin](https://github.com/machanism-org/gw-maven-plugin/tree/b8d752c574ae7418ed07b7fc8e07d977ef906903) | Primary Maven adapter for Ghostwriter. It runs guidance-driven processing or named and prompt-based Acts over selected project files, with project-wide and per-module goals, Maven settings integration, and Java class-introspection tools. |
| [Ghostwriter MCP Server](https://github.com/machanism-org/gw-mcp-server/tree/e13b50c0f98a15d5c299d6f51ef998c08b0996f0) | Runnable Java 17 MCP server that packages Ghostwriter workflows, Bindex metadata services, and the Machai MCP runtime. It exposes project-assistance, metadata retrieval, registration, and library-recommendation capabilities through STDIO or HTTP. |
| [Bindex Maven Plugin](https://github.com/machanism-org/bindex-maven-plugin/tree/ea071462b8906e8cdece54759f317f6fb588c7b2) | Maven plugin that generates and registers Bindex metadata for Maven projects and reactor builds. It provides reactor-wide and per-module goals that delegate generation and registration to Ghostwriter and Bindex Core workflows. |
| [Bindex MCP Server](https://github.com/machanism-org/bindex-mcp-server/tree/954eb4884f8ff113fadf9bb7a5f47be9ae0d805f) | Java 17 MCP application that packages Bindex Core with the Machai MCP runtime. It exposes metadata retrieval, registration, and semantic library recommendation tools through STDIO or HTTP. |

## Project Structure

Machai is a Maven parent project that coordinates eleven cooperating modules: foundation libraries, core AI services, language integration, Maven build integrations, and ready-to-run MCP server distributions. Project Layout supplies shared directory resolution, and GenAI Client supplies provider, embedding, and tool abstractions. The Machai MCP Server provides the reusable MCP runtime; Bindex Core and Ghostwriter build on the foundation libraries; Ghostwriter-Py embeds the Ghostwriter processor for Python callers; Maven plugins invoke their corresponding runtime services; and the server distributions publish Bindex and Ghostwriter capabilities to MCP clients. External AI providers serve the GenAI client, while Maven builds invoke the plugins.

## Introduction

Applications can use the GenAI client directly, expose tools through MCP, discover reusable libraries through Bindex, or automate repository-wide updates to source code, tests, documentation, configuration, diagrams, and other project files with the Ghostwriter command line and Maven plugin. The modules are designed to work independently where appropriate and together for end-to-end AI-assisted development workflows.

## Requirements and Build

- JDK 17 or newer is required to build the complete reactor. Several libraries target Java 8 bytecode, while the MCP server components and Bindex Core require Java 17.
- Apache Maven 3.8.1 or newer.
- Git and network access to download dependencies.
- Provider credentials and service configuration when running GenAI, Bindex, or custom functional-tool workflows.

Run the full verification suite with:

```bash
mvn clean verify
```

To build and stage the project site:

```bash
mvn clean install site site:stage
```

Set `MACHANISM_PACK_DIR` before running `mvn -Ppack install` when a packaging profile needs to create a CLI or server distribution.

## Usage

### Add a library dependency

Add a published module to your Maven project, replacing `RELEASE` with the version you require:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>genai-client</artifactId>
  <version>RELEASE</version>
</dependency>
```

See the selected module's documentation for provider configuration, API details, and workflow examples.

### Run an MCP server

Build the Machai MCP server with its packaging profile, add functional-tool libraries to the runtime classpath, and start it in STDIO mode:

```bash
mvn -pl machai-mcp-server -Ppack install
java -cp path/to/machai-mcp-server.jar:path/to/functional-tools.jar org.machanism.machai.mcp.server.McpServer
```

Start HTTP mode on port 45000:

```bash
java -cp path/to/machai-mcp-server.jar:path/to/functional-tools.jar org.machanism.machai.mcp.server.McpServer --port 45000
```

### Run Ghostwriter from Maven

Process guidance-tagged files:

```bash
mvn org.machanism.machai:gw-maven-plugin:gw
```

Run an Act or direct prompt for a selected path:

```bash
mvn org.machanism.machai:gw-maven-plugin:act -Dgw.act="review Focus on public APIs" -Dgw.path=src/main/java
```

Configure the required AI provider, model, credentials, Bindex repository, and tool-specific settings before running AI-backed workflows.

## Troubleshooting and Debugging

Modules that include the SLF4J SimpleLogger binding can be configured with `simplelogger.properties` on the runtime classpath, commonly in `src/main/resources`. This example enables a global level and package-specific levels, writes to standard output, and configures the log layout:

```properties
org.slf4j.simpleLogger.defaultLogLevel=info
org.slf4j.simpleLogger.log.org.machanism=debug
org.slf4j.simpleLogger.log.org.machanism.machai=trace
org.slf4j.simpleLogger.logFile=System.out
org.slf4j.simpleLogger.showThreadName=true
org.slf4j.simpleLogger.showLogName=true
org.slf4j.simpleLogger.showShortLogName=true
org.slf4j.simpleLogger.levelInBrackets=true
org.slf4j.simpleLogger.showDateTime=true
org.slf4j.simpleLogger.dateTimeFormat=yyyy-MM-dd HH:mm:ss.SSS
```

Set `org.slf4j.simpleLogger.logFile=System.err` for standard error, or set it to a writable file path such as `logs/machai.log`; its parent directory must exist. Package-specific settings override the default level. Valid levels are `trace`, `debug`, `info`, `warn`, `error`, and `off`.

Override the same properties at launch without changing classpath resources:

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -Dorg.slf4j.simpleLogger.log.org.machanism.machai=trace -Dorg.slf4j.simpleLogger.logFile=System.err -Dorg.slf4j.simpleLogger.showDateTime=true -jar machai-mcp-server.jar
```

Place JVM properties before `-jar`. If a change does not take effect, confirm that `slf4j-simple` is present at runtime, `simplelogger.properties` is on the effective classpath, and another SLF4J provider has not been selected.

## Contributing

1. Open an issue for a bug, documentation gap, feature proposal, or design question.
2. Create a focused branch from `main` and keep changes limited to the stated problem.
3. Follow the existing Java and Markdown style, preserve guidance comments, and update relevant documentation when behavior changes.
4. Add or update tests, run `mvn clean verify`, and submit a pull request with a clear summary, testing details, and configuration or compatibility impact.
5. Address review feedback and keep the branch synchronized with the target branch.

## License

Machai is distributed under the [Apache License, Version 2.0](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/LICENSE.txt).

## Contact and Support

- [Machai project site](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai)
- [GitHub issue tracker](https://github.com/machanism-org/machai/issues)
- [Machanism organization](https://machanism.org/)
- Maintainer: Viktor Tovstyi, [viktor.tovstyi@gmail.com](mailto:viktor.tovstyi@gmail.com)
