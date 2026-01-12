/**
 * Provides core management functionality and extensible tools for AI providers in the Machanism AI client framework.
 * <p>
 * The <b>org.machanism.machai.ai.manager</b> package defines interfaces and manager classes for creating,
 * selecting, and extending <code>GenAIProvider</code> implementations. It includes utilities for prompt
 * handling, dynamic provider instantiation, file and command function augmentation, and streamlined resource
 * integration (including external files and secure command-line operations).
 * <p>
 * <b>Main Components:</b>
 * <ul>
 *   <li>{@link GenAIProvider} – Unified interface for submitting prompts, managing model state, handling files,
 *       computing embeddings, and runtime tool extension.</li>
 *   <li>{@link GenAIProviderManager} – Dynamic factory/manager offering provider resolution by model identifier
 *       string.</li>
 *   <li>{@link FileFunctionTools} – File system utilities for reading, writing, listing, and recursively
 *       enumerating files and directories.</li>
 *   <li>{@link CommandFunctionTools} – Secure shell command toolset guarded against dangerous operations,
 *       supporting diagnostics and OS compatibility.</li>
 *   <li>{@link SystemFunctionTools} – Convenience wrapper for unified file and command tool attachment.</li>
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
