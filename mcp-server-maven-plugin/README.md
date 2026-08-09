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
server into a Maven build. It starts either a stateless HTTP MCP endpoint or a
streamable HTTP MCP endpoint directly from Maven, using the current project's
name, version, and base directory. This provides a repeatable way to expose an
MCP server for local development, integration tests, demonstrations, and
build-driven automation without requiring a separate launcher.

## Introduction

The plugin provides two aggregator goals, `stateless` and `streamable`, that load
MCP server configuration, apply configured parameters, register the configured
Machai tools, and start the selected server on the requested port. Existing JVM
system properties are preserved when parameters are applied. As aggregator
goals, both options can represent a complete multi-module build rather than
starting one server for every module.

## Goals

| Goal | Description | Key parameters |
| --- | --- | --- |
| `stateless` | Starts a stateless HTTP MCP server for the current Maven project. | `mcp.port`, `mcp.config`, `basedir`, `project`, `params` |
| `streamable` | Starts a streamable HTTP MCP server for the current Maven project. | `mcp.port`, `mcp.config`, `basedir`, `project`, `params` |

## Usage

### Prerequisites

- Java 17 or a compatible newer Java runtime.
- Apache Maven with access to this plugin and its dependencies.
- A Maven project with a valid `pom.xml`.
- An MCP server configuration file.
- An MCP-compatible HTTP client.
- Any credentials or AI-provider properties required by the configured Machai
  server and tools.

### Start a stateless server

```shell
mvn org.machanism.machai:mcp-server-maven-plugin:<version>:stateless \
  -Dmcp.port=8080 \
  -Dmcp.config=/path/to/mcp.properties
```

### Start a streamable server

Use `streamable` instead of `stateless` when the MCP client requires the
streamable HTTP transport:

```shell
mvn org.machanism.machai:mcp-server-maven-plugin:<version>:streamable \
  -Dmcp.port=8080 \
  -Dmcp.config=/path/to/mcp.properties
```

Replace `<version>` with the plugin version used by the project. The port is
required, and `mcp.config` should point to a readable configuration file
understood by the Machai MCP server configuration loader.

### Typical workflow

1. Prepare the Maven project and MCP configuration file.
2. Choose `stateless` or `streamable` based on the required transport.
3. Set `mcp.port`, `mcp.config`, and any additional parameter values.
4. Invoke the selected goal from the project or reactor root.
5. Connect an MCP HTTP client to the configured endpoint and use the registered
   tools.
6. Request an orderly shutdown with the `stop_mcp_server` function when the
   server is no longer needed.

## Configuration

| Parameter | Maven property | Description | Default |
| --- | --- | --- | --- |
| `basedir` | — | Maven module base directory used as the server project directory. | `${basedir}` |
| `project` | — | Current `MavenProject`, providing project name and version. | `${project}` (read-only) |
| `port` | `mcp.port` | HTTP port on which the selected MCP server listens. | Required |
| `configFile` | `mcp.config` | File used to load MCP server configuration and tool settings. | None; required for startup |
| `params` | — | Additional environment-style values copied to system properties when a property is not already set. | None |

The plugin does not overwrite an existing JVM system property when applying a
value from `params`. Keep credentials out of source control and provide them
through an appropriate secured Maven or runtime mechanism.

## Function tools

### `stop_mcp_server`

Initiates an orderly MCP server shutdown. The optional `exit_code` integer
defaults to `0` for normal termination. The tool immediately returns a
confirmation message, waits briefly for the request to complete, records usage
statistics, and then exits the JVM with the requested code.

## Resources

- [Machai platform](https://machai.machanism.org/)
- [Machai GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)
- [Bindex metadata](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/mcp-server-maven-plugin/bindex.json)
