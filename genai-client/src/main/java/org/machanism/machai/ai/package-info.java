/**
 * Provider-agnostic public API root for the Machanism generative AI (GenAI) client.
 *
 * <p>This package defines the stable, provider-neutral surface that application code should depend on.
 * Concrete integrations (for example, OpenAI or a remote web/orchestrator-backed provider) are exposed through
 * implementations located in sub-packages, and are selected and managed via the provider manager APIs.
 *
 * <h2>Architecture</h2>
 * <ul>
 *   <li><b>Provider management</b> – {@code org.machanism.machai.ai.manager} contains the SPI-facing interfaces and
 *       utilities used to discover, initialize, and access a chosen provider.</li>
 *   <li><b>Providers</b> – {@code org.machanism.machai.ai.provider} contains concrete provider implementations.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <ol>
 *   <li>Configure the desired provider for the current environment.</li>
 *   <li>Initialize and retrieve the active provider via
 *       {@link org.machanism.machai.ai.manager.GenAIProviderManager}.</li>
 *   <li>Invoke provider-neutral operations through the
 *       {@link org.machanism.machai.ai.manager.GenAIProvider} interface.</li>
 * </ol>
 */
package org.machanism.machai.ai;

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
 * 		- Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
