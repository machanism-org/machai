/*-
 * @guidance:
 *
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
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
 * Provides a no-operation {@link org.machanism.machai.ai.provider.Genai} implementation for disabled,
 * offline, and test-oriented execution paths.
 *
 * <p>This package contains {@link org.machanism.machai.ai.provider.none.NoneProvider}, a provider that
 * fulfills the {@link org.machanism.machai.ai.provider.Genai} contract without invoking any local or
 * remote generative AI backend. It is intended for scenarios where application code must continue to
 * build prompts, configure instructions, and interact with the provider API while intentionally
 * suppressing model execution.
 *
 * <p>The provider stores prompt content in memory, optionally persists instructions and collected prompt
 * input to local files during {@link org.machanism.machai.ai.provider.none.NoneProvider#perform()}, and
 * reports zero-valued usage metrics because no inference is performed.
 *
 * <h2>Primary responsibilities</h2>
 * <ul>
 *   <li>Collect prompt text appended through
 *       {@link org.machanism.machai.ai.provider.none.NoneProvider#prompt(String)}.</li>
 *   <li>Optionally write configured instructions to an {@code instructions.txt} file in the log target
 *       directory.</li>
 *   <li>Optionally write accumulated prompt content to the configured inputs log file when execution is
 *       requested.</li>
 *   <li>Provide interface-compatible no-op implementations for unsupported runtime behaviors such as tool
 *       registration and working-directory configuration.</li>
 * </ul>
 *
 * <h2>Limitations</h2>
 * <ul>
 *   <li>No model response is generated; {@link org.machanism.machai.ai.provider.none.NoneProvider#perform()}
 *       returns {@code null}.</li>
 *   <li>Embedding generation is not supported and results in an
 *       {@link java.lang.UnsupportedOperationException}.</li>
 *   <li>Usage values remain zero because no provider-side processing occurs.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Genai provider = new NoneProvider();
 * provider.inputsLog(new File("./inputsLog/inputs.txt"));
 * provider.instructions("You are a helpful assistant.");
 * provider.prompt("Describe the weather.");
 * provider.perform();
 * }</pre>
 */
package org.machanism.machai.ai.provider.none;
