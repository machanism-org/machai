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

/**
 * Provides a {@link org.machanism.machai.ai.manager.GenAIProvider} implementation that obtains model responses
 * by automating a target GenAI service through its web user interface.
 *
 * <p>Automation is executed via <a href="https://ganteater.com">Anteater</a> workspace recipes. The primary
 * entry point is {@link org.machanism.machai.ai.provider.web.WebProvider}, which:
 *
 * <ul>
 *   <li>selects an Anteater workspace configuration via
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#model(String)},</li>
 *   <li>initializes the workspace for a project directory via
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#setWorkingDir(java.io.File)}, and</li>
 *   <li>submits accumulated prompts by executing the {@code "Submit Prompt"} recipe via
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#perform()}.</li>
 * </ul>
 *
 * <p><strong>Lifecycle note:</strong> the underlying Anteater workspace is stored in static state and is
 * intended to be initialized once per JVM; attempts to change the working directory or configuration after
 * initialization result in an error.
 */
package org.machanism.machai.ai.provider.web;
