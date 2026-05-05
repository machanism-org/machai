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
 * Provider-agnostic generative AI client API for MachAI.
 *
 * <p>This root package defines the high-level integration layer used to interact with large language models and
 * related AI services without coupling application code to a specific vendor SDK, endpoint, authentication flow, or
 * execution mode. It organizes the contracts, provider implementations, provider-resolution infrastructure, tool
 * registration support, and usage accounting needed by higher-level MachAI workflows.</p>
 *
 * <h2>Package scope</h2>
 * <ul>
 *   <li><strong>Provider management</strong> in {@link org.machanism.machai.ai.manager} resolves model identifiers,
 *       initializes concrete providers with runtime configuration, and aggregates token usage reported by completed
 *       AI interactions.</li>
 *   <li><strong>Provider contracts</strong> in {@link org.machanism.machai.ai.provider} define the common
 *       {@link org.machanism.machai.ai.provider.Genai} API used for initialization, instructions, prompts, execution,
 *       embeddings, input logging, tool registration, and usage reporting.</li>
 *   <li><strong>Provider implementations</strong> under {@code org.machanism.machai.ai.provider.*} integrate concrete
 *       backends such as OpenAI-compatible services, EPAM CodeMie, and the no-operation provider used for disabled or
 *       offline execution paths.</li>
 *   <li><strong>Host-side tools</strong> in {@link org.machanism.machai.ai.tools} discover and register controlled local
 *       callbacks through Java's {@link java.util.ServiceLoader} mechanism so compatible providers can expose
 *       application capabilities to model tool or function calls.</li>
 * </ul>
 *
 * <h2>Typical usage flow</h2>
 * <ol>
 *   <li>Resolve a provider using a provider/model identifier such as {@code OpenAI:gpt-4o-mini}, a custom provider
 *       class name, or a model name that falls back to the {@code none} provider.</li>
 *   <li>Initialize the provider through the manager with an application
 *       {@link org.machanism.macha.core.commons.configurator.Configurator} so model and backend-specific settings are
 *       available at runtime.</li>
 *   <li>Configure system instructions, user prompts, files, input logs, and optional provider-specific settings.</li>
 *   <li>Optionally apply tool providers through {@link org.machanism.machai.ai.tools.FunctionToolsLoader} before
 *       executing requests that support tool calling.</li>
 *   <li>Execute the request, consume the response or embeddings, and aggregate the returned
 *       {@link org.machanism.machai.ai.manager.Usage} metrics when reporting token consumption.</li>
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
 * <p>The package is intended for application code that coordinates prompt construction and response handling while
 * delegating provider selection, backend-specific request execution, controlled tool exposure, and usage monitoring to
 * the corresponding sub-packages.</p>
 */
package org.machanism.machai.ai;
