/**
 * Project discovery and processing entry points.
 *
 * <p>This package contains the public API and core types used to:
 *
 * <ul>
 *   <li>detect a project's layout from a root directory,</li>
 *   <li>represent the discovered project and its folders/modules, and</li>
 *   <li>scan/process the project using layout-specific behavior.</li>
 * </ul>
 *
 * <h2>Key concepts</h2>
 *
 * <ul>
 *   <li><b>Layout detection</b>: {@link org.machanism.machai.project.ProjectLayoutManager} inspects a
 *       root directory and selects an appropriate {@code ProjectLayout} (for example Maven, Node,
 *       Python), with a default/fallback when no specific layout matches.</li>
 *   <li><b>Project representation</b>: types such as {@link org.machanism.machai.project.Project} and
 *       {@link org.machanism.machai.project.ProjectFolder} model the discovered structure and provide
 *       access to relevant paths and metadata.</li>
 *   <li><b>Processing/scanning</b>: {@link org.machanism.machai.project.ProjectProcessor}
 *       implementations traverse a project root and delegate to layout-specific handling.</li>
 *   <li><b>Layout implementations</b>: concrete layouts reside in
 *       {@code org.machanism.machai.project.layout}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>{@code
 * File projectDir = new File("/path/to/project");
 *
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
