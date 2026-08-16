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
 * Provides the executable Model Context Protocol (MCP) server integrations for
 * Machai.
 * <p>
 * The package adapts Machai function tools to the MCP Java SDK. It builds tool
 * names, descriptions, JSON-compatible parameter schemas, and synchronous call
 * handlers, then registers the resulting specifications with the selected MCP
 * transport. A handler passes request arguments to the underlying
 * {@code ToolFunction} together with the configured project directory and
 * {@code Configurator}; streamable exchanges also expose the MCP session ID to
 * the function.
 * </p>
 *
 * <h2>Available transports</h2>
 * <ul>
 * <li>{@link StdioMcpServer} uses standard input and output for a single
 * synchronous MCP session.</li>
 * <li>{@link HttpStatelessMcpServer} serves MCP requests over Jetty HTTP
 * without retaining session state and supports tools, prompts, and resources.</li>
 * <li>{@link HttpStreamableMcpServer} serves the streamable HTTP transport with
 * synchronous session context and supports tools and prompts.</li>
 * </ul>
 * <p>
 * {@link AbstractMcpServer} supplies the common server contract, Machai server
 * metadata, and project-directory configuration. HTTP implementations inherit
 * Jetty connector and servlet setup from {@link AbstractHttpMcpServer}. The
 * transport adapters ({@link StdioGenaiAdapter},
 * {@link HttpStatelessGenericGenaiAdapter}, and
 * {@link HttpStreamableGenericGenaiAdapter}) specialize tool specifications for
 * their respective exchange types. {@code GenericGenaiAdapter} contains the
 * shared schema and invocation logic.
 * </p>
 *
 * <h2>Command-line startup</h2>
 * <p>
 * {@link McpServer} is the command-line entry point. Its
 * {@link McpServer#main(String[]) main} method accepts the server name
 * ({@code -n}), version ({@code -v}), project directory ({@code -d}), and
 * optional configuration file ({@code -c}). Without {@code -p} or
 * {@code --port}, it starts STDIO mode. Supplying a port starts stateless HTTP
 * mode; adding {@code -s} or {@code --session} selects streamable HTTP mode.
 * Function tools are discovered by {@code FunctionToolsLoader}, configured,
 * and registered before the transport starts.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.mcp.server;
