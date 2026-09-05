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

# Machai MCP Server

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai-mcp-server.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai-mcp-server)

[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/machai-mcp-server/bindex.json)

Machai MCP Server is a Java 17 gateway that exposes Machai functional tools and prompts to Model Context Protocol (MCP) clients. It supports local STDIO integrations and remote HTTP deployments while keeping domain-specific capabilities in separately packaged tool libraries.

## Introduction

The server bridges MCP-compatible clients with Machai tools discovered on the runtime classpath. It handles transport selection, tool and prompt discovery, MCP schema adaptation, and request routing, so teams can publish their own automation, coding, data, or platform capabilities without modifying the server.

Choose the transport that fits your integration:

- **STDIO** for a local client that launches the server process.
- **Stateless HTTP** by supplying a port.
- **Streamable HTTP** by supplying both a port and `--session`.

## Usage

### Prerequisites

- Java 17 or newer
- A Machai-compatible functional-tool or prompt JAR, including its service-provider registration
- Any credentials, model configuration, environment variables, or services required by those tools
- Maven only when building from source

### Build from source

```bash
mvn clean package
```

To build the assembled release JAR, set `MACHANISM_PACK_DIR` to a writable directory and run:

```bash
mvn -Ppack install
```

### Run in STDIO mode

With no `--port` option, the server communicates over standard input and output:

```bash
java -cp "machai-mcp-server.jar;functional-tool-container.jar" org.machanism.machai.mcp.server.McpServer
```

Use `:` instead of `;` between classpath entries on Unix-like systems.

### Run in HTTP mode

Start a stateless HTTP MCP endpoint on port `45000`:

```bash
java -cp "machai-mcp-server.jar;functional-tool-container.jar" org.machanism.machai.mcp.server.McpServer --port 45000
```

Add `--session` for streamable HTTP transport. Clients connect to `http://localhost:45000/mcp`.

### Common options

| Option | Purpose |
| --- | --- |
| `-d`, `--projectDir <path>` | Provide a project directory as tool execution context. |
| `-c`, `--config <path>` | Load server properties from a configuration file. |
| `-n`, `--name <value>` | Set the server name exposed to clients. |
| `-v`, `--version <value>` | Set the version exposed to clients. |
| `-p`, `--port <number>` | Start the HTTP transport on a port. |
| `-s`, `--session` | Use streamable HTTP transport with `--port`. |

For detailed setup, client configuration, and troubleshooting, see the [project documentation](https://machai.machanism.org/machai-mcp-server/).

## Resources

- [Machai platform](https://machai.machanism.org/)
- [GitHub repository](https://github.com/machanism-org/machai)
- [Maven Central artifact](https://central.sonatype.com/artifact/org.machanism.machai/machai-mcp-server)
- [SourceForge releases](https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/)
