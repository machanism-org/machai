/*-
 * @guidance:
 * **Task:**  
 * Scan the current folder and add comprehensive Javadoc comments to all Java classes and to the `package-info.java` file within this package.

 * **Instructions:**  
 * - For each Java class in this folder, generate and insert detailed Javadoc comments describing the class, its purpose, and its public methods and fields.
 * - If a `package-info.java` file exists, add or update its Javadoc to provide an overview of the package, its responsibilities, and any important usage notes.
 * - Ensure all Javadoc follows standard Java documentation conventions and is clear, concise, and informative.
 * - Do not modify any code logic—only add or improve Javadoc comments.
 * 
 * Would you like this prompt tailored for a specific LLM or code review tool? * 
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
 * Host-integrated function tools that expose a small, auditable set of local capabilities to a
 * {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>
 * The classes in this package register named "tools" (functions) with a provider. These tools are intended to
 * run inside the host application and therefore focus on controlled access patterns, such as:
 * </p>
 * <ul>
 *   <li>Reading, writing, and enumerating files relative to a host-supplied working directory</li>
 *   <li>Executing system commands with heuristic deny-list checks, project-root confinement, and bounded output</li>
 *   <li>Fetching web content and invoking REST endpoints with optional header templating and authentication</li>
 * </ul>
 *
 * <h2>Architecture overview</h2>
 * <p>
 * Tool installers implement {@link org.machanism.machai.ai.tools.FunctionTools} and register functions via
 * {@link org.machanism.machai.ai.manager.GenAIProvider#addTool(String, String, java.util.function.Function, String...)}.
 * Installers are typically discovered using {@link java.util.ServiceLoader} and applied by
 * {@link org.machanism.machai.ai.tools.FunctionToolsLoader}.
 * </p>
 *
 * <h2>Key components</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.tools.FunctionTools} – SPI implemented by tool installers</li>
 *   <li>{@link org.machanism.machai.ai.tools.FunctionToolsLoader} – discovers tool installers via
 *       {@link java.util.ServiceLoader} and applies them to a provider</li>
 *   <li>{@link org.machanism.machai.ai.tools.FileFunctionTools} – file-system utilities</li>
 *   <li>{@link org.machanism.machai.ai.tools.CommandFunctionTools} – command execution and process termination</li>
 *   <li>{@link org.machanism.machai.ai.tools.WebFunctionTools} – web fetching and REST calls</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * GenAIProvider provider = ...;
 * FunctionToolsLoader.getInstance().applyTools(provider);
 * }
 * </pre>
 */
package org.machanism.machai.ai.tools;
