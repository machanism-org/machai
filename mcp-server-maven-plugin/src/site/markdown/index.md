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

The MCP Server Maven Plugin launches the Machai MCP Server directly from a Maven build. It exposes Machai AI tools through the Model Context Protocol (MCP) over HTTP, making it practical to connect MCP-compatible clients to a Maven project without running a separate server launcher or custom bootstrap application.

The plugin is useful for local development, integration testing, demonstrations, and project-aware AI workflows. It uses Maven project metadata, the module base directory, configured runtime parameters, and optional Maven `settings.xml` credentials to start an MCP server that is already aligned with the current build context.

## Overview

The plugin provides Maven goals for starting HTTP-based MCP servers in either stateless or streamable mode. Each goal starts a server for the current Maven project, assigns the configured port, points the server at the project directory, and loads available Machai function tools.

This approach gives teams a repeatable, Maven-native way to expose AI-assisted capabilities for a project. Developers can start the MCP server with a standard Maven command, pass configuration through plugin parameters or Maven settings, and then connect tools or clients that understand MCP.

![MCP Server Maven Plugin architecture](./images/c4-diagram.png)

## Goals

| Goal | Description | Key parameters |
| --- | --- | --- |
| `mcp-server:stateless` | Starts a stateless HTTP MCP server for the Maven project. This mode is suitable for request/response integrations where the server does not rely on a persistent stream-oriented interaction model. | `port`, `params`, `serverId`, `basedir`, `project` |
| `mcp-server:streamable` | Starts a streamable HTTP MCP server for the Maven project. This mode is suitable for MCP clients that use streamable HTTP interactions. | `port`, `params`, `serverId`, `basedir`, `project` |

Both goals are aggregator goals, so they are intended to be executed once for a Maven reactor build rather than independently for each module.

## Getting Started

### Prerequisites

- Java 17 or later.
- Apache Maven.
- A Maven project where the plugin will be executed.
- Network access to any AI provider or service configured for the Machai tools you plan to use.
- Optional Maven `settings.xml` server credentials when provider credentials should be resolved from Maven settings.
- An MCP-compatible client or integration that can connect to the HTTP server started by the plugin.

### Basic Usage

Start a stateless MCP server on port `8080`:

```bash
mvn org.machanism.machai:mcp-server-maven-plugin:stateless -Dmcp.port=8080
```

Start a streamable MCP server on port `8080`:

```bash
mvn org.machanism.machai:mcp-server-maven-plugin:streamable -Dmcp.port=8080
```

### Typical Workflow

1. Add or reference the plugin in the Maven project where the MCP server should run.
2. Configure the required `mcp.port` value and any provider-specific parameters.
3. Optionally define a Maven `settings.xml` server entry and pass its id with the provider server-id property when credentials should be loaded from Maven settings.
4. Run either the `stateless` or `streamable` goal from the project root.
5. Connect an MCP-compatible client to the HTTP endpoint exposed by the server.
6. Use the exposed Machai tools during development, testing, demonstrations, or integration workflows.
7. Stop the server from the client by using the provided stop tool, or terminate the Maven process manually.

## Configuration

| Parameter | Description | Default value |
| --- | --- | --- |
| `port` / `mcp.port` | Required TCP port on which the MCP server listens. | No default; must be provided. |
| `params` | Map of environment or runtime parameters to apply as system properties before the server starts. Existing system properties with the same keys are preserved. | Not specified. |
| `serverId` | Optional Maven `settings.xml` server id used to resolve AI provider credentials and custom server configuration values. Credentials are copied into the Machai configuration used by the server. | Not specified. |
| `basedir` | Maven module base directory used as the project directory for the MCP server. | `${basedir}` |
| `project` | Current Maven project metadata used to name and version the started MCP server. | `${project}` |
| `settings` | Maven settings used internally to resolve configured server credentials. | `${settings}` |

A typical plugin configuration looks like this:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>mcp-server-maven-plugin</artifactId>
  <version>1.2.0-SNAPSHOT</version>
  <configuration>
    <port>8080</port>
    <params>
      <example.property>example-value</example.property>
    </params>
  </configuration>
</plugin>
```

## Function Toools

The plugin exposes server-management functions as MCP tools so connected clients can control the running server when appropriate.

| Function tool | Description | Parameters | Availability |
| --- | --- | --- | --- |
| `stop_mcp_server` | Initiates shutdown of the MCP server and returns a confirmation message. The tool logs the shutdown request, waits briefly, records usage statistics, and exits the server process with the requested code. Use it when a client needs to safely stop the Maven-launched MCP server. | `exit_code`: optional integer exit code. Defaults to `0` for normal termination. | Supported for `McpServer`. |

## Resources

- [Machai Platform](https://machai.machanism.org/)
- [MCP Server Maven Plugin on Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)
- [Machai GitHub Repository](https://github.com/machanism-org/machai)
- [Bindex metadata](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/mcp-server-maven-plugin/bindex.json)
