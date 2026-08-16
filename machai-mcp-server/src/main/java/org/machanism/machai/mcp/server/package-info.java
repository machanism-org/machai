/*-
 * @guidance:
 *
 * **IMPORTANT: UPDATE THIS `package-info.java`!**
 *
 * - Use Clear and Concise Descriptions:
 *     - Write meaningful summaries that explain the purpose, behavior, and usage of the package and its elements.
 *     - Avoid vague statements; be specific about functionality and intent.
 *
 * - Update `package-info.java`:
 *     - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose, scope, and usage based on package-info.java files located on child folders.
 *     - Place the package-level Javadoc immediately before the `package` declaration.
 *
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` and &gt; in `<pre>` content for Javadoc. 
 *     - Ensure that the code is properly escaped and formatted.
 *     
 * author: Viktor Tovstyi
 * since: 1.2.0
 */

/**
 * Provides Machai's executable Model Context Protocol (MCP) server layer.
 * <p>
 * The package starts an MCP server, loads Machai function tools, and publishes
 * those tools through MCP tool schemas and synchronous call handlers. Tool calls
 * receive the request arguments, the configured project directory, and the
 * active Machai configuration; streamable sessions additionally make the MCP
 * session identifier available to tool functions.
 * </p>
 *
 * <h2>Server modes</h2>
 * <ul>
 * <li>{@link StdioMcpServer} communicates with a host process through standard
 * input and output and supports one synchronous session.</li>
 * <li>{@link HttpStatelessMcpServer} exposes a stateless HTTP endpoint through
 * Jetty. It publishes tools, prompts, and resources without retaining MCP
 * session state.</li>
 * <li>{@link HttpStreamableMcpServer} exposes the streamable HTTP transport,
 * retaining session context for synchronous exchanges and publishing tools and
 * prompts.</li>
 * </ul>
 * <p>
 * {@link AbstractMcpServer} centralizes server metadata constants, tool loading
 * hooks, startup contracts, and project-directory configuration. HTTP variants
 * inherit Jetty connector and servlet setup from
 * {@link AbstractHttpMcpServer}. The transport-specific adapters
 * ({@link StdioGenaiAdapter}, {@link HttpStatelessGenericGenaiAdapter}, and
 * {@link HttpStreamableGenericGenaiAdapter}) translate Machai
 * {@code ToolFunction} and {@code ParamDescriptor} definitions into MCP tools;
 * adapters also translate supported prompt and resource definitions into their
 * corresponding MCP specifications.
 * </p>
 *
 * <h2>Starting the server</h2>
 * <p>
 * {@link McpServer} is the command-line entry point. Invoke its
 * {@link McpServer#main(String[]) main} method with the server name, version,
 * project directory, configuration file, and optional port and session options.
 * Without {@code -p} or {@code --port}, the application starts STDIO mode. With
 * a port it starts stateless HTTP mode, or streamable HTTP mode when
 * {@code -s} or {@code --session} is also supplied. Tool implementations are
 * discovered by {@code FunctionToolsLoader} and registered before startup.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.mcp.server;