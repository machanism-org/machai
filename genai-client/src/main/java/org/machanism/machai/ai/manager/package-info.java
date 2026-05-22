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
 * Provider resolution and token-usage aggregation for generative AI integrations.
 *
 * <p>This package contains utility and value types used to instantiate
 * {@link org.machanism.machai.ai.provider.Genai} implementations from configured chat-model identifiers
 * and to collect, aggregate, and log token-consumption information produced by those providers.
 * The package is intended to support the runtime selection of AI backends as well as operational
 * visibility into prompt and response token usage.
 *
 * <h2>Core responsibilities</h2>
 * <ul>
 *   <li>Resolve provider implementations from model identifiers such as {@code OpenAI:gpt-4o-mini}.</li>
 *   <li>Apply the selected model name to a
 *   {@link org.machanism.macha.core.commons.configurator.Configurator} before provider initialization.</li>
 *   <li>Represent usage metrics for individual provider invocations with immutable value objects.</li>
 *   <li>Aggregate and log usage statistics for one or more configured models.</li>
 * </ul>
 *
 * <h2>Included types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.GenaiProviderManager} resolves and initializes provider
 *   instances for configured chat models.</li>
 *   <li>{@link org.machanism.machai.ai.manager.Usage} stores input, cached-input, and output token counts
 *   for a single provider call.</li>
 *   <li>{@link org.machanism.machai.ai.manager.UsageStatistics} collects usage records by model identifier
 *   and produces aggregated summaries for logging and inspection.</li>
 * </ul>
 *
 * <h2>Provider identifier resolution</h2>
 * <p>Provider identifiers are typically supplied in the form {@code Provider:Model}. When the provider
 * segment is a simple provider name, the corresponding implementation class is inferred using the naming
 * convention {@code org.machanism.machai.ai.provider.<provider-lowercase>.<Provider>Provider}. If the
 * provider segment already contains a package separator, it is treated as a fully qualified class name
 * and loaded directly.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Configurator configurator = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", configurator);
 * String response = provider.perform();
 *
 * UsageStatistics.addUsage("OpenAI:gpt-4o-mini", provider.usage());
 * UsageStatistics.logUsageForModel("OpenAI:gpt-4o-mini");
 * }</pre>
 */
package org.machanism.machai.ai.manager;
