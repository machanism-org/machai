/**
 * Provides a no-op ("none") {@code GenAIProvider} implementation.
 *
 * <p>This package contains {@link org.machanism.machai.ai.provider.none.NoneProvider}, a stub implementation used when
 * generative AI integrations must be disabled (for example, due to security/compliance constraints, offline execution,
 * or when running tests without access to external services).
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>No network calls are performed and no external AI backend is contacted.</li>
 *   <li>{@code prompt(String)} appends prompt text to an in-memory buffer.</li>
 *   <li>{@code instructions(String)} stores instruction text; when logging is enabled, it is written to
 *       {@code instructions.txt} alongside the prompt log.</li>
 *   <li>{@code perform()} optionally writes the accumulated prompts to the configured log file, clears internal state,
 *       and returns {@code null}.</li>
 *   <li>Features that require a real provider (for example embeddings) are unsupported and may throw exceptions.</li>
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
 * -  Escape `<` and `>` as ` <` and `>` in `<pre>` content for Javadoc.
 */
