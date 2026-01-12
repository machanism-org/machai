/**
 * Provides core management and extension tools for generic AI providers (GenAIProvider) 
 * within the Machanism AI client framework. 
 * <p>
 * This package contains primary interfaces, managers, and utility classes to support prompt handling,
 * dynamic provider selection, file system and command-line integration, and the augmentation of AI provider
 * functionality via system-level tools. The package is designed to enable flexible, extensible, and safe interaction
 * with various AI models, including tools for command execution and file management in supported environments.
 * <p>
 * <b>Main Components:</b>
 * <ul>
 *   <li>{@link GenAIProvider} &ndash; Core interface for all generic AI providers supporting text and file prompts, embeddings, and runtime extensibility.</li>
 *   <li>{@link GenAIProviderManager} &ndash; Resolves and instantiates provider implementations based on model identifiers.</li>
 *   <li>{@link FileFunctionTools} &ndash; Provides utility tools for reading, writing, and listing files and directories on disk.</li>
 *   <li>{@link CommandFunctionTools} &ndash; Enables safe execution of allowed shell commands from supported operating systems.</li>
 *   <li>{@link SystemFunctionTools} &ndash; Simplifies attachment of file and command tools for providers.</li>
 * </ul>
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * GenAIProvider provider = GenAIProviderManager.getProvider("OpenAI:gpt-3.5-turbo");
 * provider.prompt("Hello!");
 * SystemFunctionTools tools = new SystemFunctionTools();
 * tools.applyTools(provider);
 * </pre>
 */
package org.machanism.machai.ai.manager;

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
 *      - Generate comprehensive package-level Javadoc that clearly describes the packages overall purpose and usage.
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
