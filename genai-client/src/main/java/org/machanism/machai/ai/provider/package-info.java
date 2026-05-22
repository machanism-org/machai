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
 *        Ensure that the code is properly escaped and formatted.
 */

/**
 * Defines the provider abstraction layer used by MachAI to integrate with concrete
 * generative AI services.
 *
 * <p>This package contains the core {@link org.machanism.machai.ai.provider.Genai}
 * contract, reusable base implementations, and adapter types that let the rest of
 * the application interact with multiple AI backends through a consistent API.
 * Providers encapsulate configuration-driven initialization, prompt and instruction
 * collection, optional tool registration, input logging, embedding generation,
 * response execution, usage reporting, timeout handling, and provider-specific
 * client setup.</p>
 *
 * <h2>Core abstractions</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.provider.Genai} defines the common provider
 * lifecycle and operations for conversational prompting, embedding generation,
 * tool registration, request execution, usage inspection, input logging, working
 * directory propagation, and session cleanup.</li>
 * <li>{@link org.machanism.machai.ai.provider.AbstractAIProvider} supplies shared
 * behavior for configurable providers, including configuration handling,
 * instructions management, request metadata, timeout configuration, input
 * logging, web-search and MCP tool setup, and safe invocation of registered
 * tools.</li>
 * <li>{@link org.machanism.machai.ai.provider.GenaiAdapter} provides a reusable
 * delegating implementation that forwards calls to another {@code Genai}
 * instance, making it suitable for adapter and decorator patterns.</li>
 * </ul>
 *
 * <h2>Provider specializations</h2>
 * <ul>
 * <li>{@code openai}: OpenAI Responses API integration for conversational
 * requests, embeddings, function tools, optional built-in tools such as web
 * search and MCP access, and token-usage tracking.</li>
 * <li>{@code codemie}: EPAM CodeMie integration that authenticates against the
 * CodeMie identity platform and delegates requests to compatible downstream
 * provider implementations according to the configured model family.</li>
 * <li>{@code claude}: Anthropic Claude integration for Claude-backed request
 * execution within the shared provider abstraction.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>Application code typically obtains a concrete implementation from provider
 * management infrastructure, initializes it with configuration, adds optional
 * instructions and prompts, registers any required tools, and then invokes the
 * provider to produce a response, embeddings, and usage information.</p>
 */
package org.machanism.machai.ai.provider;
