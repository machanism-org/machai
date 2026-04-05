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
 * contract and shared support types that allow the rest of the application to work
 * with provider implementations through a uniform API. Providers are responsible for
 * collecting prompts and instructions, optionally registering callable tools,
 * generating embeddings, executing model requests, reporting usage, and handling
 * provider-specific configuration.</p>
 *
 * <h2>Core types</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.provider.Genai} defines the common lifecycle and
 * operations for AI-backed interactions, including initialization, prompting,
 * execution, tool registration, embedding generation, usage inspection, and session
 * cleanup.</li>
 * <li>{@link org.machanism.machai.ai.provider.GenaiAdapter} provides a reusable
 * delegating implementation that forwards all operations to another
 * {@code Genai} instance, making it suitable for adapter and decorator patterns.</li>
 * </ul>
 *
 * <h2>Available provider packages</h2>
 * <ul>
 * <li>{@code openai}: OpenAI Responses API integration with support for prompts,
 * tool calling, input logging, embeddings, and usage tracking.</li>
 * <li>{@code codemie}: EPAM CodeMie integration that acquires OAuth 2.0 tokens and
 * delegates requests to OpenAI-, Gemini-, or Claude-compatible providers based on
 * the configured model family.</li>
 * <li>{@code gemini}: Gemini provider integration scaffold that documents the intended
 * Gemini-backed lifecycle and request model.</li>
 * <li>{@code none}: No-op provider used when AI execution is disabled, offline, or
 * limited to prompt logging and flow preservation.</li>
 * <li>{@code claude}: Claude provider placeholder for future Anthropic-backed
 * integration.</li>
 * </ul>
 *
 * <h2>Usage model</h2>
 * <p>Application code typically obtains a concrete implementation from provider
 * management infrastructure, initializes it with configuration, assembles
 * instructions and prompts, optionally configures tools or input logging, and then
 * invokes the provider to obtain a response.</p>
 */
package org.machanism.machai.ai.provider;
