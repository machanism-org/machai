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
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, convert the code content to use the Javadoc `{@code ...}`inline tag instead. Ensure that the code is properly escaped and formatted for Javadoc. Only replace the code inside `<pre>` tags with `{@code ...}`; do not alter other content. `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */

/**
 * Provider selection, instantiation, and optional system tool integration for MachAI GenAI clients.
 *
 * <p>
 * The central entry point is {@link org.machanism.machai.ai.manager.GenAIProviderManager}, which resolves a
 * concrete {@link org.machanism.machai.ai.manager.GenAIProvider} implementation from a string identifier.
 * Identifiers are typically of the form {@code Provider:Model} (for example {@code OpenAI:gpt-4o-mini}).
 * If the provider portion is omitted, the manager falls back to a default provider.
 *
 * <p>
 * This package also provides optional, opt-in integration points for exposing controlled access to local system
 * capabilities as provider "tools" (for example, filesystem access or command execution) via:
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.FileFunctionTools} for reading, writing, and listing filesystem content</li>
 *   <li>{@link org.machanism.machai.ai.manager.CommandFunctionTools} for executing shell commands from a working directory</li>
 *   <li>{@link org.machanism.machai.ai.manager.SystemFunctionTools} as a convenience wrapper that installs both tool sets</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini");
 * provider.instructions("Be concise.");
 * provider.prompt("Explain the CAP theorem in one paragraph.");
 *
 * // Optionally expose additional tools to the provider
 * SystemFunctionTools tools = new SystemFunctionTools();
 * tools.applyTools(provider);
 *
 * String response = provider.perform();
 * provider.close();
 * }
 * </pre>
 */
package org.machanism.machai.ai.manager;
