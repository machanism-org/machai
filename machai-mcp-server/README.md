<!-- @guidance:
**Important:** If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
1. **Project Title and Overview:**  
   - Provide the project name and a brief description based on `src\\site\\markdown\\index.md` content summary.
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

Machai MCP Server is a Java 17 server implementation for the Model Context Protocol (MCP) built on the Machai AI framework. It bridges MCP-compatible clients with Machai functional tools, enabling AI assistants and automation clients to discover, describe, and execute runtime-provided tool capabilities through standard STDIO or HTTP MCP transports.

## Introduction

Machai MCP Server focuses on orchestration rather than bundling built-in tools. It publishes tools and prompts supplied by additional libraries on the runtime classpath, making it suitable for teams that need a reusable MCP gateway for custom automation, coding, data, or internal platform tools. The same application can be used for local desktop integration over standard input/output or for remote access over HTTP.

The core value of the project is that it turns Machai functional tool implementations into MCP-accessible capabilities with minimal runtime setup. It handles server bootstrap, transport selection, tool discovery, MCP schema adaptation, prompt exposure, and request routing while leaving domain-specific tool behavior in independently packaged extensions.

Key capabilities include:

- STDIO and HTTP MCP server modes from the same Java entry point
- Stateless HTTP and streamable session-oriented HTTP transport support
- Runtime discovery of Machai functional tools and prompts from classpath libraries
- Automatic conversion of tool metadata and parameter definitions into MCP-compatible schemas
- Configurable server name, version, port, session mode, and project-directory context
- Maven-based build and packaging for source builds and runnable distributions

## Usage

### Prerequisites

Before running Machai MCP Server, ensure you have:

- Java 17 or newer
- Maven, if building the project from source
- one or more Machai-compatible functional tool or prompt libraries on the runtime classpath
- any credentials, environment variables, model names, or service settings required by the loaded tools
- an MCP-compatible client such as Claude Desktop, MCP Inspector, CodeMie Code, or another STDIO or HTTP MCP client

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
java -cp /path/to/machai-mcp-server.jar:/path/to/functional-tool-container.jar org.machanism.machai.mcp.server.McpServer
```

Run the server in HTTP mode by providing a port:

```bash
java -cp /path/to/machai-mcp-server.jar:/path/to/functional-tool-container.jar org.machanism.machai.mcp.server.McpServer --port 45000
```

### Typical Workflow

1. Download a release package or build the server from source.
2. Prepare one or more Machai-compatible functional tool libraries with the required service-provider registration.
3. Add the server artifact and the tool libraries to the Java runtime classpath.
4. Export any environment variables or credentials required by the selected tools and AI providers.
5. Start the server in STDIO mode for local desktop integrations, or start it with `--port` for HTTP access.
6. Optionally pass `--projectDir` so tools have a known project context.
7. Connect an MCP client and verify that the expected tools and prompts are available.
8. Invoke tools through the MCP client and monitor logs when troubleshooting runtime behavior.

### Command-Line Options

| Option | Description | Default |
|---|---|---|
| `-h`, `--help` | Show the help message and print available options. | Not enabled |
| `-d`, `--projectDir <path>` | Set the project directory path used by tools as their execution context. | Not set; in HTTP mode it may be determined from the client request |
| `-n`, `--name <value>` | Set the MCP server name exposed to clients. | `mcp-machai-server` |
| `-v`, `--version <value>` | Set the MCP server version exposed to clients. | Package implementation version, or `latest` when package metadata is unavailable |
| `-p`, `--port <number>` | Start the server in HTTP mode and listen on the specified port. | Not set; STDIO mode is used |
| `-s`, `--session` | Use streamable MCP server mode for HTTP transport. | Disabled; stateless HTTP mode is used when `--port` is set |

If `--port` is omitted, the application starts in STDIO mode. If `--port` is provided, the application starts an HTTP server. When `--session` is provided together with `--port`, HTTP mode uses streamable transport; otherwise it uses stateless HTTP transport.

### Configuration Example

```bash
export gw_model=CodeMie:gpt-5.4-2026-03-05
export embedding_model=CodeMie:text-embedding-005
export GENAI_USERNAME=your_username
export GENAI_PASSWORD=your_password

java -cp /path/to/machai-mcp-server.jar:/path/to/functional-tool-container.jar org.machanism.machai.mcp.server.McpServer \
  --projectDir /path/to/project \
  --name my-mcp-server \
  --version 1.0.0 \
  --port 45000 \
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

- Official Machai platform: [https://machai.machanism.org/](https://machai.machanism.org/)
- GitHub repository: [https://github.com/machanism/machai](https://github.com/machanism/machai)
- Maven Central artifact: [https://central.sonatype.com/artifact/org.machanism.machai/machai-mcp-server](https://central.sonatype.com/artifact/org.machanism.machai/machai-mcp-server)
- Machai Functional Tools documentation: [https://machai.machanism.org/genai-client/functional-tools.html](https://machai.machanism.org/genai-client/functional-tools.html)
- SourceForge releases: [https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/](https://sourceforge.net/projects/machanism/files/machai/machai-mcp-server/releases/)
- Model Context Protocol Inspector: [https://modelcontextprotocol.io/docs/tools/inspector](https://modelcontextprotocol.io/docs/tools/inspector)
