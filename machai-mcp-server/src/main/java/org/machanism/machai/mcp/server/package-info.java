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
 * Provides Machai's synchronous Model Context Protocol (MCP) server runtime,
 * command-line launcher, and transport adapters. The package loads function
 * definitions and publishes them as MCP tools and prompts; its stateless HTTP
 * adapter also publishes resource-read handlers.
 * <p>
 * {@link AbstractMcpServer} is the common base for all transports. It stores
 * the optional project directory passed to functions and filters registered
 * tools using the optional {@code enabledTools} configuration property. Each
 * concrete server uses {@code FunctionToolsLoader} to discover function
 * definitions and an adapter to create transport-specific MCP
 * specifications.
 * </p>
 *
 * <h2>Tool and prompt adapters</h2>
 * <p>
 * {@link GenericGenaiAdapter} converts {@code ParamDescriptor} instances into
 * JSON Schema, registers tool handlers, and invokes each {@code ToolFunction}
 * with request arguments, the configured project directory, and the active
 * configurator. When a handler receives an
 * {@code McpSyncServerExchange}, it also supplies the exchange session
 * identifier as a function argument. {@link AbstractPromptGenaiAdapter}
 * performs the corresponding prompt registration and converts function
 * results into MCP prompt messages. The concrete
 * {@link StdioGenaiAdapter}, {@link HttpStreamableGenericGenaiAdapter}, and
 * {@link HttpStatelessGenericGenaiAdapter} classes bind those registrations to
 * STDIO, streamable HTTP, and stateless HTTP respectively; the stateless
 * adapter additionally registers resource handlers.
 * </p>
 *
 * <h2>Server transports</h2>
 * <ul>
 * <li>{@link StdioMcpServer} communicates through standard input and output,
 * enables tools, prompts, and logging, and installs a JVM shutdown hook to
 * close the built synchronous server.</li>
 * <li>{@link HttpStatelessMcpServer} exposes stateless synchronous MCP
 * requests through a Jetty servlet and supports tools, prompts, and
 * resources. Resource functions receive the project directory, configurator,
 * and requested resource URI.</li>
 * <li>{@link HttpStreamableMcpServer} exposes streamable synchronous MCP
 * requests through a Jetty servlet and supports tools and prompts.</li>
 * </ul>
 * {@link AbstractHttpMcpServer} configures the Jetty connector and selected
 * transport servlet. HTTP startup failures are wrapped in
 * {@link McpServerStartupException}.
 *
 * <h2>Starting a server</h2>
 * <p>
 * {@link McpServer} is the command-line entry point. Use {@code -n} or
 * {@code --name}, {@code -v} or {@code --version}, and {@code -d} or
 * {@code --projectDir} to set server metadata and the function project
 * directory. Use {@code -c} or {@code --config} to select a properties file;
 * when omitted, {@code mcp.properties} is attempted. Without {@code -p} or
 * {@code --port}, the launcher starts {@link StdioMcpServer}. With a port, it
 * starts {@link HttpStatelessMcpServer}, or starts
 * {@link HttpStreamableMcpServer} when {@code -s} or {@code --session} is
 * specified.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.mcp.server;
