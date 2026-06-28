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

Machai MCP Server is a Java server implementation of the Model Context Protocol for the Machai AI framework. It provides a bridge between MCP-compatible clients, such as IDE integrations, AI assistants, and automation processes, and Machai-powered GenAI tools. The server focuses on transport, orchestration, tool discovery, and execution context management, while functional tool and prompt implementations are supplied by additional Machai-compatible libraries.

The project enables teams to expose reusable AI tool capabilities over either standard input/output or HTTP. This makes it suitable for local agent integrations, command-line driven workflows, and remote server deployments. Its core value is to standardize how MCP clients invoke Machai tool functions, pass project workspace context, and receive structured results without each client needing custom integration code.

The component structure is summarized below. The entry point parses command-line configuration, chooses the appropriate server mode, and starts either a STDIO transport or one of the HTTP transports. A shared server abstraction manages lifecycle and project directory context. HTTP modes run on an embedded servlet container, while all server modes use an adapter layer to transform Machai tool metadata into MCP tool definitions and delegate executions to discovered tool implementations.

![Machai MCP Server C4 component diagram](./images/c4-diagram.png)

## Key Features

- MCP server implementation built on the MCP Java SDK.
- Supports STDIO mode for local process-based MCP clients.
- Supports HTTP mode with both stateless and streamable session-capable operation.
- Command-line configuration for server name, version, project directory, configuration profile, port, and session mode.
- Integrates with Machai GenAI tooling through dynamic tool metadata loading and execution delegation.
- Passes project workspace context into tool execution so tools can operate against the intended project.
- Provides configurable server identity and implementation version reporting.
- Uses embedded Jetty for HTTP server deployments.

## Getting Started

### Prerequisites

- Java 17 or later.
- Maven, if building from source.
- An MCP-compatible client that can communicate over STDIO or HTTP.
- Machai-compatible tool libraries or configuration that provide functional tools and prompts for publication through the server.
- A target project workspace when tools need to inspect or modify project files.

## CLI

[![Download Ghostwriter](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/)

### Basic Usage

Run the server in default STDIO mode:

```bash
java -jar machai-mcp-server.jar --projectDir /path/to/project
```

Run the server in HTTP stateless mode:

```bash
java -jar machai-mcp-server.jar --port 8080 --projectDir /path/to/project
```

### Typical Workflow

1. Download a packaged release or build the application from source.
2. Add or configure Machai-compatible libraries that provide the functional tools and prompts needed by your MCP client.
3. Choose a transport mode: STDIO for local client process integration, or HTTP for network-accessible operation.
4. Start the server with the desired project directory and configuration name.
5. Connect an MCP client to the server using the selected transport.
6. Invoke published tools from the client; the server maps MCP requests to Machai tool executions and returns structured results.

### Java Version

The project is compiled with `maven.compiler.release` set to Java 17. Runtime environments must provide Java 17 or newer. Functional behavior also depends on available Machai tool implementations and their configuration, because this server does not publish standalone tools by itself.

## Configuration

### Command-Line Options

The application accepts Apache Commons CLI options in short or long form. If no port is provided, it starts in STDIO mode. If a port is provided, it starts as an HTTP MCP server. Adding the session option to HTTP mode selects the streamable server variant; otherwise HTTP mode is stateless.

| Option | Description | Default value |
| --- | --- | --- |
| `-h`, `--help` | Show the help message and exit. | Not enabled |
| `-d`, `--projectDir <path>` | Specify the project directory path used as the workspace context for tool execution. In HTTP mode, if omitted, the project directory is determined from the client request when possible. | Not set |
| `-n`, `--name <name>` | Specify the MCP server name advertised to clients. | `mcp-machai-server` |
| `-c`, `--config <name>` | Specify the configuration name used by the properties configurator when loading tool configuration. | Configurator default |
| `-v`, `--version <version>` | Specify the MCP server version advertised to clients. | Package implementation version, or `latest` if unavailable |
| `-s`, `--session` | Use streamable MCP server mode. This option applies only when running the HTTP MCP server. | Disabled |
| `-p`, `--port <number>` | Specify the port number for the MCP server to listen on. Providing this option enables HTTP MCP server mode. | Not set; STDIO mode is used |

### Example

Run a streamable HTTP server on port `8080` with a custom server name, version, configuration, and project directory:

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
