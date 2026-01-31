/**
 * Provides a no-op ("none") implementation of the generative-AI provider SPI.
 *
 * <p>This package contains {@link org.machanism.machai.ai.provider.none.NoneProvider}, an implementation of
 * {@link org.machanism.machai.ai.manager.GenAIProvider} intended for environments where no external LLM integration
 * should be used (for example, security/compliance restrictions, offline execution, or tests).
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>No network calls are performed and no external AI service is contacted.</li>
 *   <li>Prompt text passed to {@link org.machanism.machai.ai.manager.GenAIProvider#prompt(String)} is accumulated
 *       in memory.</li>
 *   <li>{@link org.machanism.machai.ai.manager.GenAIProvider#perform()} optionally writes the configured
 *       instructions and accumulated prompts to local files when
 *       {@link org.machanism.machai.ai.manager.GenAIProvider#inputsLog(java.io.File)} has been set; it then clears
 *       the internal buffer and returns {@code null}.</li>
 *   <li>Capabilities that require a real backend (for example, embeddings) are unsupported and will throw an
 *       exception when invoked.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * GenAIProvider provider = new NoneProvider();
 * provider.inputsLog(new File("./inputsLog/inputs.txt"));
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Describe the weather.");
 * provider.perform();
 * }</pre>
 */
package org.machanism.machai.ai.provider.none;

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