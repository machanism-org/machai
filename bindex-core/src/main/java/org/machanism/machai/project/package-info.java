/**
 * Filesystem-backed project layout detection and traversal.
 *
 * <p>This package provides APIs to:
 * <ul>
 *   <li><strong>Detect a project layout</strong> for a directory by inspecting recognizable build/configuration
 *       files and selecting an appropriate {@link org.machanism.machai.project.layout.ProjectLayout}.</li>
 *   <li><strong>Traverse a project root</strong> and, when supported by the detected layout, discover and scan
 *       modules/subprojects while delegating per-module handling to client code.</li>
 * </ul>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.ProjectLayoutManager} detects a
 *       {@link org.machanism.machai.project.layout.ProjectLayout} for a directory.</li>
 *   <li>{@link org.machanism.machai.project.ProjectProcessor} scans a project tree and invokes
 *       {@link org.machanism.machai.project.ProjectProcessor#processFolder(org.machanism.machai.project.layout.ProjectLayout)}
 *       for each discovered module (or for the root when no modules are present).</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * File projectDir = new File("/path/to/project");
 * ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(projectDir);
 *
 * ProjectProcessor processor = ...; // implement how each layout/module is handled
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
 * 		-  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * 		- Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 
 */
