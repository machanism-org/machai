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
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */

/**
 * Provider resolution and tool-function wiring for GenAI integrations.
 *
 * <p>This package defines:
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProvider}, the core abstraction for chat-model providers
 *       that can accept prompts/instructions, attach files, compute embeddings, register tool functions, and
 *       execute a request via {@link org.machanism.machai.ai.manager.GenAIProvider#perform()}.</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProviderManager}, a reflection-based factory that resolves
 *       and instantiates a provider from an identifier typically formatted as {@code Provider:Model}, then
 *       applies the selected model.</li>
 * </ul>
 *
 * <p>The package also provides opt-in tool installers that register host-controlled local capabilities with a
 * provider via
 * {@link org.machanism.machai.ai.manager.GenAIProvider#addTool(String, String, java.util.function.Function, String...)}:
 * <ul>
 *   <li>{@link org.machanism.machai.ai.manager.FileFunctionTools} for reading, writing, and listing files
 *       relative to the provider working directory</li>
 *   <li>{@link org.machanism.machai.ai.manager.CommandFunctionTools} for executing shell commands from the
 *       working directory</li>
 *   <li>{@link org.machanism.machai.ai.manager.SystemFunctionTools} as a convenience wrapper that installs both
 *       file and command tools</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-4o-mini");
 * new SystemFunctionTools().applyTools(provider);
 *
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Summarize this project.");
 *
 * String response = provider.perform();
 * }</pre>
 */
package org.machanism.machai.ai.manager;
