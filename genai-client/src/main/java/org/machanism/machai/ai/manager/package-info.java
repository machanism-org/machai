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
 * Provider-agnostic manager API and service-provider interface (SPI) for resolving, configuring, and operating
 * generative-AI integrations.
 *
 * <p>This package defines the {@link org.machanism.machai.ai.manager.Genai} contract and the
 * {@link org.machanism.machai.ai.manager.GenaiProviderManager} factory used to locate and instantiate provider
 * implementations from a model identifier.
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.Genai} – provider contract for prompt/instruction composition,
 *   attachments, embeddings, tool registration and execution, and usage reporting.</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenaiProviderManager} – resolves providers from a model identifier and
 *   aggregates {@link org.machanism.machai.ai.manager.Usage} records for logging.</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenaiAdapter} – delegating base implementation for decorating a
 *   provider (for example, adding telemetry or cross-cutting behavior).</li>
 *   <li>{@link org.machanism.machai.ai.manager.Usage} – per-invocation token usage metrics.</li>
 * </ul>
 *
 * <h2>Provider resolution</h2>
 * <p>Providers are typically selected with a model identifier formatted as {@code Provider:Model} (for example,
 * {@code OpenAI:gpt-4o-mini}). If the provider prefix is omitted (for example, {@code gpt-4o-mini}),
 * {@link org.machanism.machai.ai.manager.GenaiProviderManager} uses the configured default provider.
 *
 * <p>A provider can be referenced either by a short provider name (mapped to
 * {@code org.machanism.machai.ai.provider.<provider>.<Provider>Provider}) or by a fully-qualified class name.
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this project.");
 * String response = provider.perform();
 *
 * GenaiProviderManager.addUsage(provider.usage());
 * GenaiProviderManager.logUsage();
 * }</pre>
 */
package org.machanism.machai.ai.manager;
