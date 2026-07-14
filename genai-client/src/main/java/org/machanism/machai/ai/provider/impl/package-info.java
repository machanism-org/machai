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
 *        
 * author: Viktor Tovstyi
 * since: 1.2.0  
 */

/**
 * Provides concrete implementations of the Machai generative AI provider
 * abstraction.
 *
 * <p>
 * This package contains provider adapters that connect Machai's common AI
 * interfaces to specific runtime backends and tool execution strategies. The
 * implementations translate prompts, instructions, tool definitions, web-search
 * configuration, MCP server configuration, embedding requests, and usage
 * accounting between Machai's internal provider model and the corresponding
 * external API or local execution mechanism.
 * </p>
 *
 * <h2>Included providers</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.provider.impl.OpenAIProvider} adapts the
 * OpenAI Java SDK Responses API and embedding API. It supports conversational
 * prompting, function tools, MCP tools, web search, usage tracking, and
 * embedding generation for OpenAI-compatible endpoints.</li>
 * <li>{@link org.machanism.machai.ai.provider.impl.AnthropicProvider} adapts
 * the Anthropic Java SDK Beta Messages API. It supports message construction,
 * local function tools, optional web search (versions {@code 20250305} and
 * {@code 20260209}), MCP server forwarding, prompt-cache control for the last
 * registered tool, and usage tracking.</li>
 * <li>{@link org.machanism.machai.ai.provider.impl.CodeMieProvider} integrates
 * with EPAM CodeMie authentication, obtains an OAuth 2.0 access token via
 * password-grant or client-credentials flow, and delegates requests to an
 * appropriate downstream provider based on the configured model prefix:
 * {@code gpt-*}, {@code gemini-*}, {@code text-embedding-*},
 * {@code codemie-text-embedding-*}, and {@code amazon.titan-embed-text-*}
 * prefixes are routed to {@link org.machanism.machai.ai.provider.impl.OpenAIProvider};
 * {@code claude-*} prefixes are routed to
 * {@link org.machanism.machai.ai.provider.impl.AnthropicProvider}.</li>
 * <li>{@link org.machanism.machai.ai.provider.impl.ToolsProvider} executes
 * locally registered function tools directly from structured YAML prompts,
 * which is useful for tool-only workflows and deterministic host-side
 * execution. It operates in a fail-fast mode: exceptions from tool execution
 * are propagated immediately rather than being returned as model text.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>
 * Applications normally create a provider through the higher-level Machai
 * provider factory or adapter APIs, initialize it with a model name and a
 * {@link org.machanism.macha.core.commons.configurator.Configurator}, add any
 * required prompts or tools, and call {@code perform()} to execute the request.
 * Providers may be reused after calling {@code clear()} to reset accumulated
 * conversation input.
 * </p>
 *
 * <pre>
 * Genai provider = new OpenAIProvider();
 * provider.init("gpt-4.1", configurator);
 * provider.prompt("Summarize the project architecture.");
 * String answer = provider.perform();
 * provider.clear();
 * </pre>
 *
 * <p>
 * The {@link org.machanism.machai.ai.provider.impl.ToolsProvider} uses the
 * special model name {@code "yaml"} and interprets the last submitted prompt as
 * a YAML tool-call descriptor:
 * </p>
 *
 * <pre>
 * ToolsProvider provider = new ToolsProvider();
 * provider.init("yaml", configurator);
 * provider.addTool("myTool", "Description", myToolFunction);
 * provider.prompt("tool: myTool\nparams:\n  key: value");
 * String result = provider.perform();
 * </pre>
 *
 * <p>
 * Configuration keys such as API credentials, base URLs, timeouts, maximum
 * output tokens, tool-call limits, MCP endpoints, and web-search options are
 * interpreted by the individual provider implementations. See each provider's
 * class-level documentation for the supported keys and backend-specific
 * behavior.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.ai.provider.impl;
