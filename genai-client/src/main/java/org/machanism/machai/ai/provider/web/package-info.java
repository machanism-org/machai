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
 * <p>This package contains providers that obtain responses from a GenAI service by driving its browser-based UI using * <a href="https://ganteater.com">Anteater</a> workspace recipes.
 *
 * <h2>How it works</h2>
 * <p>{@link org.machanism.machai.ai.provider.web.WebProvider} coordinates a shared Anteater {@code AEWorkspace} and executes * a recipe-based workflow to submit prompts and retrieve results.
 *
 * <ol>
 *   <li>Initialize provider configuration via {@code init(Configurator)} (selects the Anteater configuration name).</li>
 *   <li>Initialize the workspace by calling *       {@link org.machanism.machai.ai.provider.web.WebProvider#setWorkingDir(java.io.File)} once. This sets the workspace *       start directory, injects variables (e.g., {@code PROJECT_DIR}), loads the Anteater configuration, and runs setup *       nodes.</li>
 *   <li>Queue one or more prompts using the provider API.</li>
 *   <li>Execute {@link org.machanism.machai.ai.provider.web.WebProvider#perform()} to run the {@code "Submit Prompt"}
 *       recipe. Prompts are passed via the {@code INPUTS} system variable and the recipe is expected to return the final
 *       response in the {@code result} variable.</li>
 * </ol>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * WebProvider provider = new WebProvider();
 * provider.init(configurator); // selects chatModel / config name
 * provider.setWorkingDir(new File("C:\\path\\to\\project"));
 *
 * // provider.prompt("..."); // add one or more prompts
 * String responseText = provider.perform();
 * System.out.println(responseText);
 * }</pre>
 *
 * <h2>State and thread safety</h2>
 * <p>Browser automation is not thread-safe. The underlying workspace is stored in static state and is intended to be
 * initialized once per JVM; attempts to change the working directory after initialization fail.
 */
package org.machanism.machai.ai.provider.web;
