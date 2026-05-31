/**
 * Provides the core server implementation for the Model Context Protocol (MCP) using the Machai AI framework.
 * <p>
 * The {@code org.machanism.machai.mcp} package enables integration with GenAI tools and supports both HTTP and STDIO interfaces
 * for AI-powered tool execution and orchestration. It is designed for extensibility, allowing seamless addition of new tools
 * and capabilities for advanced AI-driven workflows.
 * </p>
 *
 * <h2>Package Structure</h2>
 * <ul>
 *   <li><b>Server Entry Points:</b>
 *     <ul>
 *       <li>{@link org.machanism.machai.mcp.McpServer} &ndash; Main entry point for launching the server in either STDIO or Remote (HTTP) mode.</li>
 *       <li>{@link org.machanism.machai.mcp.RemoteMcpServer} &ndash; Configures and runs the MCP server over HTTP.</li>
 *       <li>{@link org.machanism.machai.mcp.StdioMcpServer} &ndash; Configures and runs the MCP server over standard input/output.</li>
 *     </ul>
 *   </li>
 *   <li><b>Tool Integration:</b>
 *     <ul>
 *       <li>{@link org.machanism.machai.mcp.GenericGenaiAdapter} &ndash; Generic adapter for registering and managing GenAI tools.</li>
 *       <li>{@link org.machanism.machai.mcp.ToolSpecificationBuilder}, {@link org.machanism.machai.mcp.RemoteToolSpecificationBuilder}, {@link org.machanism.machai.mcp.StdioToolSpecificationBuilder} &ndash; Builders for tool specifications, enabling flexible tool registration for different server types.</li>
 *     </ul>
 *   </li>
 *   <li><b>Utilities:</b>
 *     <ul>
 *       <li>Integration with {@code FunctionToolsLoader} and {@code PropertiesConfigurator} for dynamic tool loading and configuration.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>
 * To start the server, use the {@link org.machanism.machai.mcp.McpServer} class with appropriate command-line options:
 * </p>
 * <ul>
 *   <li>{@code -h}, {@code --help}: Show help message and exit.</li>
 *   <li>{@code -n}, {@code --name}: Specify the MCP server name (default: mcp-machai-server).</li>
 *   <li>{@code -v}, {@code --version}: Specify the MCP server version (default: implementation version or "latest").</li>
 *   <li>{@code -p}, {@code --port}: Specify the port number for Remote MCP Server mode (HTTP).</li>
 * </ul>
 * <p>
 * If the {@code --port} option is provided, the server runs in remote (HTTP) mode; otherwise, it runs in STDIO mode for local integration.
 * </p>
 *
 * <h2>Extensibility</h2>
 * <p>
 * New tools can be registered using the {@link org.machanism.machai.mcp.GenericGenaiAdapter} and appropriate {@link org.machanism.machai.mcp.ToolSpecificationBuilder} implementations,
 * allowing rapid integration of new AI capabilities and custom workflows.
 * </p>
 *
 * @author Machanism Team
 * @since 1.1.15
 */
package org.machanism.machai.mcp;