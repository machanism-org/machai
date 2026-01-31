/**
 * Filesystem-backed APIs for inspecting a project directory, detecting an appropriate
 * {@link org.machanism.machai.project.layout.ProjectLayout}, and traversing the project structure.
 *
 * <p>This package provides types used by tooling that needs to understand how a repository is laid out on disk,
 * such as locating source/resource roots, determining whether the project is multi-module, and walking the
 * directory tree to process files.
 *
 * <p>The primary entry points are:
 *
 * <ul>
 *   <li>{@link org.machanism.machai.project.ProjectLayoutManager} for layout detection and related helpers.</li>
 *   <li>{@link org.machanism.machai.project.ProjectProcessor} for scanning a root folder and processing files,
 *       optionally recursing into modules/subprojects when supported by the detected layout.</li>
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * File projectDir = new File("/path/to/project");
 * ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);
 *
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
