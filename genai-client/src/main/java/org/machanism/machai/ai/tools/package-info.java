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
 * Host-side function tooling for integrating controlled local capabilities with a
 * {@link org.machanism.machai.ai.provider.Genai} provider.
 *
 * <p>This package contains the service-provider interface and concrete tool installers used to expose selected
 * host functionality to AI provider implementations. The tools in this package focus on capabilities that are
 * commonly needed during assisted development workflows, such as command execution, HTTP access, and package-level
 * tool discovery and registration.</p>
 *
 * <h2>Package responsibilities</h2>
 * <ul>
 *   <li>Define the {@link org.machanism.machai.ai.tools.FunctionTools} SPI used by tool installers.</li>
 *   <li>Discover and apply tool installers through {@link org.machanism.machai.ai.tools.FunctionToolsLoader}.</li>
 *   <li>Provide host-managed command execution with validation and bounded output capture.</li>
 *   <li>Provide HTTP and REST access helpers for web content retrieval and API invocation.</li>
 *   <li>Support security-related checks and utility types used by installed tools.</li>
 * </ul>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.tools.FunctionTools} - SPI for registering tools with a provider.</li>
 *   <li>{@link org.machanism.machai.ai.tools.FunctionToolsLoader} - singleton loader that discovers and applies tool installers.</li>
 *   <li>{@link org.machanism.machai.ai.tools.CommandFunctionTools} - installs command execution and termination tools.</li>
 *   <li>{@link org.machanism.machai.ai.tools.WebFunctionTools} - installs web content and REST API tools.</li>
 *   <li>{@link org.machanism.machai.ai.tools.CommandSecurityChecker} - evaluates deny-list rules for command validation.</li>
 *   <li>{@link org.machanism.machai.ai.tools.LimitedStringBuilder} - retains only the trailing portion of large text output.</li>
 * </ul>
 *
 * <h2>Usage notes</h2>
 * <p>Tool implementations are executed within the host application rather than by the model itself. As a result,
 * callers should ensure that working directories, network access, configuration values, and security policies are
 * supplied and enforced by the surrounding runtime. Paths are expected to be resolved relative to a host-provided
 * project directory, and configuration placeholders may be resolved through an injected configurator.</p>
 */
package org.machanism.machai.ai.tools;
