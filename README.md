<!-- @guidance: >>> ${guidances}/readme-content.md 
- Check the repository for any nested modules or directories.
- Project structure diagram: `https://machai.machanism.org/images/project-structure.png`.
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
   git submodule update --init --recursive
   ```
2. **Build the project using Maven:**
   ```bash
   mvn clean install
   ```

## Modules

| Name | Description |
| --- | --- |
| [Project Layout](https://github.com/machanism-org/project-layout/tree/f5eda719e9d03349c6068b5060c8437cf3dd66fa) | Utility library for describing, detecting, and resolving conventional project directories. It gives build tools, scanners, generators, and plugins a consistent model for source, test, resource, and documentation locations across Maven, Gradle, JavaScript, Python, and fallback layouts. |
| [GenAI Client](https://github.com/machanism-org/genai-client/tree/2da383548fbe884bdef0de7b8e1195117027812a) | Provider-neutral Java library for generative-AI integrations. It supports prompt execution, embeddings, provider resolution, usage tracking, web search, MCP servers, and registration of Java methods as AI-callable tools, prompts, and resources. |
| [Machai MCP Server](https://github.com/machanism-org/machai-mcp-server/tree/8e477b90eb4e9d3b43d94402c01ee24f55142f7c) | Java 17 Model Context Protocol server runtime that exposes functional tools and prompts through STDIO or HTTP, including stateless and streamable HTTP modes; domain-specific tools remain in separate runtime libraries. |
| [MCP Server Maven Plugin](https://github.com/machanism-org/mcp-server-maven-plugin/tree/2fd9cff0c0976905411c68eb031d6cf39e1de584) | Maven plugin that starts the Machai MCP Server for a Maven project over HTTP. Its aggregator goals provide stateless or streamable transport and supply project metadata, parameters, tools, and project-directory context. |
| [Bindex Core](https://github.com/machanism-org/bindex-core/tree/e77b57bee1d87edecb3473f37896cbbf010ebad3) | Core services for Bindex metadata retrieval, registration, semantic library recommendation, classification, embeddings, and MongoDB-backed persistence. It supports Ghostwriter, Maven plugins, MCP workflows, and AI-assisted project assembly. |
| [Ghostwriter](https://github.com/machanism-org/ghostwriter/tree/3c3b06b9bda412995aae961610123d15e30566c6) | AI-powered project-wide processing engine and command-line tool for source code, tests, documentation, site content, configuration, diagrams, and other project files. It uses embedded guidance and reusable Acts for repeatable AI-assisted automation. |
| [Ghostwriter-Py (mgw)](https://github.com/machanism-org/ghostwriter-py/tree/5da3b9f64c63242b6ad69a685135faef560cf59f) | Python 3.9-or-newer API and command-line wrapper around Ghostwriter. It bundles the Java runtime with `mgw`, validates `JAVA_HOME`, and starts JPype lazily. Its `gw`, `gdp`, and `adw` functions support CLI invocation, guidance processing, and Act execution; direct processor calls can resolve local JARs or Maven coordinates through `jgo` and return ordinary Python results. |
| [GW Maven Plugin](https://github.com/machanism-org/gw-maven-plugin/tree/c3acfc911bc3d4dc20ff5c6ba1a2687e1ebfc9b2) | Primary Maven adapter for Ghostwriter. It runs guidance-driven processing or named and prompt-based Acts over selected project files, with project-wide and per-module goals, Maven settings integration, and Java class-introspection tools. |
| [Ghostwriter MCP Server](https://github.com/machanism-org/gw-mcp-server/tree/65649a61b0e259db2d3cdf2acd5a870392aa37ae) | Runnable Java 17 MCP server that packages Ghostwriter workflows, Bindex metadata services, and the Machai MCP runtime. It exposes project-assistance, metadata retrieval, registration, and library-recommendation capabilities through STDIO or HTTP. |
| [Bindex Maven Plugin](https://github.com/machanism-org/bindex-maven-plugin/tree/adab828861048cff878adacb7e6c0291b9ea94ce) | Maven plugin that generates and registers Bindex metadata for Maven projects and reactor builds. It provides reactor-wide and per-module goals that delegate generation and registration to Ghostwriter and Bindex Core workflows. |
| [Bindex MCP Server](https://github.com/machanism-org/bindex-mcp-server/tree/84588c1a35a90051a98c57174d0963607dcc6143) | Standalone Java 17 distribution packaging Bindex Core with the Machai MCP runtime. It exposes complete or GraphQL-filtered metadata retrieval, validation and registration from JSON, files, or URLs, schema access, and natural-language library recommendations through STDIO or HTTP. Recommendations use classification, embeddings, and MongoDB-backed search with language, layer, score, and version filters. |

## Project Structure

Machai is a Maven parent project that coordinates eleven cooperating modules: foundation libraries, core AI services, language integration, Maven build integrations, and ready-to-run MCP server distributions. Project Layout supplies shared directory resolution, and GenAI Client supplies provider, embedding, and tool abstractions. The Machai MCP Server provides the reusable MCP runtime; Bindex Core and Ghostwriter build on the foundation libraries; Ghostwriter-Py embeds the Ghostwriter processor for Python callers; Maven plugins invoke their corresponding runtime services; and the server distributions publish Bindex and Ghostwriter capabilities to MCP clients. External AI providers serve the GenAI client, while Maven builds invoke the plugins. Each module is tracked as a Git submodule in `.gitmodules` and the Git index, and each link targets its corresponding remote GitHub repository at the recorded submodule commit rather than a relative path in the parent repository. The recorded commits are `f5eda719e9d03349c6068b5060c8437cf3dd66fa`, `2da383548fbe884bdef0de7b8e1195117027812a`, `8e477b90eb4e9d3b43d94402c01ee24f55142f7c`, `2fd9cff0c0976905411c68eb031d6cf39e1de584`, `e77b57bee1d87edecb3473f37896cbbf010ebad3`, `3c3b06b9bda412995aae961610123d15e30566c6`, `5da3b9f64c63242b6ad69a685135faef560cf59f`, `c3acfc911bc3d4dc20ff5c6ba1a2687e1ebfc9b2`, `65649a61b0e259db2d3cdf2acd5a870392aa37ae`, `adab828861048cff878adacb7e6c0291b9ea94ce`, and `84588c1a35a90051a98c57174d0963607dcc6143`.

The architecture groups the modules into foundation libraries, core services, language integration, Maven build integration, and ready-to-run MCP server distributions. External AI providers supply the GenAI client, Maven invokes the build plugins, and MCP clients consume the packaged server capabilities. Bindex Maven Plugin uses GW Maven Plugin and Bindex Core, while the Ghostwriter packaging profile includes Bindex tools, also available transitively to the Python integration. The generated project-structure diagram provides a visual overview of these relationships.

![Machai project structure](https://machai.machanism.org/images/project-structure.png)

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

To build and stage the project site, first set `MACHAI_SITE` to a writable staging directory used by the site's post-processing configuration:

```bash
mvn clean install site site:stage
```

Set `MACHANISM_PACK_DIR` before running `mvn -Ppack install` when a packaging profile needs to create a CLI or server distribution. Reactor-wide packaging also builds the Python wrapper and requires Python 3.9 or newer with the `build` package installed (`python -m pip install build`). Set `JAVA_HOME` before using the Python wrapper.

## Usage

### Add a library dependency

Add a published module to your Maven project, selecting the version you require:

```xml
<dependency>
  <groupId>org.machanism.machai</groupId>
  <artifactId>genai-client</artifactId>
  <version>1.4.2</version>
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

