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
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */

/**
 * GenAI provider management and host tool integration.
 *
 * <p>This package provides the service-provider interface (SPI) for GenAI backends
 * ({@link org.machanism.machai.ai.manager.GenAIProvider}) along with utilities to resolve and instantiate
 * a concrete provider ({@link org.machanism.machai.ai.manager.GenAIProviderManager}). Providers are typically
 * selected via a {@code Provider:Model} identifier.
 *
 * <p>The package also contains host-side "tool" implementations that can be registered with a provider (for example,
 * to allow file access or command execution) through
 * {@link org.machanism.machai.ai.manager.GenAIProvider#addTool(String, String, java.util.function.Function, String...)}.
 *
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.FileFunctionTools} provides basic file read/write and directory listing
 *       operations rooted at a provider-controlled working directory.</li>
 *   <li>{@link org.machanism.machai.ai.manager.CommandFunctionTools} exposes command execution within that working
 *       directory (subject to the caller/provider safety policy).</li>
 *   <li>{@link org.machanism.machai.ai.manager.SystemFunctionTools} is a convenience installer for both file and
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
