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
 *     - Do not use escaping in `{@code ...}` tags.   
 *     - When showing `${...}` variable placeholders, do not use escaping or wrap them in `{@code ...}`.
 */

/**
 * Host-integrated tool implementations that can be registered with a
 * {@link org.machanism.machai.ai.manager.GenAIProvider} to expose a small, auditable set of capabilities to an AI
 * workflow.
 *
 * <p>
 * This package contains installer classes that register named tool functions (for example, file I/O, directory
 * listing, command execution, and HTTP page retrieval) and small supporting utilities used by those installers.
 * Callers typically choose which tool sets to install and can constrain availability by registering only the
 * functions they intend an AI workflow to access.
 *
 * <h2>Tool installers</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.tools.FileFunctionTools} – file and directory operations (read, write, list)</li>
 *   <li>{@link org.machanism.machai.ai.tools.CommandFunctionTools} – command execution with bounded output capture</li>
 *   <li>{@link org.machanism.machai.ai.tools.WebPageFunctionTools} – HTTP GET page fetch (HTML or extracted text)</li>
 *   <li>{@link org.machanism.machai.ai.tools.FunctionToolsLoader} – convenience installer for common tool sets</li>
 * </ul>
 *
 * <h2>Supporting utilities</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.tools.LimitedStringBuilder} – retains only the last N characters of accumulated
 *       output</li>
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
