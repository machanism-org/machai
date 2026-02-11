/*-
 * @guidance:
 *
 * **IMPORTANT: UPDATE OR ADD JAVADOC FOR ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
 *
 * - Update Existing Javadoc and Add Missing Javadoc:
 *      - Review all classes in the folder.
 *      - Update any existing Javadoc to ensure it is accurate, comprehensive, and follows best practices.
 *      - Add Javadoc to any classes, methods, or fields where it is missing.
 *      - Ensure that all Javadoc is up-to-date and provides clear, meaningful documentation.
 * - Use Clear and Concise Descriptions:
 *      - Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 *      - Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * - Include Usage Examples Where Helpful:
 *      - Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * - Maintain Consistency and Formatting:
 *      - Follow a consistent style and structure for all Javadoc comments.
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *      - Review the Java class source code and include comprehensive Javadoc comments for all classes,
 *           methods, and fields, adhering to established best practices.
 *      - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *           and any exceptions thrown.
 *      - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;`
 *           and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc.
 * - Use the Java Version Defined in `pom.xml`:
 *      - All code improvements and Javadoc updates must be compatible with the Java version `maven.compiler.release` specified in the project's `pom.xml`.
 *      - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */
/**
 * Command-line orchestration and workspace scanning for Ghostwriter.
 *
 * <p>
 * This package provides the Ghostwriter CLI entry point ({@link org.machanism.machai.gw.Ghostwriter}) and the core
 * scanning/processing engine ({@link org.machanism.machai.gw.FileProcessor}). Together they traverse a project
 * workspace, locate supported files, extract embedded {@code @guidance:} blocks using file-type-specific
 * {@link org.machanism.machai.gw.reviewer.Reviewer} implementations, and submit per-file requests to the configured
 * GenAI provider.
 * </p>
 *
 * <h2>How scanning works</h2>
 * <ul>
 *   <li><strong>Project layout discovery</strong>: Uses
 *   {@link org.machanism.machai.project.layout.ProjectLayout} to identify modules and conventional source/test/doc
 *   directories.</li>
 *   <li><strong>File selection</strong>: Scans can be restricted using {@code glob:}/{@code regex:} matchers and an
 *   optional list of exclude patterns or exact relative paths.</li>
 *   <li><strong>Reviewer selection</strong>: {@link java.util.ServiceLoader} loads
 *   {@link org.machanism.machai.gw.reviewer.Reviewer} services. The processor selects a reviewer by file extension;
 *   the reviewer extracts guidance and builds a per-file prompt fragment.</li>
 *   <li><strong>Prompt execution</strong>: For each file, the processor composes a request that includes OS/project
 *   context, optional global instructions, per-file guidance, and a strict output format, then calls
 *   {@link org.machanism.machai.ai.manager.GenAIProvider#perform()}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>
 * {@code
 * // Scan a project directory
 * java -jar gw.jar C:\\projects\\my-project
 *
 * // Scan only Java sources using a glob pattern
 * java -jar gw.jar "glob:**\\*.java"
 * }
 * </pre>
 */
package org.machanism.machai.gw;
