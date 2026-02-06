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
 *          and `&gt;` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */

/**
 * No-op ("none") implementation of the generative AI provider SPI.
 *
 * <p>This package provides {@link org.machanism.machai.ai.provider.none.NoneProvider}, an implementation of
 * {@link org.machanism.machai.ai.manager.GenAIProvider} intended for environments where external LLM integration
 * must not be used (for example, offline execution, security/compliance constraints, or tests).
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>No network calls are performed and no external AI service is contacted.</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProvider#prompt(String)} accumulates text in memory.</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProvider#perform()} optionally writes:
 *     <ul>
 *       <li>{@code instructions.txt} (when instructions are set), and</li>
 *       <li>the accumulated prompts to the configured inputs log file (when
 *           {@link org.machanism.machai.ai.manager.GenAIProvider#inputsLog(java.io.File)} is set).</li>
 *     </ul>
 *     It then clears the internal buffer and returns {@code null}.</li>
 *   <li>Capabilities requiring a real backend (for example,
 *       {@link org.machanism.machai.ai.manager.GenAIProvider#embedding(String)}) are unsupported and throw
 *       {@link java.lang.UnsupportedOperationException}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * GenAIProvider provider = new NoneProvider();
 * provider.inputsLog(new File("./inputsLog/inputs.txt"));
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Describe the weather.");
 * provider.perform();
 * }</pre>
 */
package org.machanism.machai.ai.provider.none;
