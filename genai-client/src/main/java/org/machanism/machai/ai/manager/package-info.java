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
 *      - Ensure that the package-level Javadoc is placed immediately before the `package` declaration.
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
 * Provides provider construction and token-usage collection for the
 * application's generative-AI integrations.
 *
 * <p>The package contains the following components:</p>
 * <ul>
 * <li>{@link GenaiProviderManager} parses a {@code Provider:Model} identifier,
 *     locates the provider implementation, creates it through its public
 *     no-argument constructor, and initializes it with a {@code Configurator}.</li>
 * <li>{@link Usage} is an immutable value object containing input, cached-input, and output
 *     token counts for one provider interaction.</li>
 * <li>{@link UsageStatistics} stores usage records by model identifier and
 *     provides methods for retrieving records and logging token totals.</li>
 * </ul>
 *
 * <h2>Provider resolution</h2>
 * <p>Pass a provider and model separated by a colon. For a conventional provider
 * name, the manager first attempts
 * {@code org.machanism.machai.ai.provider.impl.{Provider}Provider}; if that
 * class is unavailable, it attempts a nested provider class in
 * {@link GenaiProviderManager}. The chat-provider method accepts only provider
 * names composed of Java identifier characters. The embedding-provider method
 * also treats a provider segment containing a dot as a fully qualified class
 * name. The selected class must have a public no-argument constructor and
 * implement the requested provider interface.</p>
 *
 * <h2>Usage tracking</h2>
 * <p>Initialize the statistics class if desired during application startup, then
 * add each provider response to the registry. Records are grouped by the exact
 * model identifier supplied by the caller. Retrieval of one model returns a
 * defensive copy of its list; retrieval of all models returns a shallow copy of
 * the registry map.</p>
 *
 * <h2>Example</h2>
 * <pre>
 * UsageStatistics.init();
 * Configurator conf = ...;
 * Genai chat = GenaiProviderManager.getProvider("OpenAI:gpt-4o", conf);
 * EmbeddingProvider embeddings = GenaiProviderManager.getEmbeddingProvider(
 *         "OpenAI:text-embedding-3-small", conf);
 * UsageStatistics.addUsage("OpenAI:gpt-4o", new Usage(500, 100, 200));
 * UsageStatistics.logUsage();
 * </pre>
 *
 * @see org.machanism.machai.ai.provider.Genai
 * @see org.machanism.machai.ai.provider.EmbeddingProvider
 * @see org.machanism.macha.core.commons.configurator.Configurator
 */
package org.machanism.machai.ai.manager;
