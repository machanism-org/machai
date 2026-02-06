/**
 * Facilities for discovering, describing, and processing a source-code project rooted at a filesystem directory.
 *
 * <p>This package centers on {@link org.machanism.machai.project.ProjectLayout}, a model of a project's on-disk
 * structure (for example Maven, JavaScript/Node, Python, or a default/fallback layout). A
 * {@link org.machanism.machai.project.ProjectLayoutManager} can inspect a candidate project root directory and
 * select an appropriate {@link org.machanism.machai.project.ProjectLayout} implementation.
 *
 * <p>Once a layout has been chosen, {@link org.machanism.machai.project.ProjectProcessor} can scan a project using
 * that layout and perform processing work over the discovered sources and resources.
 *
 * <h2>Typical usage</h2>
 *
 * <pre>{@code
 * File projectDir = new File("/path/to/project");
 *
 * // Detect the layout based on the project directory contents.
 * ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);
 *
 * // Process the project using a custom processor.
 * ProjectProcessor processor = ...;
 * processor.scanFolder(projectDir);
 * }</pre>
 *
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
