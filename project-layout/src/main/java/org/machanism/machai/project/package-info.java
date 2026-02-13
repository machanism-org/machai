/**
 * Facilities for discovering, describing, and processing a source-code project rooted at a filesystem directory.
 *
 * <p>This package is responsible for:
 * <ul>
 *   <li>Determining a project's on-disk structure (its <em>layout</em>)</li>
 *   <li>Configuring a {@link org.machanism.machai.project.layout.ProjectLayout} for a given project root</li>
 *   <li>Scanning project folders (optionally including modules) and delegating folder-level processing</li>
 * </ul>
 *
 * <p>At a high level, {@link org.machanism.machai.project.ProjectLayoutManager} inspects a candidate project root and
 * selects an appropriate {@link org.machanism.machai.project.layout.ProjectLayout} implementation.
 * {@link org.machanism.machai.project.ProjectProcessor} then uses that layout to scan the project root, optionally
 * iterate over modules when present, and delegate processing for each discovered folder.
 *
 * <h2>Typical usage</h2>
 *
 * <pre>{@code
 * File projectDir = new File("C:\\path\\to\\project");
 * ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);
 *
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
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
