
# MPC Machai Server

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/mcp-machai-server.svg)](https://central.sonatype.com/artifact/org.machanism.machai/mcp-machai-server)

## Introduction

MPC Machai Server uses Java's [Service Provider Interface (SPI)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) mechanism to discover and load functional tools at runtime.  
When the server starts, it scans the classpath for JAR files that provide functional tool implementations according to the [Machai Functional Tools SPI specification](https://machai.machanism.org/genai-client/functional-tools.html).

This design allows you to extend the server with your own tools without modifying or recompiling the core server code.  
Simply package your tool implementations in a JAR file and ensure they are discoverable via SPI.

### Publishing Function Tools from User JAR Files

To publish your own functional tool:

1. **Follow the Machai Functional Tools SPI Specification**

   Implement your tool as described in the [How to create a custom functional tool](https://machai.machanism.org/genai-client/functional-tools.html#How_to_create_a_custom_functional_tool).  
   This typically involves:
   - Implementing the required interfaces.
   - Providing a `META-INF/services` entry in your JAR for SPI discovery.

2. **Package Your JAR**

   Build your JAR with the compiled classes and the correct `META-INF/services` entries.

3. **Add Your JAR to the Server Classpath**

   When starting the MPC Machai Server, include your JAR in the classpath:
   ```bash
   java -cp mcp-machai-server-1.1.15-SNAPSHOT.jar;your-tools.jar org.machanism.machai.mcp.McpServer
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
        "...\\mcp-machai-server-1.1.15-SNAPSHOT.jar;...\\bindex-core-1.1.15-SNAPSHOT.jar",
        "org.machanism.machai.mcp.McpServer"
      ],
      "env": {
        "gw_model": "CodeMie:gpt-5.4-2026-03-05",
        "embedding_model": "CodeMie:text-embedding-005",
        "GENAI_USERNAME": "...",
        "GENAI_PASSWORD": "...",
        "BINDEX_REG_PASSWORD": "..."
      }
    }
  }
  ...
}
```

## HTTP MPC Server

### Start MCP Server

```bash
set gw_model = CodeMie:gpt-5.4-2026-03-05
set embedding_model = CodeMie:text-embedding-005
set GENAI_USERNAME = ...
set GENAI_PASSWORD = ...
set BINDEX_REG_PASSWORD = ...

java -cp mcp-machai-server-1.1.15-SNAPSHOT.jar;...\\bindex-core-1.1.15-SNAPSHOT.jar org.machanism.machai.mcp.McpServer -p <PORT>
```

### Claude Desktop Configuration

```json
{
  "mcpServers": {
    "<SERVER_NAME>": {
      "command": "npx",
      "args": [
        "mcp-remote",
        "http://localhost:<PORT>/mcp"
      ]
    }
  }
...
}
```
.