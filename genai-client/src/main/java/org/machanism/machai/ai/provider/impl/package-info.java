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
 * Concrete provider implementations for Machai's generative AI abstraction.
 *
 * <p>
 * This package contains adapters that connect the common Machai provider API to
 * external and local execution backends. The implementations translate prompts,
 * system instructions, tool definitions, MCP server configuration, web search
 * settings, and embedding requests into the protocol expected by each target
 * service.
 * </p>
 *
 * <h2>Providers</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.provider.impl.OpenAIProvider} integrates
 * with OpenAI and OpenAI-compatible Responses and Embeddings APIs.</li>
 * <li>{@link org.machanism.machai.ai.provider.impl.AnthropicProvider}
 * integrates with the Anthropic Messages API and supports Anthropic-specific
 * tool use, prompt caching, web search, and MCP server forwarding.</li>
 * <li>{@link org.machanism.machai.ai.provider.impl.CodeMieProvider}
 * authenticates with CodeMie, obtains OAuth access tokens, and delegates to the
 * compatible OpenAI or Anthropic provider implementation based on the selected
 * model family.</li>
 * <li>{@link org.machanism.machai.ai.provider.impl.ToolsProvider} executes
 * registered host-side tool functions directly from structured YAML input,
 * primarily for tool-only or local orchestration workflows.</li>
 * </ul>
 *
 * <p>
 * Provider instances are initialized with a model name and configuration, accept
 * prompt text through the common API, optionally register callable tools, and
 * return generated text or tool execution output from their execution methods.
 * Implementations also record usage statistics when the underlying service
 * exposes token accounting.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
package org.machanism.machai.ai.provider.impl;
