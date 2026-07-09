---
<!-- @guidance:
Generate or update the content as follows.  
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.

# Page Structure
1. **Header**
   - **Project Title:** Extract automatically from `pom.xml`.
   - **Maven Central Badge:**  
     Use the following Markdown, replacing `[groupId]` and `[artifactId]` with values from `pom.xml`:  
     `[![Maven Central](https://img.shields.io/maven-central/v/[groupId]/[artifactId].svg)](https://central.sonatype.com/artifact/[groupId]/[artifactId])`
   - Bindex Badge [![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/project-layout/bindex.json)
2. **Introduction**
   - Provide a comprehensive description of the project's purpose and main benefits.
   - Clearly explain the core functionality and value proposition of the project.
   Describe the project with diagrams bellow:
     - Create a project structure overview based on the `.puml` files below.
     - Describe the project without including file names in the description.
     - Use the project structure diagram by the path: `./images/c4-diagram.png` (`src/site/puml/c4-diagram.puml`).
5. **Key Features**
   - Present a concise, bulleted list of the primary capabilities and features.
6. **Getting Started**
   - **Prerequisites:** List all required software, services, and environment settings.
7. **CLI**  
     Add a download link:  
     [![Download Ghostwriter](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/).
   - **Basic Usage:** Provide an example command to run the application.
   - **Typical Workflow:** Outline the step-by-step process for using the project artifacts.
   - **Java Version:** State the required Java version as defined in `pom.xml`, and clarify any additional functional requirements.
8. **Configuration**
   - **Command-Line Options:** Analyze `src/main/java/org/machanism/machai/mcp/server/McpServer.java` to extract and describe all available command-line options.
   - **Options Table:** Present a table listing each option, its description, and default value.
   - **Example:** Provide a command-line example showing how to configure and run the application with custom parameters.
9. **Resources**
   - List relevant links, including the official platform, GitHub repository, and Maven Central page.
# General Instructions
- Ensure clarity, completeness, and accuracy in each section.
- Use information from project files and source code as specified.
- Structure the documentation for easy navigation and practical use.
-->
canonical: https://machai.machanism.org/machai-mcp-server/index.html
---

# Machai MCP Server

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai-mcp-server.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai-mcp-server)
[![bindex](https://img.shields.io/badge/bindex-blue.svg)](https://raw.githubusercontent.com/machanism-org/machai/refs/heads/main/project-layout/bindex.json)

## Introduction

Machai MCP Server is a Java-based Model Context Protocol server for the Machai AI framework. It provides a standard integration layer between MCP-compatible clients and Machai GenAI capabilities, allowing IDE extensions, AI assistants, automation agents, and other clients to discover and invoke tools through a consistent protocol.

The server is intentionally focused on protocol transport, server lifecycle, configuration, project context propagation, and tool orchestration. Functional tools and prompts are supplied by additional Machai-compatible libraries, which makes the server a reusable gateway for different AI-powered workflows rather than a fixed tool bundle. This separation helps teams expose their own capabilities through MCP while keeping client integrations stable and transport-independent.

The architecture supports local process integration through standard input/output as well as network-accessible HTTP operation. At startup, the command-line entry point parses configuration, establishes the advertised server name and version, loads Machai tool configuration, and selects the requested transport. A shared server abstraction manages the project directory context and tool publication pipeline. HTTP deployments run on an embedded servlet container and can operate in either stateless mode or streamable session mode, while the adapter layer maps Machai tool metadata and execution requests to MCP-compatible definitions and responses.

![Machai MCP Server C4 component diagram](./images/c4-diagram.png)

## Key Features

- Implements an MCP server using the MCP Java SDK.
- Provides STDIO transport for local MCP client process integration.
- Provides HTTP transport for remote or service-style MCP deployments.
- Supports stateless HTTP operation and streamable session-capable HTTP operation.
- Exposes configurable server identity, version, project directory, configuration profile, port, and session behavior through CLI options.
- Loads Machai-compatible tool and prompt implementations from external configuration and libraries.
- Propagates project workspace context to tool execution so tools can operate on the intended project.
- Uses embedded Jetty for HTTP server runtime support.
- Separates protocol orchestration from functional tool implementations for flexible extension.

## Getting Started

### Prerequisites

- Java 17 or later.
- Maven, when building the project from source.
- An MCP-compatible client that supports STDIO or HTTP communication.
- Machai-compatible libraries or configuration that provide the tools and prompts to publish.
- Access to the target project workspace when selected tools need to inspect, generate, or modify project artifacts.
- Network access to the configured port when running in HTTP mode.

## CLI

[![Download Ghostwriter](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/)

### Basic Usage

Run the server in default STDIO mode with an explicit project directory:

```bash
java -jar machai-mcp-server.jar --projectDir /path/to/project
```

Run the server in stateless HTTP mode:

```bash
java -jar machai-mcp-server.jar --port 8080 --projectDir /path/to/project
```

Run the server in streamable HTTP session mode:

```bash
java -jar machai-mcp-server.jar --port 8080 --session --projectDir /path/to/project
```

### Typical Workflow

1. Download a packaged release or build the server from source.
2. Ensure the runtime environment provides Java 17 or newer.
3. Configure the Machai-compatible libraries that supply the tools and prompts you want to expose.
4. Select the transport mode: STDIO for local client-managed execution, or HTTP for network-accessible service execution.
5. Start the server with the desired configuration name, project directory, server identity, and transport options.
6. Connect an MCP-compatible client to the selected transport endpoint.
7. Discover available tools from the client, invoke them, and review the structured MCP responses returned by the server.

### Java Version

The Maven build sets `maven.compiler.release` to Java 17, so both compilation and runtime should use Java 17 or newer. The server itself does not include standalone tools for publication; useful functionality requires additional Machai-compatible tool and prompt libraries plus any configuration those libraries require.

## Configuration

### Command-Line Options

The application uses Apache Commons CLI and accepts each option in short or long form. If `--port` is omitted, the server starts in STDIO mode. If `--port` is provided, the server starts in HTTP mode. In HTTP mode, `--session` selects the streamable server variant; otherwise, the HTTP server runs in stateless mode.

| Option | Description | Default value |
| --- | --- | --- |
| `-h`, `--help` | Show the command-line help message. | Not enabled |
| `-d`, `--projectDir <path>` | Specify the project directory path used as the workspace context for tool execution. In HTTP mode, if omitted, the project directory is determined from the client request when possible. | Not set |
| `-n`, `--name <name>` | Specify the MCP server name advertised to clients. | `mcp-machai-server` |
| `-c`, `--config <name>` | Specify the configuration name used by the properties configurator when loading Machai tool configuration. | Properties configurator default |
| `-v`, `--version <version>` | Specify the MCP server version advertised to clients. | Package implementation version, or `latest` if unavailable |
| `-s`, `--session` | Use streamable MCP server mode. This option is meaningful only when HTTP mode is enabled with `--port`. | Disabled |
| `-p`, `--port <number>` | Specify the port number for the MCP server to listen on. Providing this option enables HTTP MCP server mode. | Not set; STDIO mode is used |

### Example

Run a streamable HTTP server on port `8080` with a custom server name, version, configuration profile, and project directory:

```bash
java -jar machai-mcp-server.jar \
  --name team-machai-mcp \
  --version 1.2.0 \
  --config production \
  --projectDir /path/to/project \
  --port 8080 \
  --session
```

## Resources

- Official platform: [https://machai.machanism.org/](https://machai.machanism.org/)
- Project site: [https://machai.machanism.org/machai-mcp-server/](https://machai.machanism.org/machai-mcp-server/)
- GitHub repository: [https://github.com/machanism-org/machai](https://github.com/machanism-org/machai)
- Maven Central: [org.machanism.machai:machai-mcp-server](https://central.sonatype.com/artifact/org.machanism.machai/machai-mcp-server)
- Model Context Protocol: [https://modelcontextprotocol.io/](https://modelcontextprotocol.io/)
- Release downloads: [SourceForge releases](https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/)
