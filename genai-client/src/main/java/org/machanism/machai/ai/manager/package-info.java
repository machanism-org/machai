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
 * Provider resolution, interaction, and host-side tool wiring for Generative AI integrations.
 *
 * <p>This package contains:
 * <ul>
 *   <li>A provider SPI ({@link org.machanism.machai.ai.manager.GenAIProvider}) for submitting prompts, attaching files,
 *       computing embeddings, registering host-implemented tools, and executing a request.</li>
 *   <li>A reflection-based resolver ({@link org.machanism.machai.ai.manager.GenAIProviderManager}) that creates provider
 *       instances from a {@code Provider:Model} identifier.</li>
 *   <li>Optional tool installers ({@link org.machanism.machai.ai.manager.FileFunctionTools},
 *       {@link org.machanism.machai.ai.manager.CommandFunctionTools}, and
 *       {@link org.machanism.machai.ai.manager.SystemFunctionTools}) that register common host capabilities (file I/O and
 *       command execution) with a provider via {@link org.machanism.machai.ai.manager.GenAIProvider#addTool(String, String, java.util.function.Function, String...)}.</li>
 * </ul>
 *
 * <p><strong>Security note:</strong> tool functions run on the hosting machine. Applications should constrain the working
 * directory, permissible paths/commands, and execution timeouts according to their security requirements.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini");
 * provider.setWorkingDir(new File("."));
 * new SystemFunctionTools().applyTools(provider);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this project.");
 *
 * String response = provider.perform();
 * }</pre>
 */
package org.machanism.machai.ai.manager;
