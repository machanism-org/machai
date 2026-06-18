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
 * Provides Machai's generative AI integration layer, including provider abstractions,
 * concrete provider implementations, usage tracking, and Java tool metadata.
 *
 * <p>This package is the root namespace for components that connect application code
 * to large language model services through a consistent API. It brings together
 * provider resolution, runtime configuration, prompt execution, embedding support,
 * token-usage accounting, and function-tool registration so callers can work with
 * multiple model vendors without depending directly on vendor-specific SDKs.</p>
 *
 * <h2>Package areas</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager} resolves configured provider identifiers,
 *   initializes provider instances, and records token usage by model.</li>
 *   <li>{@link org.machanism.machai.ai.provider} defines the common provider contracts and
 *   shared base behavior used by conversational, embedding, tool-enabled, web-search-enabled,
 *   and MCP-enabled AI integrations.</li>
 *   <li>{@link org.machanism.machai.ai.provider.openai} contains the OpenAI-backed provider
 *   for response generation, iterative function-tool calls, web search, MCP server usage,
 *   file inputs, embeddings, and OpenAI usage conversion.</li>
 *   <li>{@link org.machanism.machai.ai.provider.anthropic} contains the Anthropic Claude
 *   provider implementation, including message construction, tool-use loops, optional web
 *   search, MCP server forwarding, and usage capture.</li>
 *   <li>{@link org.machanism.machai.ai.provider.codemie} integrates with EPAM CodeMie by
 *   acquiring OAuth 2.0 access tokens and delegating supported model families to OpenAI-
 *   compatible or Anthropic-compatible provider implementations.</li>
 *   <li>{@link org.machanism.machai.ai.provider.tools} exposes registered application tools
 *   through the provider lifecycle and supports structured YAML-based tool invocation.</li>
 *   <li>{@link org.machanism.machai.ai.tools} defines annotations, descriptors, loader
 *   utilities, and callback contracts used to discover Java methods as AI-callable tools
 *   and reusable prompts.</li>
 * </ul>
 *
 * <h2>Typical flow</h2>
 * <p>Applications usually provide a model identifier and runtime configuration to the
 * manager layer, receive a {@link org.machanism.machai.ai.provider.Genai} implementation,
 * attach prompts, instructions, files, and tools as needed, and then invoke the provider
 * to produce a response or embeddings. Providers report token usage through
 * {@link org.machanism.machai.ai.manager.Usage}, which can be aggregated and logged with
 * {@link org.machanism.machai.ai.manager.UsageStatistics}.</p>
 *
 * <pre>{@code
 * Configurator configurator = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", configurator);
 * provider.instructions("You are a concise assistant.");
 * provider.prompt("Summarize the current project.");
 * String answer = provider.perform();
 * UsageStatistics.addUsage("OpenAI:gpt-4o-mini", provider.usage());
 * }</pre>
 */
package org.machanism.machai.ai;
