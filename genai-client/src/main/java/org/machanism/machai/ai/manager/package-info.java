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
 * Provider resolution and usage tracking for generative AI integrations.
 *
 * <p>This package contains the classes that translate configured chat model identifiers into concrete
 * {@link org.machanism.machai.ai.provider.Genai} provider implementations and collect token-consumption
 * metrics produced by those providers. It supports both provider instantiation for runtime use and
 * post-execution aggregation of usage statistics for logging and inspection.
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>Resolve provider implementations from model identifiers such as {@code OpenAI:gpt-4o-mini}.</li>
 *   <li>Initialize resolved providers with a
 *   {@link org.machanism.macha.core.commons.configurator.Configurator} after applying the selected
 *   {@code chatModel} value.</li>
 *   <li>Represent token usage for individual invocations with immutable usage records.</li>
 *   <li>Aggregate and log usage information across one or more configured models.</li>
 * </ul>
 *
 * <h2>Included types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.GenaiProviderManager} creates provider instances from
 *   configured model identifiers.</li>
 *   <li>{@link org.machanism.machai.ai.manager.Usage} stores token counts for a single provider call.</li>
 *   <li>{@link org.machanism.machai.ai.manager.UsageStatistics} groups and reports usage metrics by model
 *   identifier.</li>
 * </ul>
 *
 * <h2>Identifier handling</h2>
 * <p>Provider identifiers are typically supplied in the form {@code Provider:Model}. When the provider segment
 * does not contain a package separator, the implementation class is inferred using the package naming
 * convention {@code org.machanism.machai.ai.provider.<provider-lowercase>.<Provider>Provider}. When the
 * provider segment is already a fully qualified class name, it is loaded directly.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * Genai provider = GenaiProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 * String response = provider.perform();
 *
 * UsageStatistics.addUsage("OpenAI:gpt-4o-mini", provider.usage());
 * UsageStatistics.logUsageForModel("OpenAI:gpt-4o-mini");
 * }</pre>
 */
package org.machanism.machai.ai.manager;
