<!-- @guidance: >>> ${guidances}/readme-content.md -->

# Machai Project

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/bindex.json)

Machai is a multi-module Java toolkit for GenAI-enabled developer automation. It provides provider-neutral GenAI access, embedding support, Bindex library discovery, Model Context Protocol (MCP) servers, Maven integrations, and Ghostwriter workflows for guided, repeatable repository updates.

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
| [Project Layout](project-layout/) | Utility library for detecting and resolving conventional project directories across Maven, Gradle, JavaScript, Python, and fallback layouts. |
| [GenAI Client](genai-client/) | Provider-neutral Java library for prompts, embeddings, provider resolution, usage tracking, web search, MCP integration, and registering Java methods as AI-callable tools, prompts, and resources. |
| [Machai MCP Server](machai-mcp-server/) | Java 17 MCP server runtime that exposes functional tools and prompts over STDIO or HTTP; tool implementations are supplied by additional libraries. |
| [MCP Server Maven Plugin](mcp-server-maven-plugin/) | Maven plugin that starts a configured Machai MCP server for a project or reactor over stateless or streamable HTTP transport. |
| [Bindex Core](bindex-core/) | Core library for generating, registering, retrieving, and selecting Bindex metadata, including embeddings, semantic library discovery, and MongoDB-backed persistence. |
| [Ghostwriter](ghostwriter/) | AI-powered project processing engine and CLI that uses embedded guidance and reusable Acts to update code, tests, documentation, configuration, and diagrams. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven integration for Ghostwriter that runs guidance-driven processing, named Acts, or direct prompts against project and module files. |
| [Ghostwriter MCP Server](gw-mcp-server/) | Java 17 MCP server distribution that exposes Ghostwriter workflows and Bindex metadata tools over STDIO or HTTP. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and registers Bindex metadata for individual Maven projects and complete reactor builds. |
| [Bindex MCP Server](bindex-mcp-server/) | Java 17 MCP server distribution that exposes Bindex metadata retrieval, registration, and semantic library recommendation tools over STDIO or HTTP. |

## Project Structure

Machai is organized around foundation libraries, core services, Maven build integrations, and ready-to-run MCP server distributions. Project Layout provides shared directory discovery, while GenAI Client provides provider-neutral AI and embedding capabilities. Bindex Core builds on the GenAI client for metadata and semantic discovery, and Ghostwriter combines project-layout awareness with GenAI processing for guided automation.

The Maven plugins invoke their respective runtime services during builds. The server distributions package the MCP runtime with Bindex or Ghostwriter capabilities so MCP clients can access tools through HTTP or STDIO. All modules are coordinated by the parent Maven project.

![Machai project structure](./images/project-structure.png)

## Introduction

Machai supports several ways to add AI-assisted development to a Java project: use the GenAI Client directly, make tools available through an MCP server, discover reusable libraries with Bindex, or automate project-wide changes with Ghostwriter. Its modules are designed to make these workflows maintainable and repeatable rather than relying on one-off prompts or manual integration.

## Usage

### Use a library

Add a published module as a Maven dependency:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>genai-client</artifactId>
  <version>RELEASE</version>
</dependency>
```

Use the relevant module documentation for provider configuration and API-specific examples.

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

### Run Ghostwriter through Maven

Process files containing guidance tags:

```bash
mvn org.machanism.machai:gw-maven-plugin:gw
```

Run an Act or a direct prompt against a selected path:

```bash
mvn org.machanism.machai:gw-maven-plugin:act -Dgw.act="review Focus on public APIs" -Dgw.path=src/main/java
```

Configure the required AI provider, model, credentials, Bindex repository, and tool-specific settings before running AI-backed workflows.

## Troubleshooting and Debugging

Modules that include the SLF4J SimpleLogger binding can be configured with a `simplelogger.properties` file on the runtime classpath, such as under `src/main/resources`. The following configuration enables package-level debugging and tracing, writes to standard output, and configures the log layout:

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

Set `org.slf4j.simpleLogger.logFile=System.err` to write to standard error, or set it to a writable file path such as `logs/machai.log`. Package-specific properties override the global level; supported levels are `trace`, `debug`, `info`, `warn`, `error`, and `off`.

You can override the same settings at launch time without changing classpath resources:

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -Dorg.slf4j.simpleLogger.log.org.machanism.machai=trace -Dorg.slf4j.simpleLogger.logFile=System.err -jar machai-mcp-server.jar
```

JVM properties must appear before `-jar`. If settings do not take effect, verify that `slf4j-simple` is on the runtime classpath, `simplelogger.properties` is visible to the application, and no other SLF4J provider is active.

## Contributing

1. Open an issue for bugs, documentation gaps, feature proposals, or design questions.
2. Create a focused branch from `main` and keep the change limited to the stated problem.
3. Follow existing Java and Markdown style, preserve guidance comments, and update module documentation when behavior changes.
4. Add or update tests, then run `mvn clean verify` before submitting a pull request.
5. Submit a pull request with a clear summary, testing details, and any configuration or compatibility impact.

## License

Machai is distributed under the [Apache License, Version 2.0](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/LICENSE.txt).

## Contact and Support

- [Machai project site](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai)
- [GitHub issue tracker](https://github.com/machanism-org/machai/issues)
- [Machanism organization](https://machanism.org/)
- Maintainer: Viktor Tovstyi, [viktor.tovstyi@gmail.com](mailto:viktor.tovstyi@gmail.com)
