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
 * Manager and service provider interfaces (SPI) for GenAI integrations.
 *
 * <p>This package contains the core abstractions that allow the application to select and operate a concrete
 * generative-AI provider (for example OpenAI, Gemini, a local model, or a no-op provider) at runtime. The
 * abstractions focus on:
 *
 * <ul>
 *   <li>building a request (instructions, prompts, and optional attachments/tooling),</li>
 *   <li>executing a provider run and returning the textual result, and</li>
 *   <li>capturing and aggregating token usage metrics.</li>
 * </ul>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProvider} &ndash; fluent provider contract for collecting input
 *   and executing a run.</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProviderManager} &ndash; factory/registry for resolving a provider
 *   implementation by {@code Provider:Model} identifier and for aggregating usage.</li>
 *   <li>{@link org.machanism.machai.ai.manager.Usage} &ndash; immutable token usage metrics for a single provider run.</li>
 * </ul>
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
