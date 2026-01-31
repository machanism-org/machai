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
 * <p>This package defines the API used to obtain and interact with concrete Generative-AI provider implementations
 * ({@link org.machanism.machai.ai.manager.GenAIProvider}) and includes a small set of built-in &quot;tools&quot; that expose
 * controlled host capabilities (file access and command execution) to a provider.
 *
 * <h2>Key responsibilities</h2>
 * <ul>
 *   <li><strong>Provider resolution and instantiation</strong> via
 *       {@link org.machanism.machai.ai.manager.GenAIProviderManager} using a {@code Provider:Model} identifier.</li>
 *   <li><strong>Provider interaction</strong> through prompts, instructions, file attachments, embeddings, and response
 *       generation via {@link org.machanism.machai.ai.manager.GenAIProvider}.</li>
 *   <li><strong>Tool registration</strong> by installing host-side functions with
 *       {@link org.machanism.machai.ai.manager.GenAIProvider#addTool(String, String, java.util.function.Function, String...)}.
 *       Tools are executed in a provider-supplied working directory and are expected to be further restricted by the
 *       hosting application as needed.</li>
 * </ul>
 *
 * <h2>Built-in tool installers</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.FileFunctionTools} - provides file read/write and directory listing
 *       functions.</li>
 *   <li>{@link org.machanism.machai.ai.manager.CommandFunctionTools} - provides command execution.</li>
 *   <li>{@link org.machanism.machai.ai.manager.SystemFunctionTools} - convenience installer that applies both file and
 *       command tools.</li>
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