These commands require the assembled JAR containing runtime dependencies; substitute its actual output path and your tool library paths. On Windows, quote the classpath and separate entries with `;` instead of `:`, for example `java -cp "path/to/machai-mcp-server.jar;path/to/functional-tools.jar" org.machanism.machai.mcp.server.McpServer --port 45000`. Connect an HTTP MCP client to `http://localhost:45000/mcp`; add `--session` for streamable HTTP sessions.

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

### Use the CLI, Python wrapper, and metadata tools

From an unpacked Ghostwriter delivery pack, inspect the CLI options or process a selected path:

```bash
java -jar gw.jar --help
java -jar gw.jar src --projectDir .
```

With Python 3.9 or newer and `JAVA_HOME` configured, install the wrapper and inspect its CLI:

```bash
python -m pip install mgw
python -m mgw --help
```

The wrapper exposes `gw`, `gdp`, and `adw` for Python applications. Use Bindex Maven Plugin's `bindex` and `register` goals to generate and publish metadata, or their `-per-module` variants for reactor module processing. MCP Server Maven Plugin's `stateless` and `streamable` goals launch an HTTP endpoint from Maven with `-Dmcp.port=45000 -Dmcp.config=gw.properties`. The Ghostwriter and Bindex MCP distributions package their respective tools for MCP clients; consult the linked module documentation for service configuration and packaging details.

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
