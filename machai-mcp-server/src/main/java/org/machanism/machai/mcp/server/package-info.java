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
 * Implements Machai's synchronous Model Context Protocol (MCP) server runtime
 * and the adapters that expose configured Machai functions through MCP.
 * <p>
 * A server loads function tools through {@code FunctionToolsLoader}, optionally
 * restricts them with the {@code enabledTools} configuration property, and
 * registers the resulting MCP capabilities before starting its transport.
 * {@link GenericGenaiAdapter} translates tool parameter descriptors into JSON
 * Schema, creates the transport-specific tool specification, and invokes the
 * underlying {@code ToolFunction} with request arguments, the configured
 * project directory, and the active configurator. When the synchronous exchange
 * supplies a session identifier, tool invocations also receive it. Prompt
 * adapters create MCP prompt specifications and convert function results into
 * prompt messages; streamable HTTP prompts receive their exchange session
 * identifier as well.
 * </p>
 *
 * <h2>Server transports</h2>
 * <ul>
 * <li>{@link StdioMcpServer} hosts one synchronous MCP session on standard
 * input and output and advertises tools, prompts, and logging.</li>
 * <li>{@link HttpStatelessMcpServer} hosts stateless synchronous MCP requests
 * in a Jetty servlet and advertises tools, prompts, and resources. Its
 * {@link HttpStatelessGenericGenaiAdapter} registers resource read handlers in
 * addition to tools and prompts.</li>
 * <li>{@link HttpStreamableMcpServer} hosts streamable synchronous MCP requests
 * in a Jetty servlet and advertises tools and prompts.</li>
 * </ul>
 * <p>
 * {@link AbstractMcpServer} owns the project-directory setting and configured
 * tool filtering shared by all server implementations. For HTTP transports,
 * {@link AbstractHttpMcpServer} creates the Jetty connector and installs the
 * MCP transport servlet. {@link AbstractPromptGenaiAdapter} supplies common
 * prompt registration, while {@link StdioGenaiAdapter} and
 * {@link HttpStreamableGenericGenaiAdapter} bind it to their respective
 * synchronous exchanges.
 * </p>
 *
 * <h2>Starting a server</h2>
 * <p>
 * {@link McpServer} is the command-line entry point. Use {@code -n} or
 * {@code --name} and {@code -v} or {@code --version} to set advertised server
 * metadata; {@code -d} or {@code --projectDir} to supply the directory passed
 * to tools; and {@code -c} or {@code --config} to select the properties file.
 * Without {@code -p} or {@code --port}, it starts {@link StdioMcpServer}. With
 * a port it starts {@link HttpStatelessMcpServer}; include {@code -s} or
 * {@code --session} to start {@link HttpStreamableMcpServer} instead.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.mcp.server;
