/**
 * Command-line tooling for scanning a workspace/module tree and preparing inputs for GenAI-assisted code review and
 * code generation workflows.
 *
 * <p>This package provides the primary CLI entry point ({@link org.machanism.machai.gw.Ghostwriter}) and the
 * orchestration components (for example, {@link org.machanism.machai.gw.FileProcessor}) responsible for:
 *
 * <ul>
 *   <li>discovering file-type-specific {@link org.machanism.machai.gw.reviewer.Reviewer} implementations (typically via
 *       {@link java.util.ServiceLoader});</li>
 *   <li>traversing one or more modules and selecting candidate files for processing;</li>
 *   <li>extracting and merging guidance with bundled templates and optional user instructions; and</li>
 *   <li>building prompt inputs which can be persisted for inspection or sent to a configured
 *       {@link org.machanism.machai.ai.manager.GenAIProvider}.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 *
 * <p>Typical usage is via the CLI:
 *
 * <pre>{@code
 * // java -jar gw.jar --dir /path/to/project --genai OpenAI:gpt-5.1
 * }</pre>
 *
 * <p>Programmatic usage is also supported:
 *
 * <pre>{@code
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