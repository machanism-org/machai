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
 * Provider-agnostic generative AI integration for MachAI.
 *
 * <p>This package defines the root API used to work with large language model providers, provider management,
 * host-exposed tools, and usage tracking without coupling application code to a specific backend implementation.
 * It serves as the entry point for configuring providers, preparing prompts and instructions, enabling tool calling,
 * executing model requests, and collecting usage information across supported integrations.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li><strong>Provider resolution and lifecycle</strong> via {@link org.machanism.machai.ai.manager}, including
 *       lookup by provider/model identifier, initialization with runtime configuration, and aggregation of usage
 *       metrics.</li>
 *   <li><strong>Common provider contracts</strong> in {@link org.machanism.machai.ai.provider}, centered on the
 *       {@link org.machanism.machai.ai.provider.Genai} interface for prompts, instructions, execution, embeddings,
 *       file inputs, tool integration, and token accounting.</li>
 *   <li><strong>Concrete provider implementations</strong> under {@code org.machanism.machai.ai.provider.*} for
 *       specific backends such as OpenAI-compatible services, EPAM CodeMie, and fallback no-operation behavior.</li>
 *   <li><strong>Function tool discovery and registration</strong> in {@link org.machanism.machai.ai.tools}, enabling
 *       providers that support tool or function calling to expose controlled application capabilities through
 *       {@link java.util.ServiceLoader}.</li>
 * </ul>
 *
 * <h2>Typical workflow</h2>
 * <ol>
 *   <li>Select or resolve a provider for a model identifier such as {@code OpenAI:gpt-4o-mini}.</li>
 *   <li>Initialize the provider using application configuration supplied through
 *       {@link org.machanism.macha.core.commons.configurator.Configurator}.</li>
 *   <li>Set instructions, prompts, optional files, and provider-specific options.</li>
 *   <li>Apply discovered tools with {@link org.machanism.machai.ai.tools.FunctionToolsLoader} when tool calling is
 *       needed.</li>
 *   <li>Execute the request and record returned usage with
 *       {@link org.machanism.machai.ai.manager.GenaiProviderManager}.</li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>
 * Configurator conf = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.instructions("You are a concise assistant.");
 * provider.prompt("Summarize this repository.");
 *
 * FunctionToolsLoader loader = FunctionToolsLoader.getInstance();
 * loader.setConfiguration(conf);
 * loader.applyTools(provider);
 *
 * String answer = provider.perform();
 * GenaiProviderManager.addUsage(provider.usage());
 * GenaiProviderManager.logUsage();
 * </pre>
 *
 * <p>Application code should depend on this package when it needs a stable, vendor-neutral entry point for AI
 * interactions while delegating provider-specific behavior and runtime integration details to the corresponding
 * sub-packages.</p>
 */
package org.machanism.machai.ai;
