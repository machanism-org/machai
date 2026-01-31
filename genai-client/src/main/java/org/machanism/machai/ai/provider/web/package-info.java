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
 *          and `&gt;` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */

/**
 * Web-automation-backed {@link org.machanism.machai.ai.manager.GenAIProvider} implementations.
 *
 * <p>This package contains providers that interact with a target GenAI service through its web UI. Automation is
 * executed via <a href="https://ganteater.com">Anteater</a> workspaces/recipes and is orchestrated by
 * {@link org.machanism.machai.ai.provider.web.WebProvider}.
 *
 * <h2>Typical usage</h2>
 * <ol>
 *   <li>Select an Anteater configuration using {@link org.machanism.machai.ai.provider.web.WebProvider#model(String)}.</li>
 *   <li>Set the workspace project directory with
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#setWorkingDir(java.io.File)}.</li>
 *   <li>Build the prompt using the provider prompt API and submit it with
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#perform()}, which runs the {@code "Submit Prompt"}
 *       recipe.</li>
 * </ol>
 *
 * <h2>Lifecycle and constraints</h2>
 * <ul>
 *   <li>The underlying Anteater workspace is held in static state; configuration and working directory are intended
 *       to be set once per JVM.</li>
 *   <li>Changing the configuration or working directory after initialization is not supported and results in an
 *       error.</li>
 *   <li>Call {@link org.machanism.machai.ai.provider.web.WebProvider#close()} to release workspace resources.</li>
 * </ul>
 */
package org.machanism.machai.ai.provider.web;
