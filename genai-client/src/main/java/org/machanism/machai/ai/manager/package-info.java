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
 * Provider discovery, configuration, and host-side tool wiring for generative AI integrations.
 *
 * <p>This package defines the {@link org.machanism.machai.ai.manager.GenAIProvider} service-provider interface (SPI)
 * and supporting utilities used to resolve, configure, and interact with concrete provider implementations.
 *
 * <h2>Key responsibilities</h2>
 * <ul>
 *   <li><strong>Provider resolution</strong> via {@link org.machanism.machai.ai.manager.GenAIProviderManager}, which
 *   creates provider instances using a {@code Provider:Model} identifier (or a fully qualified provider class name)
 *   and applies the selected model.</li>
 *   <li><strong>Session interaction contract</strong> via {@link org.machanism.machai.ai.manager.GenAIProvider}, which
 *   supports prompts, instructions, file attachments, embeddings, and tool registration.</li>
 *   <li><strong>Host tool installation</strong> via {@link org.machanism.machai.ai.manager.FileFunctionTools},
 *   {@link org.machanism.machai.ai.manager.CommandFunctionTools}, and the convenience wrapper
 *   {@link org.machanism.machai.ai.manager.SystemFunctionTools}.</li>
 *   <li><strong>Operational metrics</strong> via {@link org.machanism.machai.ai.manager.Usage} aggregated by
 *   {@link org.machanism.machai.ai.manager.GenAIProviderManager}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Configurator conf = ...;
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini", conf);
 *
 * // Optionally expose host tools to the provider.
 * new SystemFunctionTools().applyTools(provider);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this project.");
 * String response = provider.perform();
 * }</pre>
 *
 * <p><strong>Security note:</strong> tool functions execute on the hosting machine. Expose them only in trusted
 * environments and enforce appropriate allow/deny policies for paths and commands.
 */
package org.machanism.machai.ai.manager;
