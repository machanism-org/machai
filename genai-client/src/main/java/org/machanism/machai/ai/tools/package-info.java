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
 * Host-integrated function tools exposed to a {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>
 * This package contains a curated, auditable set of tool installers (SPI implementations) that register named
 * functions with a provider. The tools are executed inside the host application and are designed to keep access
 * scoped and observable.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li><b>Tool registration</b>: implementations of {@link org.machanism.machai.ai.tools.FunctionTools} register
 *       tools via
 *       {@link org.machanism.machai.ai.manager.GenAIProvider#addTool(String, String, java.util.function.Function, String...)}.</li>
 *   <li><b>Discovery and wiring</b>: installers are typically discovered using {@link java.util.ServiceLoader} and
 *       applied by {@link org.machanism.machai.ai.tools.FunctionToolsLoader}.</li>
 *   <li><b>Controlled access</b>: installers generally interpret paths relative to a host-supplied working
 *       directory, bound output sizes, and apply deny/validation checks for potentially risky operations.</li>
 * </ul>
 *
 * <h2>Key components</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.tools.FunctionTools} – SPI implemented by tool installers</li>
 *   <li>{@link org.machanism.machai.ai.tools.FunctionToolsLoader} – discovers and applies installers</li>
 *   <li>{@link org.machanism.machai.ai.tools.FileFunctionTools} – file-system utilities relative to a working directory</li>
 *   <li>{@link org.machanism.machai.ai.tools.CommandFunctionTools} – command execution and process termination</li>
 *   <li>{@link org.machanism.machai.ai.tools.WebFunctionTools} – HTTP fetching and REST calls</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = ...;
 * FunctionToolsLoader loader = FunctionToolsLoader.getInstance();
 * loader.setConfiguration(conf);
 * loader.applyTools(provider);
 * }
 * </pre>
 */
package org.machanism.machai.ai.tools;
