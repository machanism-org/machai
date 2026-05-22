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
 * Provider resolution and token-usage management for generative AI integrations.
 *
 * <p>This package contains the components responsible for locating and instantiating
 * {@link org.machanism.machai.ai.provider.Genai} implementations based on configured
 * chat-model identifiers, as well as immutable usage records and a shared registry for
 * collecting and logging token-consumption statistics.
 *
 * <h2>Package responsibilities</h2>
 * <ul>
 *   <li>Resolve provider implementations from identifiers such as {@code OpenAI:gpt-4o-mini}.</li>
 *   <li>Apply the selected model name to a
 *   {@link org.machanism.macha.core.commons.configurator.Configurator} before provider initialization.</li>
 *   <li>Represent token usage for individual provider invocations.</li>
 *   <li>Aggregate token-usage entries by model identifier for logging and inspection.</li>
 * </ul>
 *
 * <h2>Primary types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.GenaiProviderManager} resolves and initializes provider instances.</li>
 *   <li>{@link org.machanism.machai.ai.manager.Usage} stores token counts for a single provider interaction.</li>
 *   <li>{@link org.machanism.machai.ai.manager.UsageStatistics} records and summarizes usage entries per model.</li>
 * </ul>
 *
 * <h2>Provider resolution</h2>
 * <p>Provider identifiers are expected in the form {@code Provider:Model}. When the provider part is a
 * simple provider name, the implementation class is derived using the convention
 * {@code org.machanism.machai.ai.provider.&lt;provider-lowercase&gt;.&lt;Provider&gt;Provider}. When the
 * provider part already contains a package separator, it is treated as a fully qualified class name.
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * Configurator configurator = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", configurator);
 *
 * Usage usage = provider.usage();
 * UsageStatistics.addUsage("OpenAI:gpt-4o-mini", usage);
 * UsageStatistics.logUsageForModel("OpenAI:gpt-4o-mini");
 * }</pre>
 */
package org.machanism.machai.ai.manager;
