/**
 * Provides APIs for detecting a filesystem-backed project layout and for traversing a project
 * directory (including multi-module projects).
 * <p>
 * The main entry points are:
 * <ul>
 *   <li>{@link org.machanism.machai.project.ProjectLayoutManager} for selecting an appropriate
 *       {@link org.machanism.machai.project.layout.ProjectLayout} implementation based on the
 *       files present in a root directory.</li>
 *   <li>{@link org.machanism.machai.project.ProjectProcessor} for recursively scanning a project
 *       (and its modules, if any) and delegating the actual handling of a detected
 *       {@link org.machanism.machai.project.layout.ProjectLayout} to subclasses.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * File projectDir = new File("/path/to/project");
 *
 * ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);
 *
 * ProjectProcessor processor = ...; // your processor implementation
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
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND THIS `package-info.java`!**
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * 
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 *      
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * 
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * 
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */
