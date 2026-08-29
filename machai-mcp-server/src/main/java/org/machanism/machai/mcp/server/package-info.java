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
 * Provides Machai's executable Model Context Protocol (MCP) server integration.
 * <p>
 * The package discovers Machai {@code FunctionTools}, converts their parameter
 * descriptors into MCP JSON schemas, and registers synchronous MCP tool
 * handlers. Each handler invokes the underlying {@code ToolFunction} with the
 * request arguments, configured project directory, and {@code Configurator}.
 * For synchronous exchanges, the handler also supplies the MCP session ID as a
 * tool argument. Tool registrations can be limited through the
 * {@code enabledTools} configuration property.
 * </p>
 *
 * <h2>Available transports</h2>
 * <ul>
 * <li>{@link StdioMcpServer} provides a single synchronous session over standard
 * input and output, with tools, prompts, and logging capabilities.</li>
 * <li>{@link HttpStatelessMcpServer} provides stateless HTTP requests through
 * Jetty, with tools, prompts, and resources.</li>
 * <li>{@link HttpStreamableMcpServer} provides streamable HTTP through Jetty,
 * with synchronous session context, tools, and prompts.</li>
 * </ul>
 * <p>
 * {@link AbstractMcpServer} supplies the common lifecycle contract and
 * project-directory configuration; {@link AbstractHttpMcpServer} adds Jetty
 * connector and servlet setup for HTTP transports. The transport adapters
 * ({@link StdioGenaiAdapter},
 * {@link HttpStatelessGenericGenaiAdapter}, and
 * {@link HttpStreamableGenericGenaiAdapter}) specialize tool specifications for
 * their exchange types, while {@link GenericGenaiAdapter} contains the shared
 * schema-generation and invocation logic.
 * </p>
 *
 * <h2>Command-line startup</h2>
 * <p>
 * {@link McpServer} is the command-line entry point. Its
 * {@link McpServer#main(String[]) main} method accepts the server name
 * ({@code -n}), version ({@code -v}), project directory ({@code -d}), optional
 * configuration file ({@code -c}), and HTTP port ({@code -p} or
 * {@code --port}). Without a port, it starts the STDIO transport. With a port,
 * it starts stateless HTTP by default; {@code -s} or {@code --session} selects
 * streamable HTTP. Function tools are discovered, configured, and registered
 * before the selected transport starts.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.mcp.server;
