---
<!-- @guidance:
Generate or update the content as follows.
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
# Page Structure:
1. Header
   - Project Title: use from pom.xml
   - Maven Central Badge [![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
   - Bindex Badge [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/mcp-server-maven-plugin/bindex.json)
2. Introduction
   - Provide a comprehensive description of the GW Maven plugin, including its purpose and benefits.
   - Analyze java files in the `src/main/java/org/machanism/machai/mcp/maven` to inform the description.
   - Full description of purpose and benefits.
3. Overview
   - Explanation of the project function and value proposition.
   - Use the project structure diagram by the path: `./images/c4-diagram.png` (`src/site/puml/c4-diagram.puml`).
4. Goals
   - Table of plugin goals, their descriptions, and key parameters.
5. Getting Started
   - Prerequisites: List of required software and services.
   - Basic Usage: Example command to run the plugin.
   - Typical Workflow: Step-by-step outline of how to use the plugin.
6. Configuration
   - 
   - Table of configuration parameters, their descriptions, and default values.
7. Function Toools
   - Analyze classes in the folder: `src/main/java/org/machanism/machai/mcp/maven/tools` and use this information to create the page content but do not mentionad this as a package details.
   - If the function tool class is annotated with the `@SupportedFor` annotation, specify this in the description of the function tool methods.
   - Write a general description of the each functional tool.
8. Resources
   - List of relevant links (platform, GitHub, Maven).
-->
canonical: https://machai.machanism.org/mcp-server-maven-plugin/index.html
---

# MCP Server Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/mcp-server-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin) [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/mcp-server-maven-plugin/bindex.json)

## Introduction

The **MCP Server Maven Plugin** integrates a Machai Model Context Protocol (MCP)
server into a Maven build. It provides two aggregator goals that start either a
stateless HTTP MCP endpoint or a streamable HTTP MCP endpoint, using the current
Maven project's name, version, and base directory. This makes an MCP server
available directly from a Maven invocation instead of requiring a separate
launcher or manually assembled runtime command.

The plugin applies configured parameters as JVM system properties without
overwriting properties that were already supplied, loads the MCP server
configuration, registers the configured Machai tools, and starts the selected
server on the requested port. It is useful for local development, repeatable
integration tests, demonstrations, and build-driven automation. Because the
goals are aggregators, they are also suitable for a multi-module build when one
server should represent the complete build rather than each individual module.

## Overview

A build engineer invokes a goal through Maven. The selected Mojo shares common
project, port, parameter, and configuration handling through its base component,
then creates and starts the corresponding Machai HTTP server. MCP clients can
connect to that server while the build or development process is running, and
the exposed lifecycle tool can request a delayed shutdown with a caller-selected
exit code.

The project structure and interactions are illustrated below. The source diagram
is maintained at `src/site/puml/c4-diagram.puml` and rendered for the site as
`./images/c4-diagram.png`.

![C4 component diagram for the MCP Server Maven Plugin](./images/c4-diagram.png)

## Goals

| Goal | Description | Key parameters |
| --- | --- | --- |
| `stateless` | Starts a stateless HTTP MCP server for the current project. | `mcp.port`, `mcp.config`, `basedir`, `project`, and `params` |
| `streamable` | Starts a streamable HTTP MCP server for the current project. | `mcp.port`, `mcp.config`, `basedir`, `project`, and `params` |

Both goals are aggregator goals. They apply the configured parameters, load the
MCP configuration, attach the project's directory and metadata, configure the
port, and start the server. A startup or configuration failure is reported as a
Maven execution error.

## Getting Started

### Prerequisites

- Java 17 or a compatible newer Java runtime.
- Apache Maven with access to the plugin and its transitive dependencies.
- A Maven project with a valid `pom.xml` and a project directory.
- An MCP server configuration file supplied through `mcp.config`.
- An MCP-compatible HTTP client for consuming the endpoint.
- Any credentials or AI-provider properties required by the configured Machai
  server and tools.

### Basic usage

Run the stateless endpoint from the plugin's Maven coordinates:

```shell
mvn org.machanism.machai:mcp-server-maven-plugin:<version>:stateless \
  -Dmcp.port=8080 \
  -Dmcp.config=/path/to/mcp.properties
```

Use `streamable` instead of `stateless` when the client and deployment require
the streamable HTTP transport:

```shell
mvn org.machanism.machai:mcp-server-maven-plugin:<version>:streamable \
  -Dmcp.port=8080 \
  -Dmcp.config=/path/to/mcp.properties
```

Replace `<version>` with the version used by the project. The port is required;
the configuration path should identify a readable file understood by the Machai
MCP server configuration loader.

### Typical workflow

1. Add or resolve the plugin in the Maven project and prepare the MCP
   configuration file.
2. Select `stateless` or `streamable` according to the transport expected by the
   MCP client.
3. Set `mcp.port`, `mcp.config`, and any additional `params` values before
   invoking Maven.
4. Start the goal from the desired project or reactor root. The aggregator goal
   uses the Maven project metadata and base directory to configure one server.
5. Connect an MCP HTTP client to the configured endpoint and use the registered
   tools.
6. Request shutdown with the `stop_mcp_server` function when the server is no
   longer needed.

## Configuration

The following parameters are supplied by Maven to the plugin Mojos. Parameters
without a Maven property can be set in the plugin configuration in the `pom.xml`.

| Parameter | Maven property | Description | Default |
| --- | --- | --- | --- |
| `basedir` | — | Maven module base directory used as the server project directory. | `${basedir}` |
| `project` | — | Current `MavenProject`; supplies project name and version to the server. | `${project}` (read-only) |
| `port` | `mcp.port` | HTTP port on which the selected MCP server listens. | No default; required |
| `configFile` | `mcp.config` | File used to load MCP server configuration and tool settings. | None; optional in Maven metadata, but required for server startup |
| `params` | — | Map of additional environment-style values copied to system properties when a property is not already set. | None |

The plugin does not overwrite an existing JVM system property when applying a
value from `params`. Keep configuration and credentials out of source control
where possible, and pass sensitive values through the appropriate secured Maven
or runtime mechanism.

## Function Tools

The plugin exposes server-lifecycle functionality to the MCP server through a
function tool. The tool class is supported for `McpServer`, as indicated by its
`@SupportedFor(McpServer.class)` annotation.

### `stop_mcp_server`

Initiates an orderly MCP server shutdown. It accepts the optional `exit_code`
integer, which defaults to `0` for normal termination, immediately returns a
confirmation message, waits briefly to allow the request to complete, records
usage statistics, and then exits the JVM with the requested code. Use this tool
when an MCP client needs to stop the server process after completing its work.

## Resources

- [Machai platform](https://machai.machanism.org/)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)
- [Bindex metadata](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/mcp-server-maven-plugin/bindex.json)
