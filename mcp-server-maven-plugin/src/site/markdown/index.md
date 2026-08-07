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

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/mcp-server-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)
[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/mcp-server-maven-plugin/bindex.json)

## Introduction

The MCP Server Maven Plugin launches the Machai MCP Server directly from a Maven build. It exposes Machai AI tools through the Model Context Protocol over HTTP, making it useful for local development, testing, demonstrations, and integration workflows without requiring a separate launcher.

By using the current Maven project as its execution context, the plugin provides a repeatable way to start a project-aware MCP server. It supplies the project name and version, project directory, configured port, and optional runtime parameters to the server, allowing MCP-compatible clients to use Machai capabilities with minimal setup.

## Overview

The plugin supplies two aggregator goals for starting an HTTP MCP server for the current Maven project. The `stateless` goal starts a request/response-oriented server, while the `streamable` goal starts a server for streamable HTTP MCP interactions. Both goals apply configured parameters, register the available tools, use the Maven project metadata, and listen on the configured port.

This Maven-native approach fits naturally into existing build and development workflows. Teams can start the appropriate server with a standard Maven command, keep project context aligned with the build, and connect an MCP-compatible client without maintaining a separate server bootstrap application.

![MCP Server Maven Plugin architecture](./images/c4-diagram.png)

## Goals

| Goal | Description | Key parameters |
| --- | --- | --- |
| `mcp-server:stateless` | Starts a stateless HTTP MCP server for the current Maven project. It applies runtime parameters, registers Machai tools, sets the project directory and port, and starts the server. | `port`, `params`, `basedir`, `project` |
| `mcp-server:streamable` | Starts a streamable HTTP MCP server for the current Maven project. It performs the same project and parameter setup while providing streamable HTTP MCP interactions. | `port`, `params`, `basedir`, `project` |

Both goals are aggregator goals and are intended to run once for a Maven reactor build rather than independently for every module.

## Getting Started

### Prerequisites

- Java 17 or later.
- Apache Maven.
- A Maven project in which to run the plugin.
- An MCP-compatible client that can connect to the HTTP server.
- Network access to any external AI provider or service required by the Machai tools being used.

### Basic Usage

The `port` parameter is required. Start a stateless MCP server on port `8080` with:

```bash
mvn org.machanism.machai:mcp-server-maven-plugin:1.3.0-SNAPSHOT:stateless -Dmcp.port=8080
```

For streamable HTTP MCP interactions, use:

```bash
mvn org.machanism.machai:mcp-server-maven-plugin:1.3.0-SNAPSHOT:streamable -Dmcp.port=8080
```

### Typical Workflow

1. Open a terminal at the Maven project root.
2. Choose the `stateless` or `streamable` goal according to the client integration required.
3. Set the required `mcp.port` property and any optional runtime parameters.
4. Run the goal; the plugin applies parameters, creates the server with the Maven project name and version, registers the tools, and starts listening.
5. Configure the MCP-compatible client to connect to the server's HTTP endpoint.
6. Use the available Machai tools in the context of the project directory.
7. Stop the server with the `stop_mcp_server` tool or terminate the Maven process when the session is complete.

## Configuration

| Parameter | Description | Default value |
| --- | --- | --- |
| `port` / `mcp.port` | TCP port on which the MCP server listens. The Maven property name is `mcp.port`. | No default; required. |
| `params` | Map of runtime parameters. Each entry is applied as a system property only when that property is not already set. | Not specified. |
| `basedir` | Maven module base directory supplied to the MCP server as its project directory. | `${basedir}` |
| `project` | Current Maven project metadata used to initialize the server with the project name and version. | `${project}` |

Example plugin configuration:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>mcp-server-maven-plugin</artifactId>
  <version>1.3.0-SNAPSHOT</version>
  <configuration>
    <port>8080</port>
    <params>
      <example.property>example-value</example.property>
    </params>
  </configuration>
</plugin>
```

## Function Tools

The plugin provides server-lifecycle functions to MCP clients. These functions are registered with the running server and allow a client to request an orderly shutdown.

| Function tool | Description | Parameters | Availability |
| --- | --- | --- | --- |
| `stop_mcp_server` | Initiates shutdown of the MCP server. It logs the requested exit code, starts a delayed shutdown that records usage statistics, and then exits the process. The tool returns a confirmation as soon as shutdown has been initiated. This method is supported for `McpServer`. | `exit_code`: integer exit code; defaults to `0` for normal termination. | `McpServer` |

## Resources

- [Machai Platform](https://machai.machanism.org/)
- [MCP Server Maven Plugin on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)
- [Machai GitHub Repository](https://github.com/machanism-org/machai)
- [Bindex metadata](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/mcp-server-maven-plugin/bindex.json)
