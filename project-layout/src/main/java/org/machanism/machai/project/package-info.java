/**
 * Facilities for discovering, describing, and processing a source-code project rooted at a filesystem directory.
 *
 * <p>This package provides the core orchestration APIs for:
 *
 * <ul>
 *   <li>Detecting a project's on-disk convention (for example Maven, Gradle, JavaScript, or Python) and exposing it as a
 *       {@link org.machanism.machai.project.layout.ProjectLayout} via
 *       {@link org.machanism.machai.project.ProjectLayoutManager#detectProjectLayout(java.io.File)}.</li>
 *   <li>Scanning a project root, optionally recursing into nested modules, and delegating per-project processing logic
 *       to a {@link org.machanism.machai.project.ProjectProcessor} implementation.</li>
 * </ul>
 *
 * <p>The detected {@link org.machanism.machai.project.layout.ProjectLayout} is used to locate conventional directories
 * such as sources, tests, resources, and documentation roots.
 *
 * <h2>Typical workflow</h2>
 * <ol>
 *   <li>Detect a layout for a project directory using
 *       {@link org.machanism.machai.project.ProjectLayoutManager#detectProjectLayout(java.io.File)}.</li>
 *   <li>Provide a {@link org.machanism.machai.project.ProjectProcessor} that implements
 *       {@link org.machanism.machai.project.ProjectProcessor#processFolder(org.machanism.machai.project.layout.ProjectLayout)}.</li>
 *   <li>Invoke {@link org.machanism.machai.project.ProjectProcessor#scanFolder(java.io.File)} on the project root.
 *       If the layout reports modules, each module is scanned recursively; otherwise the root is processed directly.</li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * java.io.File projectDir = new java.io.File("C:\\path\\to\\project");
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
