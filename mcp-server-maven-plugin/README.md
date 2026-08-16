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

The **MCP Server Maven Plugin** integrates a Machai Model Context Protocol (MCP)
server into a Maven build. It provides stateless and streamable HTTP goals that
expose Machai AI tools using the current project's metadata and configuration.
The plugin supports local development, repeatable integration tests,
demonstrations, and build-driven automation without requiring a separate
launcher, including multi-module builds through aggregator goals.

## Introduction

The plugin provides two aggregator goals, `stateless` and `streamable`, which
start either a stateless HTTP MCP endpoint or a streamable HTTP MCP endpoint.
They load the MCP server configuration, apply configured parameters without
overwriting existing JVM system properties, register the configured Machai
tools, and start the selected server on the requested port. The current Maven
project's name, version, and base directory are supplied to the server so the
same invocation can represent the complete build from a reactor root.

## Goals

| Goal | Description | Key parameters |
| --- | --- | --- |
| `stateless` | Starts a stateless HTTP MCP server for the current project. | `mcp.port`, `mcp.config`, `basedir`, `project`, and `params` |
| `streamable` | Starts a streamable HTTP MCP server for the current project. | `mcp.port`, `mcp.config`, `basedir`, `project`, and `params` |

Both goals apply the configured parameters, load the MCP configuration, attach
the project's directory and metadata, configure the port, and start the server.
A startup or configuration failure is reported as a Maven execution error.

## Usage

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

## Function tools

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
