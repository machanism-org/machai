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
 * Manager API and service-provider interface (SPI) for resolving, configuring, and operating generative-AI provider
 * integrations.
 *
 * <p>This package defines the core abstraction ({@link org.machanism.machai.ai.manager.GenAIProvider}) used to interact
 * with different AI backends through a uniform contract. It also provides a manager for resolving providers from a
 * model identifier and aggregating usage.
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProvider} – provider contract exposing a common interaction surface
 *   (prompts/instructions, file inputs, embeddings, tool registration and execution).</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProviderManager} – resolves and instantiates providers based on a
 *   model identifier and aggregates {@link org.machanism.machai.ai.manager.Usage} metrics.</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIAdapter} – forwards calls to an underlying provider to enable
 *   wrapper implementations (logging, metrics, retries, request shaping).</li>
 *   <li>{@link org.machanism.machai.ai.manager.Usage} – value object capturing token usage for a single provider
 *   invocation.</li>
 * </ul>
 *
 * <h2>Provider resolution</h2>
 * <p>Providers are typically selected using a model identifier formatted as {@code Provider:Model} (for example,
 * {@code OpenAI:gpt-4o-mini}). If the provider prefix is omitted (for example, {@code gpt-4o-mini}), the manager
 * attempts to use a default provider.
 *
 * <p>A provider can be referenced either by a short provider name (mapped to
 * {@code org.machanism.machai.ai.provider.&lt;provider&gt;.&lt;Provider&gt;Provider}) or by a fully-qualified class name.
 *
 * <h2>Usage aggregation</h2>
 * <p>Provider implementations can expose per-invocation token usage via
 * {@link org.machanism.machai.ai.manager.GenAIProvider#usage()}. Applications can aggregate and log usage using
 * {@link org.machanism.machai.ai.manager.GenAIProviderManager#addUsage(org.machanism.machai.ai.manager.Usage)} and
 * {@link org.machanism.machai.ai.manager.GenAIProviderManager#logUsage()}.
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
