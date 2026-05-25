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
 * Defines the provider abstraction layer used by MachAI to integrate with
 * concrete generative AI platforms through a consistent application-facing API.
 *
 * <p>This package contains the core contracts and shared infrastructure for AI
 * providers, including request initialization, prompt and instruction handling,
 * tool registration, embedding generation, usage tracking, input logging, and
 * working-directory propagation. It establishes the common behavior that allows
 * higher-level application code to interact with different model vendors without
 * depending on provider-specific SDK details.</p>
 *
 * <h2>Core contracts and support types</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.provider.Genai} defines the primary
 * lifecycle and execution contract for conversational and tool-enabled AI
 * providers.</li>
 * <li>{@link org.machanism.machai.ai.provider.EmbeddingProvider} defines the
 * contract for providers that can generate embedding vectors for semantic and
 * similarity-based workflows.</li>
 * <li>{@link org.machanism.machai.ai.provider.AbstractAIProvider} supplies
 * reusable base behavior for configuration-driven providers, including timeout
 * handling, request logging, optional web-search support, MCP server
 * registration, and guarded tool invocation.</li>
 * <li>{@link org.machanism.machai.ai.provider.GenaiAdapter} provides a
 * delegating implementation that enables wrapper, adapter, and decorator
 * patterns around a concrete {@code Genai} instance.</li>
 * </ul>
 *
 * <h2>Included provider families</h2>
 * <ul>
 * <li>{@code openai} contains the OpenAI-backed implementation responsible for
 * text generation, iterative function-tool execution, optional web-search and
 * MCP integration, and embedding requests through OpenAI-compatible APIs.</li>
 * <li>{@code codemie} contains the CodeMie integration that acquires access
 * tokens, resolves supported hosted model families, and delegates requests to
 * the appropriate provider implementation.</li>
 * <li>{@code claude} contains the Anthropic Claude integration that applies the
 * shared provider model to Claude-backed requests.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>Application code typically resolves a concrete provider, initializes it
 * with runtime configuration and a model identifier, optionally adds
 * instructions, prompts, tools, and file context, and then invokes the common
 * API to perform generation or embedding operations while reading usage data
 * from the resulting provider instance.</p>
 */
package org.machanism.machai.ai.provider;
