/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
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
 * Manager API and service provider interface (SPI) for resolving, configuring, and operating generative-AI provider
 * integrations.
 *
 * <p>This package defines the core abstractions for integrating multiple AI backends behind a single contract.
 * Implementations can be discovered and configured, then selected at runtime by name and model.
 *
 * <h2>Core types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProvider} defines the provider contract (prompting, tools,
 *   embeddings, execution, and lifecycle).</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIAdapter} decorates a provider instance to add cross-cutting
 *   behavior (for example logging, metrics, retries, or request shaping) without modifying the underlying provider.</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProviderManager} resolves a provider from a provider/model
 *   identifier and initializes it using a
 *   {@link org.machanism.macha.core.commons.configurator.Configurator}.</li>
 *   <li>{@link org.machanism.machai.ai.manager.Usage} captures per-invocation token/usage metrics that can be
 *   aggregated and reported by the manager.</li>
 * </ul>
 *
 * <h2>Provider resolution</h2>
 * <p>Providers are resolved from a {@code Provider:Model} identifier (for example, {@code OpenAI:gpt-4o-mini}). If the
 * provider prefix is omitted, a default provider is selected.
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this project.");
 * String response = provider.perform();
 *
 * GenAIProviderManager.addUsage(provider.usage());
 * GenAIProviderManager.logUsage();
 * }</pre>
 */
package org.machanism.machai.ai.manager;
