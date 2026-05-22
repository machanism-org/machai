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
 * <p>This package defines the root API for working with large language model providers, provider selection,
 * provider lifecycle management, function tool integration, and usage accounting. It gives application code a
 * stable entry point for AI interactions while isolating provider-specific behavior in dedicated sub-packages.</p>
 *
 * <h2>Scope</h2>
 * <p>The package organizes the abstractions and top-level services needed to configure a provider, prepare
 * prompts and instructions, attach optional files or tools, execute requests, and track consumption across
 * supported integrations.</p>
 *
 * <h2>Main areas</h2>
 * <ul>
 *   <li><strong>Provider contracts</strong> in {@link org.machanism.machai.ai.provider}, centered on
 *       {@link org.machanism.machai.ai.provider.Genai} for text generation, embeddings, file-aware requests,
 *       tool calling, and usage reporting.</li>
 *   <li><strong>Provider management</strong> in {@link org.machanism.machai.ai.manager}, which resolves provider
 *       implementations from model identifiers, initializes them with runtime configuration, and aggregates usage
 *       metrics for monitoring and logging.</li>
 *   <li><strong>Tool exposure</strong> in {@link org.machanism.machai.ai.tools}, where host-side functions are
 *       discovered and applied so compatible providers can invoke controlled application capabilities.</li>
 *   <li><strong>Backend implementations</strong> in concrete {@code provider.*} sub-packages for supported services
 *       such as OpenAI-compatible integrations, EPAM CodeMie, and no-operation fallbacks.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <ol>
 *   <li>Resolve a provider for a configured model identifier.</li>
 *   <li>Initialize the provider with application configuration.</li>
 *   <li>Set instructions, prompts, and optional tool support.</li>
 *   <li>Execute the request and process the generated response.</li>
 *   <li>Collect and log usage information for observability.</li>
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
 * <p>Use this package when application code needs a vendor-neutral API for AI features while delegating concrete
 * integration details to the child packages that implement provider-specific behavior.</p>
 */
package org.machanism.machai.ai;
