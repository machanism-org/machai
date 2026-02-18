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
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 *      - Do not use escaping in `{@code ...}` tags.    
 */

/**
 * No-op ("none") generative AI provider implementation.
 *
 * <p>This package provides {@link org.machanism.machai.ai.provider.none.NoneProvider}, an implementation of
 * {@link org.machanism.machai.ai.manager.GenAIProvider} that intentionally avoids any external LLM/backend calls.
 * It is useful for offline execution, security/compliance constrained deployments, or deterministic testing where
 * prompts should be captured locally.
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>{@code prompt(...)} accumulates prompt text in memory.</li>
 *   <li>{@code perform()} optionally writes {@code instructions.txt} and the prompt log to the configured
 *       {@link java.io.File} (via {@code inputsLog(...)}), clears the internal buffer, and returns {@code null}.</li>
 *   <li>Capabilities that require a real backend (for example {@code embedding(...)}) are unsupported and throw
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
