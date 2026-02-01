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
 * Provider resolution, interaction, and host-side tool wiring for generative AI integrations.
 *
 * <p>This package contains the core abstractions and utilities used to integrate one or more generative AI
 * backends into the host application:
 *
 * <ul>
 *   <li><strong>Provider contract</strong> – {@link org.machanism.machai.ai.manager.GenAIProvider} defines the
 *       common operations for prompting, attaching files, computing embeddings, and registering callable tools.</li>
 *   <li><strong>Provider resolution</strong> – {@link org.machanism.machai.ai.manager.GenAIProviderManager}
 *       resolves a {@code Provider:Model} identifier to an implementation and configures the chosen model.</li>
 *   <li><strong>Tool installation</strong> – {@link org.machanism.machai.ai.manager.FileFunctionTools} and
 *       {@link org.machanism.machai.ai.manager.CommandFunctionTools} register host-executed tools; 
 *       {@link org.machanism.machai.ai.manager.SystemFunctionTools} is a convenience installer for both.</li>
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
