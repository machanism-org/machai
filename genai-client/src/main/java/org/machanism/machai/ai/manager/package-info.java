/**
 * Core management and extension tools for AI providers (GenAIProvider) within the Machanism AI client framework.
 * <p>
 * This package contains foundational interfaces and manager classes responsible for prompt handling,
 * dynamic provider selection/instantiation, function augmentation (file and command tools), and utilities for
 * integrating external resources and command-line operations.
 * <p>
 * <b>Main Components:</b>
 * <ul>
 *   <li>{@link GenAIProvider} &ndash; Interface for generic AI providers supporting flexible prompt submission, embeddings, file operations, and runtime extensibility.</li>
 *   <li>{@link GenAIProviderManager} &ndash; Factory and resolver for AI provider implementations by model identifier string.</li>
 *   <li>{@link FileFunctionTools} &ndash; Utilities for reading, writing, and listing files and directories on disk.</li>
 *   <li>{@link CommandFunctionTools} &ndash; Enables secure execution of allowed shell commands from supported OS platforms.</li>
 *   <li>{@link SystemFunctionTools} &ndash; Simplifies attachment of file and command tools for providers.</li>
 * </ul>
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * GenAIProvider provider = GenAIProviderManager.getProvider(&quot;OpenAI:gpt-3.5-turbo&quot;);
 * provider.prompt(&quot;Hello!&quot;);
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
