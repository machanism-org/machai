
[Connect to local MCP servers](https://modelcontextprotocol.io/docs/develop/connect-local-servers)

- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- Linux: `~/.config/Claude/claude_desktop_config.json`

# Stdio MCP Server

```json
{
  "mcpServers": {
    "stdio-mcp-server": {
      "command": "java",
      "args": [
        "-cp",
        "...\\mcp-machai-server-1.1.15-SNAPSHOT.jar;...\\bindex-core-1.1.15-SNAPSHOT.jar",
        "org.machanism.machai.mcp.server.StdioMcpServer"
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

```bash
set gw_model = CodeMie:gpt-5.4-2026-03-05
set embedding_model = CodeMie:text-embedding-005
set GENAI_USERNAME = ...
set GENAI_PASSWORD = ...
set BINDEX_REG_PASSWORD = ...

java -cp mcp-machai-server-1.1.15-SNAPSHOT.jar;...\\bindex-core-1.1.15-SNAPSHOT.jar org.machanism.machai.mcp.server.RemoteMcpServer "mcp-server" 45450
```

# HTTP MPC Server

```json
  "mcpServers": {
    "remote-mpc-server": {
      "command": "npx",
      "args": [
        "mcp-remote",
        "http://localhost:45450/mcp"
      ]
    }
  }
```
