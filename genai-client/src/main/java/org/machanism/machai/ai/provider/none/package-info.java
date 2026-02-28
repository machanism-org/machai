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
 * No-op generative-AI provider.
 *
 * <p>This package contains {@link org.machanism.machai.ai.provider.none.NoneProvider}, an implementation of
 * {@link org.machanism.machai.ai.manager.GenAIProvider} intended for environments where calls to external model
 * backends are disabled, unavailable, or not permitted.
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.ai.provider.none.NoneProvider#prompt(String)} accumulates prompt text in memory.
 *       Prompts are separated by a blank line.</li>
 *   <li>{@link org.machanism.machai.ai.provider.none.NoneProvider#perform()} does not call a model. It may persist the
 *       accumulated prompt to an inputs log configured via
 *       {@link org.machanism.machai.ai.provider.none.NoneProvider#inputsLog(java.io.File)}.</li>
 *   <li>If instructions are configured via
 *       {@link org.machanism.machai.ai.provider.none.NoneProvider#instructions(String)}, they are written to an
 *       {@code instructions.txt} file alongside the inputs log (or in the process working directory when the log file
 *       has no parent directory).</li>
 *   <li>Model-dependent features (for example
 *       {@link org.machanism.machai.ai.provider.none.NoneProvider#embedding(String, long)}) are unsupported and throw
 *       {@link java.lang.UnsupportedOperationException}.</li>
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
