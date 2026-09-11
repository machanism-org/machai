<!-- @guidance: >>> ${guidances}/readme-content.md -->
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
| [Project Layout](project-layout/) | Utility library that detects and resolves conventional project directories, giving tools a consistent model for source, test, resource, and documentation locations across common build ecosystems. |
| [GenAI Client](genai-client/) | Provider-neutral Java client for prompts, embeddings, provider resolution, usage tracking, web search, MCP servers, and Java methods registered as AI-callable tools, prompts, and resources. |
| [Machai MCP Server](machai-mcp-server/) | Java 17 MCP server runtime that exposes functional tools and prompts over STDIO or HTTP transports. |
| [MCP Server Maven Plugin](mcp-server-maven-plugin/) | Maven plugin that starts a Machai MCP server over HTTP and supplies Maven-project metadata, parameters, tools, and project-directory context. |
| [Bindex Core](bindex-core/) | Core Bindex services for metadata retrieval and registration, semantic library recommendation, classification, embeddings, and persistence. |
| [Ghostwriter](ghostwriter/) | Guidance-driven AI processing engine and command-line tool for repository-wide updates to code, tests, documentation, configuration, diagrams, and other project files. |
| [GW Maven Plugin](gw-maven-plugin/) | Maven adapter for Ghostwriter that runs guidance-driven processing, named Acts, and prompt-based Acts over project files. |
| [Ghostwriter MCP Server](gw-mcp-server/) | Runnable Java 17 MCP server that exposes Ghostwriter workflows, Bindex services, and the Machai MCP runtime over STDIO or HTTP. |
| [Bindex Maven Plugin](bindex-maven-plugin/) | Maven plugin that generates and registers Bindex metadata for individual Maven projects and reactor builds. |
| [Bindex MCP Server](bindex-mcp-server/) | Java 17 MCP application that exposes Bindex metadata retrieval, registration, and semantic library recommendation tools over STDIO or HTTP. |

## Project Structure

Machai is a Maven parent project that coordinates foundation libraries, core AI services, Maven build integrations, and ready-to-run MCP server distributions. Project Layout and GenAI Client supply shared directory and AI abstractions. Bindex Core and Ghostwriter build on those foundations; Maven plugins invoke the corresponding runtime services; and the server distributions publish Bindex and Ghostwriter capabilities to MCP clients. External AI providers serve the GenAI client, while Maven builds and runs the plugins.

![Machai project structure](./images/project-structure.png)

## Introduction

Applications can use the GenAI client directly, expose tools through MCP, discover reusable libraries through Bindex, or automate repository-wide changes with the Ghostwriter command line and Maven plugin. The modules are designed to work independently where appropriate and together for end-to-end AI-assisted development workflows.

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
