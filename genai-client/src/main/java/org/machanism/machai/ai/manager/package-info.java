/*-
 * @guidance:
 *
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package's overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 *      - Do not use escaping in `{@code ...}` tags.    
 */

/**
 * Provides management utilities and data structures for working with generative
 * AI providers and their token-usage metrics.
 *
 * <p>
 * This package contains three cooperating components:
 * </p>
 * <ol>
 * <li>{@link org.machanism.machai.ai.manager.GenaiProviderManager} &ndash; a
 * static utility that resolves and instantiates {@link org.machanism.machai.ai.provider.Genai}
 * chat providers and {@link org.machanism.machai.ai.provider.EmbeddingProvider}
 * embedding providers from a provider/model string such as
 * {@code OpenAI:gpt-4o} or {@code OpenAI:text-embedding-3-small}. Provider
 * classes are located either by a conventional package pattern
 * ({@code org.machanism.machai.ai.provider.impl.{Provider}Provider}) or by a
 * fully qualified class name when the provider segment contains a dot.</li>
 * <li>{@link org.machanism.machai.ai.manager.Usage} &ndash; an immutable
 * value object that captures the input, cached-input, and output token counts
 * returned by a provider for a single request-response cycle.</li>
 * <li>{@link org.machanism.machai.ai.manager.UsageStatistics} &ndash; a
 * thread-safe static registry that accumulates {@code Usage} records grouped
 * by model identifier and exposes methods to query and log aggregated token
 * totals.</li>
 * </ol>
 *
 * <h2>Provider Resolution</h2>
 * <p>
 * Provider and model are specified together in a single string using the
 * {@code Provider:Model} format. The resolution strategy applied by
 * {@code GenaiProviderManager} is:
 * </p>
 * <ol>
 * <li>If the provider segment contains a dot, it is used as-is as a fully
 * qualified class name.</li>
 * <li>Otherwise the conventional name
 * {@code org.machanism.machai.ai.provider.impl.{Provider}Provider} is tried
 * first.</li>
 * <li>If that class is not loadable, a nested-class fallback is attempted
 * inside {@code GenaiProviderManager} itself.</li>
 * </ol>
 *
 * <h2>Token-Usage Tracking</h2>
 * <p>
 * After each AI call, callers can create a {@code Usage} record and register
 * it with {@code UsageStatistics}. Aggregated totals can then be logged at any
 * time for operational visibility.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * // Initialize statistics module at startup
 * UsageStatistics.init();
 *
 * // Resolve a chat provider
 * Configurator conf = ...;
 * Genai chat = GenaiProviderManager.getProvider("OpenAI:gpt-4o", conf);
 *
 * // Resolve an embedding provider
 * EmbeddingProvider embedder =
 *         GenaiProviderManager.getEmbeddingProvider("OpenAI:text-embedding-3-small", conf);
 *
 * // Record token usage after a call
 * UsageStatistics.addUsage("OpenAI:gpt-4o", new Usage(500, 100, 200));
 *
 * // Log a summary for all registered models
 * UsageStatistics.logUsage();
 * </pre>
 *
 * <p>
 * Typical responsibilities covered by this package include:
 * </p>
 * <ul>
 * <li>Resolving provider implementation classes from provider/model strings.</li>
 * <li>Initializing chat and embedding provider instances with application
 * configuration.</li>
 * <li>Capturing immutable token-usage values for individual AI interactions.</li>
 * <li>Aggregating and logging usage totals grouped by model identifier.</li>
 * </ul>
 */
package org.machanism.machai.ai.manager;
