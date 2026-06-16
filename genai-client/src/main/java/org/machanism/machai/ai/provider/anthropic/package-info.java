/*-
 * @guidance:
 *
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package's overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 *      - Do not use escaping in `{@code ...}` tags.    
 */

/**
 * Provides the Anthropic-backed implementation of MachAI's generative AI provider abstraction.
 *
 * <p>
 * This package contains {@link org.machanism.machai.ai.provider.anthropic.AnthropicProvider},
 * which adapts the Anthropic Java SDK to MachAI's {@link org.machanism.machai.ai.provider.Genai}
 * interface. It enables applications to send prompts to Anthropic Claude models, register
 * custom function tools, configure web search, connect MCP servers, and track token usage.
 * </p>
 *
 * <h2>Key Components</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.provider.anthropic.AnthropicProvider} &mdash;
 *       The primary entry point. Builds and sends requests to the Anthropic Beta Messages API,
 *       handles tool-use loops, parses responses, and captures usage statistics.</li>
 * </ul>
 *
 * <h2>Configuration Properties</h2>
 * <ul>
 *   <li>{@code chatModel} (required): Claude model identifier, e.g. {@code "claude-3-opus-20240229"}.</li>
 *   <li>{@code ANTHROPIC_API_KEY} (required): Anthropic API key or auth token.</li>
 *   <li>{@code ANTHROPIC_BASE_URL} (optional): Override the default Anthropic API base URL
 *       (useful for proxy or compatible endpoints).</li>
 *   <li>{@code GENAI_TIMEOUT} (optional): Request timeout in seconds. 0 or absent means SDK default.</li>
 *   <li>{@code MAX_OUTPUT_TOKENS} (optional): Maximum tokens in the model response. Defaults to
 *       the value defined in {@code AbstractAIProvider}.</li>
 *   <li>{@code MAX_TOOL_CALLS} (optional): Maximum number of tool calls per response. 0 leaves
 *       the limit unset.</li>
 *   <li>{@code cacheThreshold} (optional): Tool result character length above which prompt caching
 *       ({@code BetaCacheControlEphemeral}) is applied automatically.</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Obtain a Configurator with the required properties
 * Configurator config = ...; // contains ANTHROPIC_API_KEY, chatModel, etc.
 *
 * AnthropicProvider provider = new AnthropicProvider();
 * provider.init("claude-3-opus-20240229", config);
 *
 * // Optionally register a custom tool
 * provider.addTool(
 *     "get_weather",
 *     "Returns current weather for a given city",
 *     (params, projectDir) -&gt; fetchWeather(params),
 *     new ParamDescriptor("city", "string", "City name", true)
 * );
 *
 * // Send a prompt and receive the response
 * provider.prompt("What is the weather in Berlin today?");
 * String response = provider.perform();
 * </pre>
 *
 * <h2>Tool-Use Loop</h2>
 * <p>
 * When the model responds with one or more tool-use blocks, {@code AnthropicProvider} automatically
 * invokes the matching registered {@link org.machanism.machai.ai.tools.ToolFunction}, appends the
 * result to the conversation, and re-submits the request until the model returns a plain text
 * response.
 * </p>
 *
 * <h2>Web Search Support</h2>
 * <p>
 * Web search can be enabled by calling {@code addWebSearch} with a supported tool version
 * ({@code "20260209"} or {@code "20250305"}) and optional user location hints (city, country,
 * region). The corresponding {@code BetaWebSearchTool} is then included in every request.
 * </p>
 *
 * <h2>MCP Server Support</h2>
 * <p>
 * External MCP (Model Context Protocol) servers can be registered via {@code addMcpServer},
 * supplying a name, URL, optional authorization token, and description. All registered servers
 * are forwarded with each request via {@code MessageCreateParams}.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.1.13
 */
package org.machanism.machai.ai.provider.anthropic;
