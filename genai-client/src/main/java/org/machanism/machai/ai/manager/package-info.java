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
 * 		- Use proper Markdown or HTML formatting for readability.
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
 * Provider management and tool integration for the MachAI GenAI client.
 *
 * <p>
 * This package defines the main abstraction for interacting with a concrete GenAI backend
 * (see {@link org.machanism.machai.ai.manager.GenAIProvider}) and the mechanism for resolving an
 * appropriate implementation at runtime (see {@link org.machanism.machai.ai.manager.GenAIProviderManager}).
 *
 * <p>
 * In addition, the package supplies optional "function tools" that can be applied to a provider
 * to expose controlled access to local system capabilities:
 * {@link org.machanism.machai.ai.manager.FileFunctionTools},
 * {@link org.machanism.machai.ai.manager.CommandFunctionTools}, and
 * {@link org.machanism.machai.ai.manager.SystemFunctionTools}.
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini");
 * String response = provider.prompt("Explain the CAP theorem in one paragraph.");
 *
 * // Optionally expose additional tools to the provider
 * SystemFunctionTools tools = new SystemFunctionTools();
 * tools.applyTools(provider);
 * }</pre>
 */
package org.machanism.machai.ai.manager;
