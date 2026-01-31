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
 * GenAI provider management and host-side tool integration.
 *
 * <p>This package provides the service-provider interface (SPI) used to integrate concrete Generative-AI backends and the
 * utilities to resolve, create, and configure provider instances.
 *
 * <p>At a high level, callers obtain a {@link org.machanism.machai.ai.manager.GenAIProvider} via
 * {@link org.machanism.machai.ai.manager.GenAIProviderManager} and then configure it with:
 *
 * <ul>
 *   <li>Model selection (often using a {@code Provider:Model} identifier).</li>
 *   <li>System instructions and user prompts.</li>
 *   <li>Optional host-side &quot;tools&quot; (functions) that the model can request to execute.</li>
 * </ul>
 *
 * <p>The tool helpers in this package register controlled capabilities such as file I/O and command execution through
 * {@link org.machanism.machai.ai.manager.GenAIProvider#addTool(String, String, java.util.function.Function, String...)}.
 * Implementations typically scope these capabilities to a provider-controlled working directory and apply additional
 * safety policy.
 *
 * <h2>Typical components</h2>
 *
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.FileFunctionTools} - file read/write and directory listing operations.</li>
 *   <li>{@link org.machanism.machai.ai.manager.CommandFunctionTools} - command execution within the working directory.</li>
 *   <li>{@link org.machanism.machai.ai.manager.SystemFunctionTools} - convenience installer for both file and command
 *       tools.</li>
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini");
 * new SystemFunctionTools().applyTools(provider);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this project.");
 *
 * String response = provider.perform();
 * }</pre>
 */
package org.machanism.machai.ai.manager;
