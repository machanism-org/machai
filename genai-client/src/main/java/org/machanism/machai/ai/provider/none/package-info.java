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
 * Provides a no-op {@link org.machanism.machai.ai.provider.Genai} implementation for offline, disabled, or test-only
 * execution paths.
 *
 * <p>This package contains {@link org.machanism.machai.ai.provider.none.NoneProvider}, which satisfies the
 * {@link org.machanism.machai.ai.provider.Genai} contract without invoking any external model backend. It is useful
 * when AI integration must be turned off while still preserving the surrounding application flow and prompt assembly
 * behavior.
 *
 * <p>The provider collects prompt content in memory, optionally persists configured instructions and prompt input to
 * local files during execution, and reports zero usage because no remote or local model inference is performed.
 *
 * <h2>Supported behavior</h2>
 * <ul>
 *   <li>Accumulates prompt text passed through
 *       {@link org.machanism.machai.ai.provider.none.NoneProvider#prompt(String)}.</li>
 *   <li>Optionally writes the prompt buffer to a configured log file through
 *       {@link org.machanism.machai.ai.provider.none.NoneProvider#inputsLog(java.io.File)} when
 *       {@link org.machanism.machai.ai.provider.none.NoneProvider#perform()} is invoked.</li>
 *   <li>Optionally writes configured instructions to a sibling {@code instructions.txt} file.</li>
 *   <li>Returns {@code null} from execution and zero-valued usage metrics.</li>
 * </ul>
 *
 * <h2>Unsupported behavior</h2>
 * <ul>
 *   <li>Embedding generation via
 *       {@link org.machanism.machai.ai.provider.none.NoneProvider#embedding(String, long)}.</li>
 *   <li>Tool execution and working-directory configuration beyond interface compatibility no-ops.</li>
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
