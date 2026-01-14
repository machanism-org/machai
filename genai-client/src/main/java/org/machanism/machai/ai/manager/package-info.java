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
 * Management layer for resolving and extending {@code GenAIProvider} implementations.
 * <p>
 * This package contains the primary abstractions used by the client to obtain a concrete provider
 * (for example, OpenAI- or Ollama-backed implementations) and to attach common runtime "tools"
 * that enable providers to interact with the local system in a controlled way.
 * <p>
 * Typical responsibilities include:
 * <ul>
 *   <li>Resolving a provider implementation from a model identifier string via
 *       {@link org.machanism.machai.ai.manager.GenAIProviderManager}.</li>
 *   <li>Providing a stable, provider-agnostic interface for prompts and other AI operations via
 *       {@link org.machanism.machai.ai.manager.GenAIProvider}.</li>
 *   <li>Attaching file and command tools via {@link org.machanism.machai.ai.manager.FileFunctionTools},
 *       {@link org.machanism.machai.ai.manager.CommandFunctionTools}, and
 *       {@link org.machanism.machai.ai.manager.SystemFunctionTools}.</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-3.5-turbo");
 * provider.prompt("Hello!");
 *
 * SystemFunctionTools tools = new SystemFunctionTools();
 * tools.applyTools(provider);
 * </pre>
 */
package org.machanism.machai.ai.manager;
