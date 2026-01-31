/**
 * Provider-agnostic API root for the Machanism generative AI (GenAI) client.
 *
 * <p>This package defines the stable, provider-neutral surface that application code should depend on. Provider
 * selection, initialization, and access are handled by the manager APIs in {@code org.machanism.machai.ai.manager},
 * while concrete provider implementations live under {@code org.machanism.machai.ai.provider}.
 *
 * <h2>Typical usage</h2>
 * <ol>
 *   <li>Configure the desired provider for the current environment.</li>
 *   <li>Initialize and obtain the active provider via
 *       {@link org.machanism.machai.ai.manager.GenAIProviderManager}.</li>
 *   <li>Invoke provider-neutral operations through
 *       {@link org.machanism.machai.ai.manager.GenAIProvider}.</li>
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
