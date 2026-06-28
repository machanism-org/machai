<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src/site/markdown/index.md` content summary.
   - Add `[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai-mcp-server.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai-mcp-server)` after the title as a new paragraph.
3. **Introduction**
   - Use from documentation folder: site/markdown/index.md
2. **Usage:**  
   - Use from documentation folder: site/markdown/index.md
   - Add the Ghostwriter CLI application jar download link: [![Download Ghostwriter](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/) to the installation section.
**Formatting Requirements:**
- Use Markdown syntax for headings, lists, code blocks, and links.
- Ensure clarity and conciseness in each section.
- Organize the README for easy navigation and readability.
-->

# Machai MCP Server

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/machai-mcp-server.svg)](https://central.sonatype.com/artifact/org.machanism.machai/machai-mcp-server)

Machai MCP Server is a Java 17 Model Context Protocol server for the Machai AI framework. It bridges MCP-compatible clients with Machai-powered functional tools and prompts, exposing reusable tool capabilities over STDIO or HTTP while handling transport selection, tool discovery, execution context management, and MCP schema adaptation.

## Introduction

Machai MCP Server provides a bridge between MCP-compatible clients, such as IDE integrations, AI assistants, and automation processes, and Machai-powered GenAI tools. The server focuses on transport, orchestration, tool discovery, and execution context management, while functional tool and prompt implementations are supplied by additional Machai-compatible libraries.

The project enables teams to expose reusable AI tool capabilities over either standard input/output or HTTP. This makes it suitable for local agent integrations, command-line driven workflows, and remote server deployments. Its core value is to standardize how MCP clients invoke Machai tool functions, pass project workspace context, and receive structured results without each client needing custom integration code.

The component structure starts from a command-line entry point that parses configuration, chooses the appropriate server mode, and starts either a STDIO transport or one of the HTTP transports. A shared server abstraction manages lifecycle and project directory context. HTTP modes run on an embedded servlet container, while all server modes use an adapter layer to transform Machai tool metadata into MCP tool definitions and delegate executions to discovered tool implementations.

## Key Features

- MCP server implementation built on the MCP Java SDK.
- Supports STDIO mode for local process-based MCP clients.
- Supports HTTP mode with both stateless and streamable session-capable operation.
- Command-line configuration for server name, version, project directory, configuration profile, port, and session mode.
- Integrates with Machai GenAI tooling through dynamic tool metadata loading and execution delegation.
- Passes project workspace context into tool execution so tools can operate against the intended project.
- Provides configurable server identity and implementation version reporting.
- Uses embedded Jetty for HTTP server deployments.

## Usage

### Prerequisites

Before running Machai MCP Server, ensure you have:

- Java 17 or later.
- Maven, if building from source.
- An MCP-compatible client that can communicate over STDIO or HTTP.
- Machai-compatible tool libraries or configuration that provide functional tools and prompts for publication through the server.
- A target project workspace when tools need to inspect or modify project files.
- Any credentials, environment variables, model names, or service settings required by the loaded tools.

### Installation

Download a release package:

[![Download Ghostwriter](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/)

Or build from source:

```bash
mvn clean package
```

To create a packaged distribution jar with dependencies using the assembly profile:

```bash
mvn -Ppack install
```

### Basic Usage

Run the server in default STDIO mode:

```bash
java -jar machai-mcp-server.jar --projectDir /path/to/project
```

Run the server in HTTP stateless mode:

```bash
java -jar machai-mcp-server.jar --port 8080 --projectDir /path/to/project
```

When using external Machai tool containers, place the server jar and tool libraries on the runtime classpath:

```bash
java -cp /path/to/machai-mcp-server.jar:/path/to/functional-tool-container.jar org.machanism.machai.mcp.server.McpServer --projectDir /path/to/project
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

## Publishing Function Tools from User JAR Files

To publish your own functional tool implementation:

1. Implement the tool according to the [Machai Functional Tools SPI documentation](https://machai.machanism.org/genai-client/functional-tools.html#How_to_create_a_custom_functional_tool).
2. Package the implementation into a jar with the required service-provider registration.
3. Add both the server jar and your custom tool jar to the runtime classpath.
4. Start the server and let it discover and register the tool automatically.
5. Connect with an MCP client and call the published tool through the exposed MCP interface.

## Client Examples

### Claude Desktop STDIO Configuration

```json
{
  "mcpServers": {
    "stdio-mcp-server": {
      "command": "java",
      "args": [
        "-cp",
        "/path/to/your/machai-mcp-server.jar:/path/to/your/functional-tool-container.jar",
        "org.machanism.machai.mcp.server.McpServer"
      ],
      "env": {
        "gw_model": "CodeMie:gpt-5.4-2026-03-05",
        "embedding_model": "CodeMie:text-embedding-005",
        "GENAI_USERNAME": "your_username",
        "GENAI_PASSWORD": "your_password"
      }
    }
  }
}
```

### HTTP MCP Server

Start an HTTP MCP server:

```bash
java -cp /path/to/your/machai-mcp-server.jar:/path/to/your/functional-tool-container.jar org.machanism.machai.mcp.server.McpServer --port 45000
```

Connect through a client that supports remote MCP endpoints. For Claude Desktop, one option is `mcp-remote`:

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

## Testing

Use MCP Inspector to validate tool registration, prompt exposure, and runtime behavior:

```bash
npx @modelcontextprotocol/inspector
```

For CodeMie Code, configure a local HTTP server with:

```bash
codemie mcp add --scope project mcp-remote-server "http://localhost:45000/mcp"
```

Then launch the CLI:

```bash
codemie-claude
```

## Resources

- Official platform: [https://machai.machanism.org/](https://machai.machanism.org/)
- Project site: [https://machai.machanism.org/machai-mcp-server/](https://machai.machanism.org/machai-mcp-server/)
- GitHub repository: [https://github.com/machanism-org/machai](https://github.com/machanism-org/machai)
- Maven Central: [org.machanism.machai:machai-mcp-server](https://central.sonatype.com/artifact/org.machanism.machai/machai-mcp-server)
- Machai Functional Tools documentation: [https://machai.machanism.org/genai-client/functional-tools.html](https://machai.machanism.org/genai-client/functional-tools.html)
- Model Context Protocol: [https://modelcontextprotocol.io/](https://modelcontextprotocol.io/)
- Model Context Protocol Inspector: [https://modelcontextprotocol.io/docs/tools/inspector](https://modelcontextprotocol.io/docs/tools/inspector)
- Release downloads: [SourceForge releases](https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/)
