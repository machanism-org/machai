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
 * <p>This package defines the {@link org.machanism.machai.ai.manager.GenAIProvider} contract and utilities to:
 * <ul>
 *   <li>resolve and instantiate providers from a {@code Provider:Model} identifier,</li>
 *   <li>configure providers from a {@link org.machanism.macha.core.commons.configurator.Configurator},</li>
 *   <li>optionally install host-executed tools (file system access and command execution) for providers that
 *       support tool calling.</li>
 * </ul>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li><strong>Provider contract</strong> – {@link org.machanism.machai.ai.manager.GenAIProvider} defines the API
 *       for prompting, file attachment, embeddings, and tool registration.</li>
 *   <li><strong>Provider resolution</strong> – {@link org.machanism.machai.ai.manager.GenAIProviderManager}
 *       instantiates an implementation by conventional short name (for example {@code OpenAI}) or a
 *       fully-qualified class name.</li>
 *   <li><strong>Tool installation</strong> – {@link org.machanism.machai.ai.manager.FileFunctionTools} and
 *       {@link org.machanism.machai.ai.manager.CommandFunctionTools} register host tools;
 *       {@link org.machanism.machai.ai.manager.SystemFunctionTools} installs both.</li>
 * </ul>
 *
 * <h2>Usage</h2>
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
 * <p><strong>Security note:</strong> tools execute on the hosting machine. Restrict allowed paths, commands,
 * working directories, and timeouts according to your security requirements.
 */
package org.machanism.machai.ai.manager;
