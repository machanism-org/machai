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

The **MCP Server Maven Plugin** starts a configured Machai Model Context Protocol (MCP) HTTP server as part of a Maven build. Its aggregator goals provide stateless and streamable transports, making configured MCP tools available to compatible clients without a separate launcher. This supports local development, demonstrations, repeatable integration tests, and build-driven automation, including multi-module Maven reactors where one server represents the build.

## Introduction

The plugin uses the current Maven project's name, version, and base directory to configure the server. For either transport, it applies optional `params` entries as JVM system properties only when those properties are not already set, loads the Machai `PropertiesConfigurator` from `mcp.config`, registers the configured tools, sets the required HTTP port, and starts the endpoint. Configuration-loading and startup failures are reported as Maven execution errors.

Choose the `stateless` goal for the stateless HTTP transport or `streamable` for streamable HTTP. Both goals are Maven aggregators, so invoking one at a reactor root starts a single server instead of one per module.

## Usage

### Prerequisites

- Java 17 or a compatible newer runtime.
- Apache Maven and a Maven project with a valid `pom.xml`.
- A readable Machai MCP properties file containing the required server and tool settings.
- An available HTTP port and an MCP-compatible HTTP client.

### Start a server

Run the stateless transport:

```shell
mvn org.machanism.machai:mcp-server-maven-plugin:<version>:stateless \
  -Dmcp.port=8080 \
  -Dmcp.config=/path/to/mcp.properties
```

For streamable HTTP, replace `stateless` with `streamable`:

```shell
mvn org.machanism.machai:mcp-server-maven-plugin:<version>:streamable \
  -Dmcp.port=8080 \
  -Dmcp.config=/path/to/mcp.properties
```

Replace `<version>` with the version used by your project. Run the goal from the target project or reactor root, then connect the MCP client to the configured endpoint while Maven remains running.

### Configuration

| Parameter | Maven property | Purpose |
| --- | --- | --- |
| `port` | `mcp.port` | Required HTTP listening port. |
| `configFile` | `mcp.config` | Path to the Machai MCP configuration file loaded at startup. |
| `params` | — | Additional JVM system properties; existing process properties take precedence. |
| `basedir` | — | Maven project directory supplied to the server. |

Keep credentials and environment-specific settings in the external configuration file or another secure runtime mechanism rather than source control.

## Resources

- [Machai platform](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/mcp-server-maven-plugin)
- [Bindex metadata](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/mcp-server-maven-plugin/bindex.json)
