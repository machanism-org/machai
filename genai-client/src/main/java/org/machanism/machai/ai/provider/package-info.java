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
 * Defines the provider abstraction layer that connects MachAI to concrete
 * generative AI platforms through a shared, provider-neutral API.
 *
 * <p>This package contains the central {@link org.machanism.machai.ai.provider.Genai}
 * contract together with reusable base and adapter implementations used to build
 * provider integrations. It standardizes how the application initializes AI
 * clients, supplies instructions and prompts, registers callable tools, generates
 * embeddings, executes model requests, tracks usage, propagates working-directory
 * context, and releases provider resources.</p>
 *
 * <h2>Core abstractions</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.provider.Genai} defines the common lifecycle
 * and operations for prompt execution, embedding generation, tool registration,
 * usage inspection, input logging, and cleanup.</li>
 * <li>{@link org.machanism.machai.ai.provider.AbstractAIProvider} supplies shared
 * infrastructure for configuration-backed providers, including instruction and
 * prompt collection, timeout handling, request metadata, logging, optional web
 * search and MCP tool support, and guarded invocation of registered tools.</li>
 * <li>{@link org.machanism.machai.ai.provider.GenaiAdapter} provides a delegating
 * implementation that forwards operations to another {@code Genai} instance,
 * making it suitable for adapter, wrapper, and decorator scenarios.</li>
 * </ul>
 *
 * <h2>Provider implementations</h2>
 * <ul>
 * <li>{@code openai} contains the OpenAI-backed implementation for response
 * generation, function-tool execution, embeddings, and usage reporting through
 * OpenAI-compatible APIs.</li>
 * <li>{@code codemie} contains the CodeMie integration that acquires platform
 * access tokens and configures delegated providers for CodeMie-hosted model
 * endpoints.</li>
 * <li>{@code claude} contains the Anthropic Claude integration used to execute
 * Claude-backed requests within the same shared provider model.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>Provider management code typically creates or resolves a concrete
 * implementation, initializes it from runtime configuration, optionally adds
 * instructions, prompts, files, and tools, and then invokes the provider to
 * obtain generated content, embeddings, and usage statistics through the common
 * API exposed by this package.</p>
 */
package org.machanism.machai.ai.provider;
