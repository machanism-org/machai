
# MCP Machai Server

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/mcp-machai-server.svg)](https://central.sonatype.com/artifact/org.machanism.machai/mcp-machai-server)

## Introduction

MCP Machai Server uses Java's [Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) mechanism to discover and load functional tools at runtime.  
When the server starts, it scans the classpath for JAR files that provide functional tool implementations according to the [Machai Functional Tools SPI specification](https://machai.machanism.org/genai-client/functional-tools.html).

This design allows you to extend the server with your own tools without modifying or recompiling the core server code.  
Simply package your tool implementations in a JAR file and ensure they are discoverable via SPI.

Download the Machai MCP Server:

[![Download Machai MCP Server](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/machanism/files/machai/mcp-machai-server/releases)

## Publishing Function Tools from User JAR Files

To publish your own functional tool:

1. **Follow the Machai Functional Tools SPI Specification**

   Implement your tool as described in the [How to create a custom functional tool](https://machai.machanism.org/genai-client/functional-tools.html#How_to_create_a_custom_functional_tool).  
   This typically involves:
   - Implementing the required interfaces.
   - Providing a `META-INF/services` entry in your JAR for SPI discovery.

2. **Package Your JAR**

   Build your JAR with the compiled classes and the correct `META-INF/services` entries.

3. **Add Your JAR to the Server Classpath**

   When starting the MCP Machai Server, include your JAR in the classpath:
   ```bash
   java -cp /absolute/path/to/your/mcp-machai-server.jar:/absolute/path/to/your/functional-tool-container.jar org.machanism.machai.mcp.McpServer
   ```

4. **Verify Tool Registration**

   On startup, the server will automatically discover and register your functional tools.  
   You can now invoke your custom tools via MCP.

**For detailed instructions and examples, see:**  
[Machai Functional Tools SPI documentation](https://machai.machanism.org/genai-client/functional-tools.html)

## Stdio MCP Server

### Claude Desktop Configuration

```json
{
  "mcpServers": {
    "stdio-mcp-server": {
      "command": "java",
      "args": [
        "-cp",
        "/absolute/path/to/your/mcp-machai-server.jar:/absolute/path/to/your/functional-tool-container.jar",
        "org.machanism.machai.mcp.McpServer"
      ],
      "env": {
        "gw_model": "CodeMie:gpt-5.4-2026-03-05",
        "embedding_model": "CodeMie:text-embedding-005",
        "GENAI_USERNAME": "your_username",
        "GENAI_PASSWORD": "your_password"
        // Add other properties here if needed
      }
    }
  }
}
```

## HTTP MCP Server

### Start MCP Server

```bash
export gw_model=CodeMie:gpt-5.4-2026-03-05
export embedding_model=CodeMie:text-embedding-005
export GENAI_USERNAME=your_username
export GENAI_PASSWORD=your_password
export BINDEX_REG_PASSWORD=your_bindex_password

java -cp /absolute/path/to/your/mcp-machai-server.jar:/absolute/path/to/your/functional-tool-container.jar org.machanism.machai.mcp.McpServer -p 8080
```

### Claude Desktop Configuration

```json
{
  "mcpServers": {
    "localMcpServer": {
      "command": "npx",
      "args": [
        "mcp-remote",
        "http://localhost:8080/mcp"
      ]
    }
  }
  // Add other properties here if needed
}
```

## Testing

### MCP Inspector

The MCP Inspector is an interactive developer tool for testing and debugging MCP servers. While the Debugging Guide covers the Inspector as part of the overall debugging toolkit, 
this document provides a detailed exploration of the Inspector’s features and capabilities.

See: [MCP Inspector](https://modelcontextprotocol.io/docs/tools/inspector)

```bash
npx -y @modelcontextprotocol/inspector
```

![MCP Inspector](images/mcp-inspector.png)

### Claude Desktop

Claude Desktop is Anthropic's official standalone application designed to bring advanced AI capabilities directly to your computer. 
It moves beyond basic web-browser chatting by offering deep operating system integration, local file management, and powerful agentic features.

See more: [Desktop application](https://code.claude.com/docs/en/desktop)

![Claude Desktop](images/claude-desktop.png)
.