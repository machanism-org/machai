/**
 * Filesystem-backed APIs for inspecting and processing on-disk project structures.
 *
 * <p>This package provides a coordination layer for:
 * <ul>
 *   <li>Detecting a suitable {@link org.machanism.machai.project.layout.ProjectLayout} for a given
 *       project directory via {@link org.machanism.machai.project.ProjectLayoutManager}.</li>
 *   <li>Recursively scanning a project (and any discovered modules) and delegating layout-specific
 *       behavior to implementations of {@link org.machanism.machai.project.ProjectProcessor}.</li>
 * </ul>
 *
 * <p>Detection supports multiple layout families (for example Maven, JavaScript/Node, and Python)
 * and falls back to a default layout when the directory exists but no specific layout is detected.
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
