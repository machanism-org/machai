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
 * Provider-resolution and usage-tracking infrastructure for generative-AI integrations.
 *
 * <p>This package contains the manager-side API used to resolve {@link org.machanism.machai.ai.provider.Genai}
 * implementations from provider and model identifiers, initialize them with application configuration, and collect
 * token-consumption statistics across provider invocations.
 *
 * <h2>Core responsibilities</h2>
 * <ul>
 *   <li>Resolve a concrete provider implementation from a model identifier such as
 *   {@code OpenAI:gpt-4o-mini} or a provider-specific class name.</li>
 *   <li>Initialize provider instances using a {@link org.machanism.macha.core.commons.configurator.Configurator}.</li>
 *   <li>Capture and aggregate per-call {@link org.machanism.machai.ai.manager.Usage} metrics for logging.</li>
 * </ul>
 *
 * <h2>Included types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.GenaiProviderManager} creates and initializes provider instances and
 *   logs aggregated token usage.</li>
 *   <li>{@link org.machanism.machai.ai.manager.Usage} represents immutable token-usage metrics for a single AI
 *   interaction.</li>
 * </ul>
 *
 * <h2>Provider identifiers</h2>
 * <p>Provider resolution expects identifiers in the form {@code Provider:Model}. When the provider segment is omitted,
 * the implementation falls back to the default "none" provider while preserving the supplied model value.
 * Short provider names are mapped to conventional implementation class names under
 * {@code org.machanism.machai.ai.provider}, while fully qualified class names may also be supplied directly.
 *
 * <h2>Typical usage</h2>
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
