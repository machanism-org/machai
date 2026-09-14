<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/[artifactId].svg)](https://central.sonatype.com/artifact/org.machanism.machai/[artifactId])` after the title as a new paragraph.
   - Add [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/[artifactId]/bindex.json)
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
- If used resources by uri: `src/site/resources/`, need to use project site location: `https://machai.machanism.org/[artifactId]/`.
-->

# MCP Server Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/mcp-server-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)

[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/mcp-server-maven-plugin/bindex.json)

The **MCP Server Maven Plugin** integrates a Machai Model Context Protocol (MCP) server into a Maven build. Its aggregator goals start configured stateless or streamable HTTP MCP servers directly from Maven, enabling local development, repeatable integration tests, demonstrations, and build-driven automation without a separate launcher.

## Introduction

The plugin creates and starts an HTTP MCP server using the current Maven project's name, version, and base directory. The `stateless` goal provides a stateless HTTP transport, while `streamable` provides streamable HTTP. An MCP client can therefore use the configured Machai server and its tools directly from a Maven invocation.

Before startup, the plugin applies `params` values as JVM system properties without replacing properties that are already set, loads a `PropertiesConfigurator` from the configured file, applies the project directory and port, registers configured tools, and starts the selected server. Configuration and startup failures are reported as Maven execution errors. Because both goals are aggregators, a reactor build exposes one server representing the build rather than starting one server per module.

## Overview

A build engineer invokes a plugin goal through Maven. The selected Mojo shares parameter handling and configuration loading through the common server Mojo, creates the corresponding `HttpStatelessMcpServer` or `HttpStreamableMcpServer`, and supplies Maven project metadata, the project directory, configured port, and tools before starting it. MCP clients connect to the resulting HTTP endpoint, and the lifecycle tool can request a delayed JVM shutdown after the client completes its work.

The project structure and interactions are illustrated below. The source diagram is maintained at `src/site/puml/c4-diagram.puml`.

![C4 component diagram for the MCP Server Maven Plugin](https://machai.machanism.org/mcp-server-maven-plugin/images/c4-diagram.png)

## Goals

| Goal | Description | Key parameters |
| --- | --- | --- |
| `stateless` | Aggregator goal that creates, configures, and starts an `HttpStatelessMcpServer`. | `mcp.port`, `mcp.config`, `basedir`, `project`, and `params` |
| `streamable` | Aggregator goal that creates, configures, and starts an `HttpStreamableMcpServer`. | `mcp.port`, `mcp.config`, `basedir`, `project`, and `params` |

Both goals apply `params`, load the configuration file, create a server with the Maven project's name and version, set the project directory and port, register tools, and start the server. The port is required. A failure to load configuration or start the server is surfaced as a `MojoExecutionException`.

## Usage

### Prerequisites

- Java 17 or a compatible newer Java runtime, matching the plugin's compiler release.
- Apache Maven with access to this plugin and its transitive dependencies.
- A Maven project with a valid `pom.xml` and a project base directory.
- A readable MCP configuration file for the Machai `PropertiesConfigurator`.
- An MCP-compatible HTTP client and any credentials or AI-provider properties required by the configured Machai server and tools.

### Basic usage

Run the stateless endpoint using the plugin's Maven coordinates:

```shell
mvn org.machanism.machai:mcp-server-maven-plugin:1.4.1:stateless \
  -Dmcp.port=8080 \
  -Dmcp.config=/path/to/mcp.properties
```

For streamable HTTP, use the `streamable` goal instead:

```shell
mvn org.machanism.machai:mcp-server-maven-plugin:1.4.1:streamable \
  -Dmcp.port=8080 \
  -Dmcp.config=/path/to/mcp.properties
```

The examples use version `1.4.1`. The port must be provided, and `mcp.config` should point to a file accepted by the Machai MCP server configuration loader. The implementation dereferences this file when loading configuration, so supply it even though the Maven annotation does not mark the parameter as required.

### Typical workflow

1. Prepare an MCP configuration file and resolve the plugin in the Maven project.
2. Choose `stateless` or `streamable` according to the transport expected by the MCP client.
3. Supply `mcp.port`, `mcp.config`, and any `params` values before invoking Maven.
4. Run the aggregator goal from the desired project or reactor root; it uses Maven project metadata and the base directory to configure one server.
5. Connect an MCP HTTP client to the selected endpoint and use the configured tools.
6. Invoke `stop-mcp-server` when the server is no longer needed; it returns an acknowledgement and then performs a delayed process exit.

## Configuration

The following parameters are injected by Maven into both Mojos. Parameters without a Maven property can be supplied in plugin configuration in `pom.xml`; the two goals are aggregator goals, so the invocation is normally made from the reactor or project root.

| Parameter | Maven property | Description | Default |
| --- | --- | --- | --- |
| `basedir` | — | Maven module base directory passed to the MCP server as its project directory. | `${basedir}`; required |
| `project` | — | Read-only `MavenProject` that supplies the project name and version used to create the server. | `${project}`; read-only |
| `port` | `mcp.port` | HTTP port on which the selected MCP server listens. | No default; required |
| `configFile` | `mcp.config` | File whose absolute path is passed to `McpServer.getConfigurator(...)` to load server configuration and tool settings. | `mcp.properties` |
| `params` | — | Map of additional key/value values copied to JVM system properties only when the property is not already set; null values are ignored. | No default |

When `mcp.config` is omitted, the plugin resolves its `mcp.properties` default to an absolute path relative to the Maven process's working directory; provide `mcp.config` when the configuration file is elsewhere. Existing JVM system properties take precedence over values in `params`, and null parameter values are not applied. Keep configuration and credentials out of source control where possible, and pass sensitive values through an appropriate secured Maven or runtime mechanism.

## Function Tools

The lifecycle function tool provides an MCP client with a controlled way to stop the running server. It is supported for `McpServer`, as declared by the function-tool implementation's `@SupportedFor(McpServer.class)` annotation.

### `stop-mcp-server`

`stop-mcp-server` accepts the optional integer parameter `exit-code`, which defaults to `0`, and immediately returns `MCP server shutdown initiated.` It logs the requested exit code, starts a background shutdown task, waits one second, records usage statistics, and exits the JVM with that code. This supports orderly termination after a client completes its work; an interrupted delay is logged before shutdown continues.

## Resources

- [Machai platform](https://machai.machanism.org/)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)
- [Bindex metadata](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/mcp-server-maven-plugin/bindex.json)
