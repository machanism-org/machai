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
 * Provides management utilities and data structures for working with generative
 * AI providers and their token-usage metrics.
 *
 * <p>
 * This package contains the provider manager responsible for resolving and
 * instantiating chat and embedding providers from model identifiers, together
 * with lightweight usage records and an in-memory statistics registry. Provider
 * implementations are loaded dynamically by {@link org.machanism.machai.ai.manager.GenaiProviderManager}
 * using configured model strings such as {@code OpenAI:gpt-4o} or
 * {@code OpenAI:text-embedding-3-small}.
 * </p>
 *
 * <p>
 * Token usage reported by providers can be represented with
 * {@link org.machanism.machai.ai.manager.Usage} and aggregated through
 * {@link org.machanism.machai.ai.manager.UsageStatistics}. This allows callers
 * to record prompt, cached prompt, and completion token counts per model and to
 * log summarized usage information for operational visibility.
 * </p>
 *
 * <p>
 * Typical responsibilities covered by this package include:
 * </p>
 * <ul>
 * <li>Resolving provider implementation classes from provider/model strings.</li>
 * <li>Initializing chat and embedding provider instances with application configuration.</li>
 * <li>Capturing immutable token-usage values for individual AI interactions.</li>
 * <li>Aggregating and logging usage totals grouped by model identifier.</li>
 * </ul>
 */
package org.machanism.machai.ai.manager;
