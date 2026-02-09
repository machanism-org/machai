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
 * <p>This package contains a {@link org.machanism.machai.ai.manager.GenAIProvider} implementation that obtains model
 * responses by automating a target generative-AI provider through its web user interface (UI).
 *
 * <p>Automation is executed using <a href="https://ganteater.com">Anteater</a> workspace recipes. The provider bridges
 * the core GenAI Client prompt API to an Anteater workspace, including configuration loading, workspace initialization,
 * and recipe execution.
 *
 * <h2>How it works</h2>
 * <ul>
 *   <li><strong>Configuration</strong>: selected via
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#model(String)} and loaded when the workspace is
 *       initialized.</li>
 *   <li><strong>Workspace initialization</strong>: performed via
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#setWorkingDir(java.io.File)}. The provider uses the
 *       supplied working directory as the root and resolves a recipe/config directory from system property
 *       {@code recipes} (default: {@code genai-client/src/main/resources}) if it exists under the working directory.</li>
 *   <li><strong>Prompt submission</strong>: executed via
 *       {@link org.machanism.machai.ai.provider.web.WebProvider#perform()} by running the {@code "Submit Prompt"}
 *       recipe. Prompts are passed into the workspace through system variable {@code INPUTS}; the recipe is expected to
 *       return the final text in variable {@code result}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("Web:CodeMie");
 * provider.model("config.yaml");
 * provider.setWorkingDir(new File("C:\\path\\to\\project"));
 * String response = provider.perform();
 * }</pre>
 *
 * <h2>Lifecycle and constraints</h2>
 * <ul>
 *   <li>The underlying Anteater workspace is maintained in static state and is intended to be initialized once per JVM.</li>
 *   <li>Changing the model/configuration or working directory after initialization is not supported and results in an
 *       {@link java.lang.IllegalArgumentException}.</li>
 *   <li>Call {@link org.machanism.machai.ai.provider.web.WebProvider#close()} to release workspace resources.</li>
 * </ul>
 */
package org.machanism.machai.ai.provider.web;
