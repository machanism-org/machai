/**
 * Provides a non-operational ("none") generative AI provider implementation.
 * <p>
 * This package contains {@link org.machanism.machai.ai.none.NoneProvider}, a {@link org.machanism.machai.ai.manager.GenAIProvider}
 * implementation intended for environments where no external GenAI/LLM integration should be used. The provider can optionally
 * write the accumulated prompts (and instructions, if supplied) to local files for audit, troubleshooting, or deferred/manual
 * processing.
 * <p>
 * Behavior overview:
 * <ul>
 *   <li>No network calls are made and no external AI service is contacted.</li>
 *   <li>{@code prompt(..)} accumulates text; {@code perform()} optionally writes inputs to the configured log files and returns
 *   {@code null}.</li>
 *   <li>Operations that require real GenAI capability (for example, embeddings) are not supported and may throw exceptions.</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 * {@code
 * GenAIProvider provider = new NoneProvider();
 * provider.inputsLog(new File("./inputsLog/inputs.txt"));
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Describe the weather.");
 * provider.perform(); // Writes inputs locally; returns null.
 * }
 * </pre>
 */
package org.machanism.machai.ai.none;

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
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
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
