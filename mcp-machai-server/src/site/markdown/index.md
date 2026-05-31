
# MPC Machai Server

[![Maven Central](https://img.shields.io/maven-central/v/org.machanism.machai/mcp-machai-server.svg)](https://central.sonatype.com/artifact/org.machanism.machai/mcp-machai-server)

## Introduction

[Connect to local MCP servers](https://modelcontextprotocol.io/docs/develop/connect-local-servers)

- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- Linux: `~/.config/Claude/claude_desktop_config.json`

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
        "org.machanism.machai.mcp.server.StdioMcpServer",
        "-n",
        "<SERVER_NAME>",
        "-v",
        "<VERSION>"
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

java -cp mcp-machai-server-1.1.15-SNAPSHOT.jar;...\\bindex-core-1.1.15-SNAPSHOT.jar org.machanism.machai.mcp.server.RemoteMcpServer -n <SERVER_NAME> -v <VERSION> -p <PORT>
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
