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
 * Provides Machai's executable synchronous Model Context Protocol (MCP) server
 * implementations and their transport adapters.
 * <p>
 * Servers load {@code FunctionTools} and expose their tools and prompts through
 * MCP. {@link GenericGenaiAdapter} converts each tool's parameter descriptors
 * to a JSON Schema object, creates the transport-specific tool specification,
 * and invokes the underlying {@code ToolFunction} with request arguments, the
 * configured project directory, and the active configurator. Prompt adapters
 * register prompt specifications and convert function results into MCP prompt
 * messages. Streamable HTTP prompt calls receive the MCP session identifier;
 * tool calls receive it whenever their exchange provides one. The optional
 * {@code enabledTools} configuration property limits which discovered function
 * tools are registered.
 * </p>
 *
 * <h2>Available transports</h2>
 * <ul>
 * <li>{@link StdioMcpServer} runs a single synchronous session over standard
 * input and output, with tool, prompt, and logging capabilities.</li>
 * <li>{@link HttpStatelessMcpServer} serves stateless synchronous requests from
 * a Jetty servlet and supports tools, prompts, and resources. Its
 * {@link HttpStatelessGenericGenaiAdapter} also registers resource handlers.</li>
 * <li>{@link HttpStreamableMcpServer} serves streamable synchronous requests
 * from a Jetty servlet and supports tools and prompts.</li>
 * </ul>
 * <p>
 * {@link AbstractMcpServer} centralizes the project-directory setting and tool
 * filtering shared by every server. {@link AbstractHttpMcpServer} supplies the
 * Jetty connector and servlet hosting used by the HTTP implementations.
 * {@link AbstractPromptGenaiAdapter}, {@link StdioGenaiAdapter}, and
 * {@link HttpStreamableGenericGenaiAdapter} provide the prompt and
 * transport-specific integration used alongside the generic tool adapter.
 * </p>
 *
 * <h2>Command-line use</h2>
 * <p>
 * Start {@link McpServer} with {@code -n}/{@code --name} and
 * {@code -v}/{@code --version} to set advertised server metadata,
 * {@code -d}/{@code --projectDir} to set the tool project directory, and
 * {@code -c}/{@code --config} to choose a properties file. Without
 * {@code -p}/{@code --port}, it starts {@link StdioMcpServer}. A port starts
 * {@link HttpStatelessMcpServer}; add {@code -s}/{@code --session} to select
 * {@link HttpStreamableMcpServer}. The selected server loads and registers its
 * configured function tools before it starts.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.mcp.server;
