<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src\\site\\markdown\\index.md` content summary.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])` after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

# MCP Server Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/mcp-server-maven-plugin.svg)](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)

MCP Server Maven Plugin is a Maven plugin for launching the Machai MCP Server directly from a Maven build. It exposes Machai AI tools through the Model Context Protocol (MCP) over HTTP, making it convenient to start a local MCP endpoint for development, testing, demonstrations, and integration workflows without a separate launcher.

## Introduction

MCP Server Maven Plugin integrates Machai MCP Server startup into normal Maven workflows. It is designed for projects that already use Maven as their primary build tool and need a straightforward way to make Machai AI tools available to MCP-compatible clients.

The plugin starts an HTTP-based Machai MCP Server using metadata from the current Maven project, including the project name, version, and base directory. During execution, it applies configured system properties, optionally resolves credentials from Maven `settings.xml`, discovers available Machai tools, and starts the selected HTTP transport on the configured port.

The plugin supports two transport styles:

- **Stateless HTTP** via `mcp-server:stateless`, where each request is handled independently.
- **Streamable HTTP** via `mcp-server:streamable`, where responses can be delivered incrementally over an HTTP connection.

Both goals are aggregator goals intended to run once from the execution root in a multi-module build. The Maven process remains active while the server is running so external MCP clients can connect to the exposed endpoint.

## Usage

### Prerequisites

- Java 17 or later.
- Apache Maven.
- An available TCP port for the MCP HTTP server.
- Access to any required AI provider credentials, either through system properties, runtime configuration, or a Maven `settings.xml` `<server>` entry referenced by `serverId`.
- An MCP-compatible client, such as Claude Desktop or another MCP HTTP client, if interactive access is required.

### Basic Configuration

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

### Run the Server

Start the stateless HTTP server:

```bash
mvn mcp-server:stateless
```

Start the streamable HTTP server:

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

## Goals

| Goal | Description | Key parameters |
|---|---|---|
| `mcp-server:stateless` | Starts the Machai MCP Server in stateless HTTP mode using the current Maven project name and version. | `port`, `params`, `serverId` |
| `mcp-server:streamable` | Starts the Machai MCP Server in streamable HTTP mode using the current Maven project name and version. | `port`, `params`, `serverId` |

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
