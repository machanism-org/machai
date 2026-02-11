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
 * Web-automation-backed {@link org.machanism.machai.ai.manager.GenAIProvider} implementation.
 *
 * <p>This package contains {@link org.machanism.machai.ai.manager.GenAIProvider} implementations that obtain model
 * responses by automating a target generative-AI provider through its web user interface (UI) using
 * <a href="https://ganteater.com">Anteater</a> workspace recipes.
 *
 * <h2>Overview</h2>
 * <ul>
 *   <li><strong>Configuration selection</strong>: typically via
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#model(String)}.</li>
 *   <li><strong>Workspace initialization</strong>: via
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#setWorkingDir(java.io.File)}; recipe/config location
 *       may be overridden with system property {@code recipes}.</li>
 *   <li><strong>Prompt submission</strong>: via {@link org.machanism.machai.ai.provider.web.WebProvider#perform()} by
 *       running the {@code "Submit Prompt"} recipe and reading the {@code result} variable.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
 * provider.model("config.yaml");
 * provider.setWorkingDir(new File("C:\\path\\to\\project"));
 * String response = provider.perform();
 * }</pre>
 */
package org.machanism.machai.ai.provider.web;
