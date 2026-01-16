/**
 * Provides a non-operational ("none") generative-AI provider implementation.
 *
 * <p>This package contains {@link org.machanism.machai.ai.provider.none.NoneProvider}, a
 * {@link org.machanism.machai.ai.manager.GenAIProvider} implementation intended for environments where no external
 * generative AI / LLM integration should be used.
 *
 * <p>Behavior summary:
 * <ul>
 *   <li>No network calls are made and no external AI service is contacted.</li>
 *   <li>{@code prompt(String)} accumulates prompt text in memory.</li>
 *   <li>{@code perform()} optionally writes instructions and prompts to local files when {@code inputsLog(File)} is
 *       configured, then returns {@code null}.</li>
 *   <li>Provider capabilities that require a real backend (for example, embeddings) are unsupported.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>
 * {@code
 * GenAIProvider provider = new NoneProvider();
 * provider.inputsLog(new File("./inputsLog/inputs.txt"));
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Describe the weather.");
 * provider.perform();
 * }
 * </pre>
 */
package org.machanism.machai.ai.provider.none;

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
 *		- Use proper Markdown or HTML formatting for readability.
 *
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes,
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */
