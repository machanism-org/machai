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
 * Web UI automation backed {@link org.machanism.machai.ai.manager.GenAIProvider} implementations.
 *
 * <p>This package contains providers that obtain responses from a generative AI service by automating its web user
 * interface (UI) using <a href="https://ganteater.com">Anteater</a> workspace recipes.
 *
 * <p>The primary entry point is {@link org.machanism.machai.ai.provider.web.WebProvider}, which:
 * <ul>
 *   <li>selects an Anteater workspace configuration via
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#model(String)};</li>
 *   <li>initializes a shared {@code AEWorkspace} for a project directory via
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#setWorkingDir(java.io.File)} (intended to be called once
 *       per JVM); and</li>
 *   <li>submits accumulated prompts by running the {@code "Submit Prompt"} recipe via
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#perform()} and returning the {@code result} variable.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
 * provider.model("config.yaml");
 * provider.setWorkingDir(new File("C:\\path\\to\\project"));
 * String response = provider.perform();
 * }</pre>
 */
package org.machanism.machai.ai.provider.web;
