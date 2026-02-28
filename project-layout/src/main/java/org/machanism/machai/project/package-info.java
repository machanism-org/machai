/**
 * Facilities for discovering, describing, and processing a source-code project rooted at a filesystem directory.
 *
 * <p>This package provides the core APIs used to:
 *
 * <ul>
 *   <li>Detect a project's on-disk convention (for example, Maven or Gradle) and expose it as a
 *       {@link org.machanism.machai.project.layout.ProjectLayout}.</li>
 *   <li>Traverse a project root (and any nested modules) and invoke a per-project processing hook via
 *       {@link org.machanism.machai.project.ProjectProcessor}.</li>
 * </ul>
 *
 * <p>In typical usage, a caller detects the {@link org.machanism.machai.project.layout.ProjectLayout} for a project
 * directory using {@link org.machanism.machai.project.ProjectLayoutManager#detectProjectLayout(java.io.File)} and then
 * runs a {@link org.machanism.machai.project.ProjectProcessor} over the project root.
 *
 * <h2>Typical workflow</h2>
 *
 * <ol>
 *   <li>Detect a layout for a project directory using
 *       {@link org.machanism.machai.project.ProjectLayoutManager#detectProjectLayout(java.io.File)}.</li>
 *   <li>Provide a {@link org.machanism.machai.project.ProjectProcessor} implementation (or use a provided one).</li>
 *   <li>Invoke {@link org.machanism.machai.project.ProjectProcessor#scanFolder(java.io.File)} on the project root.
 *       The processor will recurse into modules when the selected layout reports them.</li>
 * </ol>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * File projectDir = new File("C:\\path\\to\\project");
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
 *          and `>` as `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
