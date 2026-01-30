/**
 * Command-line tooling and orchestration for scanning a workspace and preparing GenAI-ready prompt inputs.
 *
 * <p>This package provides the {@link org.machanism.machai.gw.Ghostwriter} CLI and the
 * {@link org.machanism.machai.gw.FileProcessor} pipeline used to:
 *
 * <ul>
 *   <li>Detect project/module layouts and traverse directories while honoring common exclusions.</li>
 *   <li>Delegate file-type-specific guidance extraction to {@link org.machanism.machai.gw.reviewer.Reviewer}
 *       implementations discovered via {@link java.util.ServiceLoader}.</li>
 *   <li>Assemble prompt inputs from bundled templates and optional user instructions.</li>
 *   <li>Invoke a configured {@link org.machanism.machai.ai.manager.GenAIProvider} to perform the requested
 *       processing and optionally log the generated prompt inputs under a temporary folder.</li>
 * </ul>
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