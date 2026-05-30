
[Connect to local MCP servers](https://modelcontextprotocol.io/docs/develop/connect-local-servers)

- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- Linux: `~/.config/Claude/claude_desktop_config.json`

```json
{
  "mcpServers": {
    "bindex-mcp-server": {
      "command": "java",
      "args": [
         "-jar",
        "C:\\projects\\machanism.org\\machai\\bindex-mcp-server\\target\\bindex-mcp-server-1.1.15-SNAPSHOT.jar"
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