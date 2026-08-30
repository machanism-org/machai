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

Machai MCP Server is a Java 17 implementation of the Model Context Protocol (MCP) built on the Machai AI framework. It bridges MCP-compatible clients and Machai functional tools, enabling AI assistants and automation clients to discover, describe, and execute tool capabilities through a standard protocol interface.

## Introduction

Machai MCP Server is intentionally focused on orchestration rather than bundling built-in tools. It publishes tools and prompts supplied by additional libraries on the runtime classpath, making it suitable as a reusable MCP gateway for custom automation, coding, data, or internal platform tools. The same application supports local desktop integration over standard input/output and remote access over HTTP.

The server turns Machai functional tool implementations into MCP-accessible capabilities with minimal runtime setup. It handles bootstrap, transport selection, tool discovery, MCP schema adaptation, prompt exposure, and request routing, while independently packaged extensions provide domain-specific behavior.

Its architecture comprises a command-line bootstrap layer; shared server setup and project-context management; STDIO and HTTP transports; an adapter that converts discovered Machai tools and prompts into MCP definitions and handlers; and embedded web hosting for HTTP deployments.

## Key Features

- Starts as either a STDIO or HTTP MCP server from the same Java entry point.
- Supports stateless and streamable, session-aware HTTP modes.
- Discovers custom Machai functional tools and prompts from libraries on the runtime classpath.
- Converts tool metadata and parameter definitions into MCP-compatible schemas automatically.
- Configures the server name, version, and optional project directory at launch time.
- Uses Maven-based packaging, including an assembly profile for a jar with dependencies.
- Keeps domain-specific capabilities decoupled so deployments can add or replace tools without changing server code.

## Usage

### Prerequisites

Before running Machai MCP Server, ensure you have:

- Java 17 or later.
- Maven, if building from source.
- An MCP-compatible client that can communicate over STDIO or HTTP.
- One or more Machai-compatible functional tool or prompt libraries on the runtime classpath; the server does not publish built-in tools by itself.
- A target project workspace when tools need to inspect or modify project files.
- Any credentials, environment variables, model names, or service settings required by the loaded tools.
- Network access and an available TCP port when running HTTP mode.

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

Run the server in STDIO mode by placing the server jar and at least one functional tool container jar on the classpath:

```bash
java -cp /path/to/machai-mcp-server.jar:/path/to/functional-tool-container.jar org.machanism.machai.mcp.server.McpServer --projectDir /path/to/project
```

Run the server in HTTP stateless mode:

```bash
java -cp /path/to/machai-mcp-server.jar:/path/to/functional-tool-container.jar org.machanism.machai.mcp.server.McpServer --port 8080 --projectDir /path/to/project
```

When using external Machai tool containers, place the server jar and tool libraries on the runtime classpath:

```bash
java -cp /path/to/machai-mcp-server.jar:/path/to/functional-tool-container.jar org.machanism.machai.mcp.server.McpServer --projectDir /path/to/project
```

### Typical Workflow

1. Download a packaged release or build the server from source.
2. Prepare Machai-compatible functional tool libraries with the required service-provider registration.
3. Add the server artifact and tool libraries to the Java runtime classpath.
4. Export the environment variables or credentials required by the selected tools and AI providers.
5. Start STDIO mode for local integrations, or provide `--port` for HTTP access.
6. Optionally pass `--projectDir` so tools have a known project context.
7. Connect an MCP client, verify available tools and prompts, and invoke tools through the MCP interface.

### Java Version

The project is compiled with `maven.compiler.release` set to Java 17. Runtime environments must provide Java 17 or newer. Functional operation also depends on the additional tool libraries supplied at runtime and any services, credentials, model configuration, or project files those tools require.

## Configuration

### Command-Line Options

The application accepts Apache Commons CLI options in short or long form. If no port is provided, it starts in STDIO mode. If a port is provided, it starts as an HTTP MCP server. Adding the session option to HTTP mode selects the streamable server variant; otherwise HTTP mode is stateless. When no configuration file is supplied, the server attempts to load `mcp.properties`; a missing default file is tolerated, while an explicitly supplied file must be readable.

| Option | Description | Default value |
| --- | --- | --- |
| `-h`, `--help` | Show the help message and exit. | Not enabled |
| `-d`, `--projectDir <path>` | Specify the project directory path used as the workspace context for tool execution. In HTTP mode, if omitted, the project directory is determined from the client request when possible. | Not set |
| `-n`, `--name <name>` | Specify the MCP server name advertised to clients. | `mcp-machai-server` |
| `-c`, `--config <path>` | Specify the configuration file path used to initialize server properties. | `mcp.properties`; a missing default file is tolerated |
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
