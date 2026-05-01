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
 * contract and shared support types that let the rest of the application interact
 * with different AI backends through a uniform API. Providers encapsulate model
 * initialization, prompt and instruction collection, optional tool registration,
 * embedding generation, request execution, usage reporting, and provider-specific
 * configuration concerns.</p>
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
 * <h2>Provider implementations</h2>
 * <ul>
 * <li>{@code openai}: OpenAI Responses API integration with support for prompts,
 * tool calling, input logging, embeddings, and usage tracking.</li>
 * <li>{@code codemie}: EPAM CodeMie integration that acquires OAuth 2.0 tokens and
 * delegates requests to OpenAI-, Gemini-, or Claude-compatible provider
 * implementations based on the configured model family.</li>
 * <li>{@code gemini}: Gemini-focused provider package for Gemini-backed request
 * execution and related provider behavior.</li>
 * <li>{@code none}: No-op provider used when AI execution is disabled, offline, or
 * limited to prompt logging while preserving application flow.</li>
 * <li>{@code claude}: Claude-focused provider package for Anthropic-backed request
 * execution and related provider behavior.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>Application code typically obtains a concrete implementation from provider
 * management infrastructure, initializes it with configuration, assembles
 * instructions and prompts, optionally configures tools or input logging, and then
 * invokes the provider to obtain a response, embeddings, and usage information.</p>
 */
package org.machanism.machai.ai.provider;
