/**
 * Filesystem-backed APIs for inspecting on-disk project structures, detecting the
 * appropriate {@link org.machanism.machai.project.layout.ProjectLayout}, and applying
 * processing logic across a project root and its modules.
 *
 * <p>The primary responsibilities of this package are:
 *
 * <ul>
 *   <li>Detecting a suitable {@link org.machanism.machai.project.layout.ProjectLayout}
 *       implementation for a project directory via {@link org.machanism.machai.project.ProjectLayoutManager}.</li>
 *   <li>Providing a recursive, module-aware scanning workflow via
 *       {@link org.machanism.machai.project.ProjectProcessor}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>{@code
 * File projectDir = new File("/path/to/project");
 *
 * // Detect a layout (Maven / Node / Python / default).
 * ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);
 *
 * // Scan and process the project, recursing into modules if the layout provides them.
 * ProjectProcessor processor = ...;
 * processor.scanFolder(projectDir);
 * }</pre>
 *
 * @author machanism
 * @since 0.0.2
 */
package org.machanism.machai.project;

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
