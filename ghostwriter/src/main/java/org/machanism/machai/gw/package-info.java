/**
 * Ghostwriter command-line tooling and orchestration for scanning a workspace and preparing AI-ready
 * prompt inputs.
 *
 * <p>This package provides the CLI entry point and a processing pipeline that typically:
 * <ul>
 *   <li>Walks a project directory (and optionally its modules) while honoring layout exclusions.</li>
 *   <li>Delegates file-type-specific extraction to {@code Reviewer} implementations.</li>
 *   <li>Aggregates extracted {@code @guidance:} directives from supported files and produces prompt input
 *       artifacts.</li>
 *   <li>Invokes a configured {@link org.machanism.machai.ai.manager.GenAIProvider} to generate or review
 *       documentation content.</li>
 * </ul>
 *
 * <p>Common entry points:
 * <ul>
 *   <li>{@link org.machanism.machai.gw.Ghostwriter} &mdash; CLI entry point.</li>
 *   <li>{@link org.machanism.machai.gw.FileProcessor} &mdash; scanning/orchestration engine.</li>
 * </ul>
 *
 * <p>Example:
 * <pre>{@code
 * // From the command line.
 * // java -jar gw.jar --dir /path/to/project --genai OpenAI:gpt-5.1
 *
 * // Programmatic usage.
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
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND THIS `package-info.java`!**
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * 
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the packages overall purpose and usage.
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
