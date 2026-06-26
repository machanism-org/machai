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
7. Resources
   - List of relevant links (platform, GitHub, Maven).
-->
canonical: https://machai.machanism.org/mcp-server-maven-plugin/index.html
---

# MCP Server Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/mcp-server-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)
[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/mcp-server-maven-plugin/bindex.json)

## Introduction

MCP Server Maven Plugin is a Maven plugin for launching the [Machai MCP Server](https://machai.machanism.org/machai-mcp-server/index.html) directly from a Maven build. It makes Machai AI tools available through the Model Context Protocol (MCP) over HTTP so that MCP-compatible clients can connect to a locally started server during development, testing, demonstrations, or integration workflows.

The plugin is especially useful when a project already uses Maven as its primary build tool and needs a simple way to start an MCP endpoint without introducing a separate launcher or runtime wrapper. By packaging server startup as Maven goals, the plugin fits naturally into existing developer workflows and multi-module builds.

## Overview

The plugin starts an HTTP-based Machai MCP Server using metadata from the current Maven project, including the project name, version, and base directory. During execution, it applies configured system properties, optionally resolves credentials from Maven `settings.xml`, discovers available Machai tools, and starts the selected HTTP transport on the configured port.

It supports two transport styles:

- **Stateless HTTP** via `mcp-server:stateless`, where each request is handled independently.
- **Streamable HTTP** via `mcp-server:streamable`, where responses can be delivered incrementally over an HTTP connection.

Both goals are aggregator goals, so they are intended to run once from the execution root in a multi-module build. In both cases, the plugin blocks while the server is running, allowing external MCP clients to connect to the exposed endpoint.

![Project overview diagram](./images/c4-diagram.png)

## Goals

| Goal | Description | Key parameters |
|---|---|---|
| `mcp-server:stateless` | Starts the Machai MCP Server in stateless HTTP mode using the current Maven project name and version. | `port`, `params`, `serverId` |
| `mcp-server:streamable` | Starts the Machai MCP Server in streamable HTTP mode using the current Maven project name and version. | `port`, `params`, `serverId` |

## Getting Started

### Prerequisites

- **Java 17 or later**.
- **Apache Maven**.
- **An available TCP port** for the MCP HTTP server.
- **Access to any required AI provider credentials**, either through system properties, environment-driven runtime configuration, or a Maven `settings.xml` `<server>` entry referenced by `serverId`.
- **An MCP-compatible client** if you plan to connect interactively, such as Claude Desktop or another MCP HTTP client.

### Basic Usage

Configure the plugin in your `pom.xml`:

```xml
<plugin>
  <groupId>org.machanism.machai</groupId>
  <artifactId>mcp-server-maven-plugin</artifactId>
  <version>1.2.0-SNAPSHOT</version>
  <configuration>
    <port>45000</port>
  </configuration>
</plugin>
```

Run the stateless HTTP server:

```bash
mvn mcp-server:stateless
```

Run the streamable HTTP server:

```bash
mvn mcp-server:streamable
```

Override the configured port from the command line:

```bash
mvn mcp-server:stateless -Dmcp.port=8080
```

### Typical Workflow

1. Add the plugin to the Maven project that should host the MCP server.
2. Configure the required `port` value in the plugin configuration or provide it with `-Dmcp.port=...`.
3. If needed, supply additional runtime properties through `params` and configure `serverId` to load credentials from Maven `settings.xml`.
4. Start either `mcp-server:stateless` or `mcp-server:streamable`.
5. Connect your MCP client to the running HTTP endpoint, typically `http://localhost:<port>/mcp`.
6. Stop the server by terminating the Maven process or by using the exposed shutdown tool when appropriate.

## Configuration

| Parameter | Property | Description | Default |
|---|---|---|---|
| `port` | `mcp.port` | Port used by the HTTP MCP server. This value is required. | *(none)* |
| `params` | — | Map of system properties to apply before server startup when they are not already set. | *(none)* |
| `serverId` | `mcp.ai.serverid` | Maven `settings.xml` server identifier used to load credentials and custom configuration properties for AI providers. | *(none)* |
| `basedir` | — | Maven project base directory passed to the server as the project directory. Resolved automatically from Maven. | `${basedir}` |
| `project` | — | Current Maven project used to supply the project name and version. Resolved automatically from Maven. | `${project}` |
| `settings` | — | Maven settings object used internally to resolve the configured `serverId`. | `${settings}` |

## Resources

- [Machai platform](https://machai.machanism.org/)
- [Machai MCP Server](https://machai.machanism.org/machai-mcp-server/index.html)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)
- [Model Context Protocol](https://modelcontextprotocol.io/)
