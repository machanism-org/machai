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
 * Provider-neutral generative AI integration for Machai.
 *
 * <p>This package is the root namespace for Machai's large language model
 * support. It provides the high-level contracts and orchestration components
 * used by application code to resolve AI providers, configure requests, prepare
 * prompts and instructions, attach optional files or callable tools, request
 * embeddings, execute model calls, and collect usage information. Provider-
 * specific implementation details are isolated in child packages so callers can
 * work through a stable API regardless of the selected model vendor.</p>
 *
 * <h2>Scope</h2>
 * <p>The package covers the provider lifecycle from model identifier resolution
 * through request execution and usage accounting. It is intended for code that
 * needs to issue text generation or embedding requests without depending
 * directly on OpenAI, Claude, CodeMie routing, or any other concrete provider
 * implementation.</p>
 *
 * <h2>Main package areas</h2>
 * <ul>
 *   <li><strong>Provider contracts</strong> in
 *       {@link org.machanism.machai.ai.provider}, centered on
 *       {@link org.machanism.machai.ai.provider.Genai}. These types define the
 *       common operations for prompt submission, instructions, file attachment,
 *       embeddings, structured interactions, usage reporting, and request
 *       logging.</li>
 *   <li><strong>Provider resolution and lifecycle management</strong> in
 *       {@link org.machanism.machai.ai.manager}. The manager layer maps model
 *       identifiers to provider implementations, initializes them from runtime
 *       configuration, and aggregates usage metrics for observability and cost
 *       tracking.</li>
 *   <li><strong>Tool integration</strong> in
 *       {@link org.machanism.machai.ai.tools}. Tool support discovers host
 *       application capabilities, describes them for model providers, and
 *       exposes them in a controlled form to providers that support tool or
 *       function calling.</li>
 *   <li><strong>Concrete provider implementations</strong> in child packages
 *       below {@link org.machanism.machai.ai.provider}. These packages contain
 *       vendor-specific adapters and shared provider utilities while preserving
 *       the common API exposed from this namespace.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <ol>
 *   <li>Resolve a provider for a configured model identifier.</li>
 *   <li>Initialize the provider with application configuration.</li>
 *   <li>Set system instructions and the user prompt.</li>
 *   <li>Optionally attach files, request embeddings, or register callable
 *       tools.</li>
 *   <li>Execute the request and process the generated response.</li>
 *   <li>Record provider usage for logging, monitoring, or cost analysis.</li>
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
 * <p>Use this package when application code needs a vendor-neutral AI facade
 * while keeping provider-specific behavior, credentials, request formatting,
 * and response handling encapsulated in child packages.</p>
 */
package org.machanism.machai.ai;
