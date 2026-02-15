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
 * Web UI automation-backed {@link org.machanism.machai.ai.manager.GenAIProvider} implementations.
 *
 * <p>This package provides {@link org.machanism.machai.ai.provider.web.WebProvider}, which obtains model responses by
 * automating a target GenAI service through its web user interface using
 * <a href="https://ganteater.com">Anteater</a> workspace recipes.
 *
 * <h2>Typical usage</h2>
 * <p>Callers typically configure the target automation recipe ("model") and working directory, then execute the
 * automation to obtain a response.
 *
 * <pre>{@code
 * WebProvider provider = new WebProvider()
 *     .model("openai-chatgpt")
 *     .setWorkingDir(new File("C:\\anteater-workspace"));
 *
 * GenAIResponse response = provider.perform();
 * System.out.println(response.text());
 * }</pre>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>Select the Anteater configuration via {@link org.machanism.machai.ai.provider.web.WebProvider#model(String)}.</li>
 *   <li>Initialize the workspace via
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#setWorkingDir(java.io.File)}.</li>
 *   <li>Execute automation and obtain the response via {@link org.machanism.machai.ai.provider.web.WebProvider#perform()}.</li>
 * </ol>
 *
 * <h2>State and thread safety</h2>
 * <p>Automation is not thread-safe. The underlying workspace is held in static state and is initialized once per JVM;
 * attempts to change the working directory or configuration after initialization result in an error.
 */
package org.machanism.machai.ai.provider.web;
