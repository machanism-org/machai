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
 * Implements Machai's executable Model Context Protocol (MCP) server.
 * <p>
 * The package exposes Machai {@code FunctionTools} as synchronous MCP tools,
 * prompts, and, for the stateless HTTP transport, resources. Tool parameter
 * descriptors are converted to JSON Schema input properties before a
 * transport-specific specification is registered. At invocation time, the
 * adapter passes the request arguments, configured project directory, and
 * configurator to the underlying {@code ToolFunction}; synchronous exchanges
 * also contribute their session identifier. The {@code enabledTools}
 * configuration property optionally restricts the discovered tools.
 * </p>
 *
 * <h2>Server transports</h2>
 * <ul>
 * <li>{@link StdioMcpServer} serves one synchronous MCP session through standard
 * input and output and advertises tool, prompt, and logging capabilities.</li>
 * <li>{@link HttpStatelessMcpServer} hosts stateless synchronous MCP requests in
 * Jetty and advertises tool, prompt, and resource capabilities.</li>
 * <li>{@link HttpStreamableMcpServer} hosts streamable synchronous MCP requests
 * in Jetty and advertises tool and prompt capabilities.</li>
 * </ul>
 * <p>
 * {@link AbstractMcpServer} defines the shared lifecycle, project-directory
 * setting, and enabled-tool filtering. {@link AbstractHttpMcpServer} configures
 * the Jetty connector and servlet hosting used by both HTTP servers.
 * {@link GenericGenaiAdapter} supplies common tool schema creation and handler
 * invocation; {@link AbstractPromptGenaiAdapter} adds prompt registration.
 * {@link StdioGenaiAdapter}, {@link HttpStatelessGenericGenaiAdapter}, and
 * {@link HttpStreamableGenericGenaiAdapter} create the specifications required
 * by their respective transport APIs.
 * </p>
 *
 * <h2>Starting a server</h2>
 * <p>
 * {@link McpServer} is the command-line entry point. Use {@code -n} and
 * {@code -v} to set the advertised name and version, {@code -d} to supply a
 * project directory, and {@code -c} to select a properties file. With no
 * {@code -p}/{@code --port} option, it starts the STDIO server. Supplying a
 * port starts {@link HttpStatelessMcpServer}; add {@code -s}/{@code --session}
 * to select {@link HttpStreamableMcpServer}. Before startup, the selected server
 * loads and registers the configured function tools.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.mcp.server;
