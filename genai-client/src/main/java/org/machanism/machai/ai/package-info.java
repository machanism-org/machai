/**
 * Provider-agnostic public API for the Machanism generative AI (GenAI) client.
 *
 * <p>This package contains provider-neutral abstractions intended to be used directly by application code.
 * Implementations for specific providers live under {@code org.machanism.machai.ai.provider}. Provider discovery,
 * selection, and lifecycle are coordinated by the manager APIs in {@code org.machanism.machai.ai.manager}.
 *
 * <h2>Typical flow</h2>
 * <ol>
 *   <li>Resolve a {@link org.machanism.machai.ai.manager.GenAIProvider} via
 *       {@link org.machanism.machai.ai.manager.GenAIProviderManager}.</li>
 *   <li>Configure the request by supplying instructions, prompts, tools, and any other provider inputs.</li>
 *   <li>Execute the request using {@link org.machanism.machai.ai.manager.GenAIProvider#perform()} and consume the
 *       returned result.</li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini");
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this text.");
 * String response = provider.perform();
 * }
 * </pre>
 */
package org.machanism.machai.ai;

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
 * 		- Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&amp;gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
