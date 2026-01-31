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
 */

/**
 * Provider resolution, interaction, and host-side tool wiring for generative AI integrations.
 *
 * <p>This package provides:
 * <ul>
 *   <li>A provider SPI ({@link org.machanism.machai.ai.manager.GenAIProvider}) for building and executing requests,
 *       including prompts, file attachments, embeddings, and host-implemented tool registration.</li>
 *   <li>A resolver ({@link org.machanism.machai.ai.manager.GenAIProviderManager}) that maps a {@code Provider:Model}
 *       identifier to a concrete provider instance.</li>
 *   <li>Optional tool installers ({@link org.machanism.machai.ai.manager.FileFunctionTools},
 *       {@link org.machanism.machai.ai.manager.CommandFunctionTools}, and
 *       {@link org.machanism.machai.ai.manager.SystemFunctionTools}) that register common host capabilities
 *       (filesystem and command execution) with a provider.</li>
 * </ul>
 *
 * <p><strong>Security note:</strong> tools execute on the hosting machine. Applications should restrict allowable paths,
 * commands, working directories, and timeouts according to their security requirements.
 */
package org.machanism.machai.ai.manager;
