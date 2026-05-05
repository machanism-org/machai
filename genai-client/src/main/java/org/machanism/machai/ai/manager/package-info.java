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
 * Provider management and token-usage aggregation for generative AI integrations.
 *
 * <p>This package contains the manager-level API used to resolve, initialize, and monitor
 * {@link org.machanism.machai.ai.provider.Genai} implementations. It translates user-facing chat model
 * identifiers into provider classes, applies the selected model to the runtime configuration, and records
 * provider-reported token consumption for summary logging.
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>Resolve a provider implementation from a model identifier in the form {@code Provider:Model}, such as
 *   {@code OpenAI:gpt-4o-mini}.</li>
 *   <li>Support fully qualified provider class names for custom provider implementations.</li>
 *   <li>Fall back to the {@code none} provider when a model name is supplied without an explicit provider
 *   segment.</li>
 *   <li>Initialize each resolved provider with an application {@link org.machanism.macha.core.commons.configurator.Configurator}
 *   after storing the selected {@code chatModel} value.</li>
 *   <li>Aggregate immutable {@link org.machanism.machai.ai.manager.Usage} records and log total input, cached
 *   input, and output token counts.</li>
 * </ul>
 *
 * <h2>Included types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.GenaiProviderManager} provides static factory and aggregation
 *   methods for provider resolution and usage reporting.</li>
 *   <li>{@link org.machanism.machai.ai.manager.Usage} represents token metrics for a single generative AI
 *   invocation.</li>
 * </ul>
 *
 * <h2>Provider identifiers</h2>
 * <p>Short provider names are mapped to provider classes using the convention
 * {@code org.machanism.machai.ai.provider.<provider-lowercase>.<Provider>Provider}. For example,
 * {@code OpenAI:gpt-4o-mini} resolves to {@code org.machanism.machai.ai.provider.openai.OpenAIProvider} and
 * configures that provider with {@code gpt-4o-mini}. If the provider portion contains a dot, it is treated as a
 * fully qualified class name and loaded directly.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * provider.prompt("Summarize this project.");
 * String response = provider.perform();
 *
 * GenaiProviderManager.addUsage(provider.usage());
 * GenaiProviderManager.logUsage();
 * }</pre>
 */
package org.machanism.machai.ai.manager;
