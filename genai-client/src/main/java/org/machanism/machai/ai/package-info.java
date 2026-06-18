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
 * Provider-agnostic generative AI integration for Machai.
 *
 * <p>This package defines the root API and orchestration layer for working with
 * large language model providers, provider resolution, request preparation,
 * tool exposure, file-aware interactions, embeddings, and usage accounting. It
 * supplies the stable entry point used by application code while delegating
 * provider-specific behavior and specialized integration details to child
 * packages.</p>
 *
 * <h2>Scope</h2>
 * <p>The package groups the top-level abstractions needed to resolve a provider
 * from a model identifier, initialize it with runtime configuration, build
 * prompts and instructions, attach optional files or callable tools, execute
 * requests, and collect provider-reported usage metrics for monitoring and
 * logging.</p>
 *
 * <h2>Main package areas</h2>
 * <ul>
 *   <li><strong>Provider contracts</strong> in
 *       {@link org.machanism.machai.ai.provider}, centered on
 *       {@link org.machanism.machai.ai.provider.Genai} and related types for
 *       text generation, embeddings, structured interactions, request logging,
 *       and provider adaptation.</li>
 *   <li><strong>Provider resolution and lifecycle management</strong> in
 *       {@link org.machanism.machai.ai.manager}, which maps model identifiers
 *       to implementations, initializes providers from configuration, and
 *       aggregates usage statistics.</li>
 *   <li><strong>Tool tool integration</strong> in
 *       {@link org.machanism.machai.ai.tools}, where host application
 *       capabilities are discovered, described, and exposed to compatible
 *       providers for controlled tool calling.</li>
 *   <li><strong>Concrete provider implementations</strong> in child packages
 *       below {@link org.machanism.machai.ai.provider}, including
 *       OpenAI-backed, Claude-backed, CodeMie-routed, and shared provider
 *       abstractions.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <ol>
 *   <li>Resolve a provider for a configured model identifier.</li>
 *   <li>Initialize the provider with application configuration.</li>
 *   <li>Set system instructions and the user prompt.</li>
 *   <li>Optionally attach files, embeddings, or callable tools.</li>
 *   <li>Execute the request and process the generated response.</li>
 *   <li>Record usage information for observability and cost tracking.</li>
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
 * <p>Use this package when code needs a vendor-neutral API for generative AI
 * features while keeping provider-specific behavior isolated in child
 * packages.</p>
 */
package org.machanism.machai.ai;
