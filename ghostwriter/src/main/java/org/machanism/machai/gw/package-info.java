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
 * - Use the Java Version Defined in `pom.xml`:
 *     - All code improvements and Javadoc updates must be compatible with the Java version specified in the project's `pom.xml`.
 *     - Do not use features or syntax that require a higher Java version than defined in `pom.xml`.
 */
/**
 * Command-line tooling for scanning a project workspace and preparing prompt inputs for GenAI-assisted code review and
 * code generation workflows.
 *
 * <p>
 * The {@link org.machanism.machai.gw.Ghostwriter} CLI scans one or more directories (optionally constrained by glob or
 * regular-expression patterns) within a project root. {@link org.machanism.machai.gw.FileProcessor} traverses the
 * project layout (including multi-module builds), discovers file-type-specific
 * {@link org.machanism.machai.gw.reviewer.Reviewer} implementations via {@link java.util.ServiceLoader}, extracts
 * per-file {@code @guidance:} blocks, and orchestrates prompt assembly and invocation of the configured GenAI provider.
 * </p>
 *
 * <p>
 * Primary responsibilities include:
 * </p>
 *
 * <ul>
 *   <li>Discovering and selecting reviewers based on file extension.</li>
 *   <li>Traversing a project layout (sources/tests/docs/modules) and applying excludes and patterns.</li>
 *   <li>Extracting guidance blocks and combining them with bundled prompt templates and optional user instructions.</li>
 *   <li>Optionally persisting prompt inputs to a temporary directory for inspection and debugging.</li>
 * </ul>
 */
package org.machanism.machai.gw;
