/**
 * Provides APIs for detecting a project's layout and traversing its modules or root directory for processing.
 *
 * <p>This package contains the entry points used to inspect a project directory, classify it by a supported
 * build or ecosystem convention, and delegate work to a processor implementation.
 * The resulting layout abstraction describes conventional locations such as source, test, resource,
 * and documentation directories.
 *
 * <p>Key responsibilities in this package include:
 *
 * <ul>
 *   <li>Detecting an appropriate {@link org.machanism.machai.project.layout.ProjectLayout} for a filesystem directory.</li>
 *   <li>Recursively scanning project roots and nested modules when the detected layout exposes module definitions.</li>
 *   <li>Providing a base processor abstraction that delegates concrete folder handling to subclasses.</li>
 * </ul>
 *
 * <p>Typical usage starts by calling
 * {@link org.machanism.machai.project.ProjectLayoutManager#detectProjectLayout(java.io.File)}
 * for a project root and then invoking
 * {@link org.machanism.machai.project.ProjectProcessor#scanFolder(java.io.File)}
 * on a {@link org.machanism.machai.project.ProjectProcessor} implementation.
 *
 * <h2>Example</h2>
 * <pre>
 * java.io.File projectDir = new java.io.File("C:\\path\\to\\project");
 * org.machanism.machai.project.ProjectProcessor processor = ...;
 * processor.scanFolder(projectDir);
 * </pre>
 *
 * @since 0.0.2
 */
package org.machanism.machai.project;

/*-
 * @guidance:
 *
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
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
