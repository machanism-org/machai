/**
 * Facilities for discovering, describing, and processing a source-code project rooted at a filesystem directory.
 *
 * <p>The primary responsibilities of this package are:
 *
 * <ul>
 *   <li>Detect a project’s on-disk conventions (its {@link org.machanism.machai.project.layout.ProjectLayout}).</li>
 *   <li>Traverse the detected source/resource folders and (optionally) recurse into modules.</li>
 *   <li>Provide an extensible processing hook via {@link org.machanism.machai.project.ProjectProcessor}.</li>
 * </ul>
 *
 * <h2>Typical workflow</h2>
 *
 * <ol>
 *   <li>Call {@link org.machanism.machai.project.ProjectLayoutManager#detectProjectLayout(java.io.File)} to select a
 *       {@link org.machanism.machai.project.layout.ProjectLayout} implementation based on marker files (for example,
 *       {@code pom.xml} for Maven or {@code package.json} for Node).</li>
 *   <li>Use a {@link org.machanism.machai.project.ProjectProcessor} implementation to scan the project root via
 *       {@link org.machanism.machai.project.ProjectProcessor#scanFolder(java.io.File)}. The processor will recurse into
 *       modules when the selected layout reports them.</li>
 *   <li>Implement {@link org.machanism.machai.project.ProjectProcessor#processFolder(org.machanism.machai.project.layout.ProjectLayout)}
 *       to perform the work for each scanned project/module.</li>
 * </ol>
 *
 * <h2>Example</h2>
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
 *          and `>` as `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
