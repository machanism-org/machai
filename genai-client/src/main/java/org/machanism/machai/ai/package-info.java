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
 * Provides Machai's AI integration layer, including provider abstractions,
 * provider implementations, provider lifecycle management, usage accounting,
 * and function-tool metadata used during model-assisted workflows.
 *
 * <p>The package groups the application-facing components that allow Machai to
 * communicate with generative AI services without binding callers directly to a
 * specific vendor SDK. Higher-level code can resolve and configure providers,
 * submit prompts or instructions, register callable tools and resources, request
 * embeddings, and inspect usage statistics through stable contracts.</p>
 *
 * <h2>Package responsibilities</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.provider} defines the common provider API
 * for text generation, embeddings, tool-aware execution, request logging,
 * configuration, and provider delegation. It includes the core
 * {@link org.machanism.machai.ai.provider.Genai} and
 * {@link org.machanism.machai.ai.provider.EmbeddingProvider} contracts, the
 * {@link org.machanism.machai.ai.provider.AbstractAIProvider} base class with
 * shared infrastructure, the {@link org.machanism.machai.ai.provider.GenaiAdapter}
 * decorator, and the {@link org.machanism.machai.ai.provider.TypeConverter}
 * utility for schema-compatible type mapping.</li>
 * <li>{@link org.machanism.machai.ai.provider.impl} contains concrete provider
 * integrations for supported model backends, including OpenAI-compatible,
 * Anthropic Claude, CodeMie (with OAuth 2.0 delegation), and tool-focused
 * provider workflows via {@link org.machanism.machai.ai.provider.impl.ToolsProvider}.</li>
 * <li>{@link org.machanism.machai.ai.manager} coordinates provider and embedding
 * provider resolution from {@code Provider:Model} strings, instantiation via
 * Java reflection, and token or request usage tracking across generation
 * operations through {@link org.machanism.machai.ai.manager.GenaiProviderManager},
 * {@link org.machanism.machai.ai.manager.Usage}, and
 * {@link org.machanism.machai.ai.manager.UsageStatistics}.</li>
 * <li>{@link org.machanism.machai.ai.tools} defines annotations
 * ({@link org.machanism.machai.ai.tools.Tool}, {@link org.machanism.machai.ai.tools.Prompt},
 * {@link org.machanism.machai.ai.tools.Resource}, {@link org.machanism.machai.ai.tools.Param},
 * {@link org.machanism.machai.ai.tools.SupportedFor}), descriptors, roles, and loader
 * utilities used to expose Java methods as model-callable function tools and prompts,
 * as well as the {@link org.machanism.machai.ai.tools.FunctionTools} SPI,
 * {@link org.machanism.machai.ai.tools.FunctionToolsLoader}, and
 * {@link org.machanism.machai.ai.tools.ToolFunction} functional interface.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>Clients generally obtain a configured generative AI provider through the
 * manager layer, enrich it with prompts, instructions, optional file context,
 * and annotated tools, then invoke the provider API to execute a generation or
 * embedding request. The shared abstractions in this package keep those
 * workflows consistent across supported backend providers while preserving
 * access to usage information for accounting, diagnostics, and optimization.</p>
 *
 * <pre>
 * // Resolve a chat provider by provider:model string
 * Configurator conf = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4.1", conf);
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize the project architecture.");
 * String answer = provider.perform();
 *
 * // Record token usage after the call
 * UsageStatistics.addUsage("OpenAI:gpt-4.1", new Usage(500, 100, 200));
 * UsageStatistics.logUsage();
 * provider.clear();
 * </pre>
 */
package org.machanism.machai.ai;
