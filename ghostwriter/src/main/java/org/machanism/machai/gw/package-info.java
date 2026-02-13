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
 * This package provides the command-line entry point ({@link org.machanism.machai.gw.Ghostwriter}) and the scanning
 * engine ({@link org.machanism.machai.gw.FileProcessor}) that walks a project directory and submits per-file review
 * requests to a configured {@link org.machanism.machai.ai.manager.GenAIProvider}.
 * </p>
 *
 * <h2>Processing model</h2>
 * <ul>
 *   <li><strong>Layout discovery</strong>: Uses {@link org.machanism.machai.project.layout.ProjectLayout} to determine
 *   conventional source/test/docs folders and (optionally) child modules.</li>
 *   <li><strong>Child-first traversal</strong>: When modules are present, module folders are processed before files in
 *   the parent project directory. Dependency resolution between modules is not performed.</li>
 *   <li><strong>Filtering</strong>: Scans may be limited using {@code glob:} / {@code regex:} matchers and an optional
 *   exclude list.</li>
 *   <li><strong>Reviewer selection</strong>: {@link java.util.ServiceLoader} loads
 *   {@link org.machanism.machai.gw.reviewer.Reviewer} services. The processor selects a reviewer by file extension;
 *   the reviewer extracts any embedded {@code @guidance:} blocks and produces a prompt fragment.</li>
 * </ul>
 */
package org.machanism.machai.gw;
