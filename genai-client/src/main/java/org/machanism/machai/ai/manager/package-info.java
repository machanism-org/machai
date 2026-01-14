/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND THIS `package-info.java`!**
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * 
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package's overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 *      
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * 
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */

/**
 * Provider discovery, instantiation, and optional system tool integration for the MachAI GenAI client.
 *
 * <p>
 * The primary abstraction is {@link org.machanism.machai.ai.manager.GenAIProvider}. A provider is typically
 * obtained using {@link org.machanism.machai.ai.manager.GenAIProviderManager}, which resolves a concrete
 * implementation from a {@code Provider:Model} identifier (for example, {@code OpenAI:gpt-4o-mini}).
 *
 * <p>
 * This package also offers optional "function tools" that can be attached to a provider to expose controlled
 * access to local capabilities:
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.FileFunctionTools} – read/write/list filesystem content under a working directory</li>
 *   <li>{@link org.machanism.machai.ai.manager.CommandFunctionTools} – execute shell commands from a working directory</li>
 *   <li>{@link org.machanism.machai.ai.manager.SystemFunctionTools} – convenience wrapper that installs both tool sets</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini");
 * provider.instructions("Be concise and include sources when available.");
 * provider.prompt("Explain the CAP theorem in one paragraph.");
 *
 * // Optionally expose additional tools to the provider
 * SystemFunctionTools tools = new SystemFunctionTools();
 * tools.applyTools(provider);
 *
 * String response = provider.perform();
 * provider.close();
 * }</pre>
 */
package org.machanism.machai.ai.manager;
