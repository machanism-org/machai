/**
 * Command-line tooling and orchestration for scanning a workspace and preparing GenAI-ready prompt inputs.
 *
 * <p>This package provides the Ghostwriter CLI entry point and the scanning/processing pipeline.
 * Implementations in this package can:
 *
 * <ul>
 *   <li>Traverse a project directory (and optionally its modules) while honoring configured exclusions.</li>
 *   <li>Apply file-type-specific reviewers/processors to extract directives and content.</li>
 *   <li>Aggregate extracted data and generate prompt-input artifacts for downstream processing.</li>
 *   <li>Invoke a configured {@link org.machanism.machai.ai.manager.GenAIProvider} to generate or review output.</li>
 * </ul>
 *
 * <p>Common entry points:
 *
 * <ul>
 *   <li>{@link org.machanism.machai.gw.Ghostwriter} – CLI entry point.</li>
 *   <li>{@link org.machanism.machai.gw.FileProcessor} – scanning/orchestration engine.</li>
 * </ul>
 *
 * <p>Example:
 *
 * <pre>{@code
 * // From the command line:
 * // java -jar gw.jar --dir /path/to/project --genai OpenAI:gpt-5.1
 *
 * // Programmatic usage:
 * FileProcessor processor = new FileProcessor("OpenAI:gpt-5.1");
 * processor.setModuleMultiThread(true);
 * processor.scanDocuments(new File("/path/to/project"));
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
package org.machanism.machai.gw;

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
 *          and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */