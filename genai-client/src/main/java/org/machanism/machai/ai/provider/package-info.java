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
 *     - Generate comprehensive package-level Javadoc that clearly describes the package's overall purpose, scope, and usage based on package-info.java files located on child folders.
 *     - Place the package-level Javadoc immediately before the `package` declaration.
 *
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` and &gt; in `<pre>` content for Javadoc. 
 *        Ensure that the code is properly escaped and formatted.
 */

/**
 * Defines the provider abstraction layer used by Machai to integrate with
 * concrete generative AI platforms through a consistent application-facing API.
 *
 * <p>This package contains the core contracts, shared infrastructure, and
 * utility types for AI providers, including request initialization, prompt and
 * instruction handling, tool registration, resource registration, embedding
 * generation, usage tracking, input logging, and working-directory propagation.
 * It establishes the common behavior that allows higher-level application code
 * to interact with different model vendors without depending on
 * provider-specific SDK details.</p>
 *
 * <h2>Core contracts</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.provider.Genai} defines the primary
 * lifecycle and execution contract for conversational and tool-enabled AI
 * providers, covering initialization, prompting, instruction setting, tool and
 * resource registration, error handling configuration, and response
 * generation.</li>
 * <li>{@link org.machanism.machai.ai.provider.EmbeddingProvider} defines the
 * contract for providers that can generate embedding vectors for semantic and
 * similarity-based workflows.</li>
 * </ul>
 *
 * <h2>Base and adapter implementations</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.provider.AbstractAIProvider} supplies
 * reusable base behavior for configuration-driven providers, including timeout
 * handling, request input logging, optional web-search support, MCP server
 * registration, annotation-driven tool and prompt discovery, guarded tool
 * invocation with configurable error handling, and reflective method
 * invocation for tool and prompt callbacks.</li>
 * <li>{@link org.machanism.machai.ai.provider.GenaiAdapter} provides a
 * delegating implementation that forwards all {@link org.machanism.machai.ai.provider.Genai}
 * calls to an underlying provider instance, enabling wrapper, adapter, and
 * decorator patterns such as cross-cutting logging, metrics, retries, or
 * request shaping around a concrete {@code Genai} instance.</li>
 * </ul>
 *
 * <h2>Support utilities</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.provider.TypeConverter} provides
 * conversion between Java types and their simplified schema-compatible string
 * representations (e.g., {@code "string"}, {@code "integer"}, {@code "array"},
 * {@code "object"}), and performs runtime conversion of string inputs to typed
 * Java objects—including collections, maps, primitives, and arbitrary types
 * with single-argument string constructors.</li>
 * </ul>
 *
 * <h2>Concrete provider implementations</h2>
 * <p>The {@link org.machanism.machai.ai.provider.impl} sub-package contains
 * concrete provider adapters that connect Machai's common AI interfaces to
 * specific runtime backends:</p>
 * <ul>
 * <li>{@link org.machanism.machai.ai.provider.impl.OpenAIProvider} adapts the
 * OpenAI Responses API and embedding API, supporting conversational prompting,
 * function tools, MCP tools, web search, usage tracking, and embedding
 * generation for OpenAI-compatible endpoints.</li>
 * <li>{@link org.machanism.machai.ai.provider.impl.AnthropicProvider} adapts
 * the Anthropic Claude Beta Messages API, supporting function tools, optional
 * web search, MCP server forwarding, prompt-cache control, and usage
 * tracking.</li>
 * <li>{@link org.machanism.machai.ai.provider.impl.CodeMieProvider} integrates
 * with EPAM CodeMie authentication and delegates to the appropriate downstream
 * provider ({@code OpenAIProvider} or {@code AnthropicProvider}) based on the
 * configured model prefix.</li>
 * <li>{@link org.machanism.machai.ai.provider.impl.ToolsProvider} executes
 * locally registered function tools directly from structured YAML prompts,
 * useful for tool-only workflows and deterministic host-side execution.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>Application code typically resolves a concrete provider, initializes it
 * with runtime configuration and a model identifier, optionally adds
 * instructions, prompts, tools, and file context, and then invokes the common
 * API to perform generation or embedding operations while reading usage data
 * from the resulting provider instance.</p>
 *
 * <pre>
 * Configurator conf = ...;
 * Genai provider = new OpenAIProvider();
 * provider.init("gpt-4.1", conf);
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize the project architecture.");
 * String answer = provider.perform();
 * provider.clear();
 * </pre>
 */
package org.machanism.machai.ai.provider;
