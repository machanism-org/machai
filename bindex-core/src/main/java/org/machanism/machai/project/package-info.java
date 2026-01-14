/**
 * Provides APIs for detecting a filesystem-backed project layout and for traversing a project directory,
 * including simple multi-module/workspace projects.
 *
 * <p>This package centers around two responsibilities:
 * <ul>
 *   <li><strong>Layout detection</strong>: selecting a {@link org.machanism.machai.project.layout.ProjectLayout}
 *       implementation based on the presence of well-known build/configuration files in a project root.</li>
 *   <li><strong>Project traversal</strong>: scanning a project root and, when applicable, recursively scanning
 *       detected modules while delegating project-specific handling to client code.</li>
 * </ul>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.ProjectLayoutManager} detects a
 *       {@link org.machanism.machai.project.layout.ProjectLayout} for a given directory.</li>
 *   <li>{@link org.machanism.machai.project.ProjectProcessor} provides a template method for scanning a project
 *       and invoking {@link org.machanism.machai.project.ProjectProcessor#processFolder(org.machanism.machai.project.layout.ProjectLayout)}
 *       for each discovered module (or for the root when there are no modules).</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * File projectDir = new File("/path/to/project");
 *
 * // Detect the layout (Maven, JS workspaces, Python, or a default fallback).
 * ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);
 *
 * // Traverse the project; subclasses implement how to handle each detected layout.
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
