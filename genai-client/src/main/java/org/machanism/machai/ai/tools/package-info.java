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
 * Host-integrated “tool” implementations that can be registered with a
 * {@link org.machanism.machai.ai.manager.GenAIProvider} to expose a small, auditable set of capabilities to an AI
 * workflow.
 *
 * <p>
 * Tools in this package are typically installed by registering named functions via
 * {@link org.machanism.machai.ai.manager.GenAIProvider#addTool(String, String, java.util.function.Function, String...)}.
 * Each function should be narrowly scoped (for example, reading a file, listing a directory, executing a command, or
 * fetching a web page) so that calling code can reason about and constrain what the AI can do.
 *
 * <h2>Tool installers</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.tools.FileFunctionTools} – file and directory operations (read, write, list)</li>
 * <li>{@link org.machanism.machai.ai.tools.CommandFunctionTools} – command execution with bounded output capture</li>
 * <li>{@link org.machanism.machai.ai.tools.WebPageFunctionTools} – HTTP GET page fetch (HTML or extracted text)</li>
 * <li>{@link org.machanism.machai.ai.tools.SystemFunctionTools} – convenience installer for the common tool set</li>
 * </ul>
 *
 * <h2>Supporting utilities</h2>
 * <ul>
 * <li>{@link org.machanism.machai.ai.tools.LimitedStringBuilder} – retains only the last N characters of accumulated
 * output</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * GenAIProvider provider = ...;
 * new SystemFunctionTools().applyTools(provider);
 * }
 * </pre>
 */
package org.machanism.machai.ai.tools;
