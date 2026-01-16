/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND THIS `package-info.java`!**
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * 
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the packages overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 *      
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * 
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * 
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */

/**
 * Web UI automation based GenAI provider.
 *
 * <p>This package provides a {@link org.machanism.machai.ai.manager.GenAIProvider} implementation that obtains
 * model responses by automating a target GenAI service through its web user interface. Automation is executed
 * via <a href="https://ganteater.com">Anteater</a> workspace recipes.
 *
 * <p>The primary implementation is {@link org.machanism.machai.ai.provider.web.WebProvider}. It:
 * <ul>
 *   <li>selects an Anteater workspace configuration via {@code model(String)},</li>
 *   <li>initializes the workspace for a project directory via {@code setWorkingDir(java.io.File)}, and</li>
 *   <li>runs the {@code "Submit Prompt"} recipe via {@code perform()} to submit the current prompt list and
 *       return the captured result.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * {@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
 * provider.model("config.yaml");
 * provider.setWorkingDir(new File("/path/to/project"));
 * String response = provider.perform();
 * }
 * </pre>
 *
 * <h2>Operational notes</h2>
 * <ul>
 *   <li>Execution depends on the configured Anteater recipes and workspace configuration being available and
 *       compatible with the target web UI.</li>
 *   <li>The provider maintains static workspace state; it is not thread-safe and does not support switching
 *       working directories within the same JVM instance.</li>
 * </ul>
 */
package org.machanism.machai.ai.provider.web;
