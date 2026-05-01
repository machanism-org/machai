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
 * Provider resolution and usage-tracking support for generative AI integrations.
 *
 * <p>This package contains manager-level types that locate {@link org.machanism.machai.ai.provider.Genai}
 * implementations from provider and model identifiers, initialize resolved providers with application
 * configuration, and collect token-consumption metrics for later logging.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Resolve a concrete provider implementation from identifiers such as
 *   {@code OpenAI:gpt-4o-mini} or from a fully qualified provider class name.</li>
 *   <li>Apply runtime configuration, including the selected chat model, before the provider is used.</li>
 *   <li>Aggregate {@link org.machanism.machai.ai.manager.Usage} records produced during provider calls.</li>
 *   <li>Log summarized token usage across one or more generative AI interactions.</li>
 * </ul>
 *
 * <h2>Included types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.GenaiProviderManager} resolves provider implementations,
 *   initializes them, and aggregates usage statistics.</li>
 *   <li>{@link org.machanism.machai.ai.manager.Usage} is an immutable value object representing token usage
 *   for a single interaction.</li>
 * </ul>
 *
 * <h2>Identifier handling</h2>
 * <p>Provider resolution expects identifiers in the form {@code Provider:Model}. If the provider segment is
 * omitted, the default {@code none} provider is selected and the original input value is preserved as the model
 * name. Short provider names are translated into conventional implementation class names under
 * {@code org.machanism.machai.ai.provider}, while fully qualified class names can be supplied directly.
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
