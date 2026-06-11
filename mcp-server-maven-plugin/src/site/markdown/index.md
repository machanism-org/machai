---
<!-- @guidance:
Generate or update the content as follows.
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
# Page Structure:
1. Header
   - Project Title: use from pom.xml
   - Maven Central Badge ([![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])
2. Introduction
   - Full description of purpose and benefits.
3. Overview
   - Explanation of the project function and value proposition.
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

## Introduction

MCP Server Maven Plugin is a Maven plugin that starts the [MCP Machai Server](https://machai.machanism.org/mcp-server/index.html) as part of a Maven build. It exposes the Machai AI tools over the Model Context Protocol via HTTP, making the server available to MCP-compatible clients such as Claude Desktop during development, integration testing, or any workflow that requires a running MCP endpoint alongside a Maven build.

The plugin provides two goals corresponding to the two HTTP transport modes supported by the MCP Machai Server: stateless HTTP and streamable HTTP. Both goals are aggregator goals that run once at the execution root, start the server on a configured port, and block until the process is stopped.

## Overview

The plugin bridges Maven project context with the MCP Machai Server runtime. When a goal is invoked, it resolves the project base directory and version from the current Maven project, constructs the appropriate server instance, registers the available tools via SPI discovery, and starts the server on the configured port.

Two HTTP transport modes are available:

- **Stateless HTTP** (`mcp-server:stateless`) starts an HTTP MCP server using a stateless request/response model. Each request is handled independently with no persistent session state.
- **Streamable HTTP** (`mcp-server:streamable`) starts an HTTP MCP server using a streamable transport model, which supports incremental result delivery over an HTTP connection.

Both modes expose the same SPI-discovered Machai tools and accept the same `mcp.port` configuration parameter.

## Goals

| Goal | Description |
|---|---|
| `mcp-server:stateless` | Starts the MCP Machai Server in stateless HTTP mode on the configured port. Aggregator goal; runs at the execution root. |
| `mcp-server:streamable` | Starts the MCP Machai Server in streamable HTTP mode on the configured port. Aggregator goal; runs at the execution root. |

## Getting Started

### Prerequisites

- **Java 17 or later**, as required by `maven.compiler.release=17` in `pom.xml`.
- **Apache Maven 3.8.1 or later**.
- **A network port** available for the server to bind to.
- **GenAI provider credentials** in the environment when the tools loaded by the server require model access (for example, `GENAI_USERNAME` and `GENAI_PASSWORD` for CodeMie, or `ANTHROPIC_API_KEY` for Claude).

### Basic Usage

Add the plugin to your `pom.xml`:

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

Start the stateless HTTP server:

```bash
mvn mcp-server:stateless
```

Start the streamable HTTP server:

```bash
mvn mcp-server:streamable
```

Override the port at the command line:

```bash
mvn mcp-server:stateless -Dmcp.port=8080
```

### Typical Workflow

1. Add the plugin to the `pom.xml` of the project that will serve as the MCP server root.
2. Set the desired `port` in the plugin configuration, or use `-Dmcp.port=...` at the command line.
3. Ensure any required GenAI provider credentials are available as environment variables.
4. Run the appropriate goal (`mcp-server:stateless` or `mcp-server:streamable`).
5. Connect an MCP-compatible client such as Claude Desktop to `http://localhost:<port>/mcp`.
6. Stop the server by terminating the Maven process.

### Claude Desktop Configuration (HTTP)

```json
{
  "mcpServers": {
    "localMcpServer": {
      "command": "npx",
      "args": [
        "mcp-remote",
        "http://localhost:45000/mcp"
      ]
    }
  }
}
```

## Configuration

| Parameter | Property | Description | Default |
|---|---|---|---|
| `port` | `mcp.port` | Port number the HTTP MCP server will listen on. Required. | *(none, required)* |

The `basedir` and `project` parameters are resolved automatically from the Maven execution context and do not require manual configuration.

## Resources

- MCP Machai Server: https://machai.machanism.org/mcp-server/index.html
- Machai platform: https://machai.machanism.org/
- GitHub repository: https://github.com/machanism-org/machai
- Maven Central: https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin
- Model Context Protocol: https://modelcontextprotocol.io/
