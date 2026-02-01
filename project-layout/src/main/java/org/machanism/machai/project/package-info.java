/**
 * Project discovery, representation, and processing.
 *
 * <p>This package contains the core API for working with a source-code project rooted at a filesystem
 * directory. It focuses on:
 *
 * <ul>
 *   <li><b>Layout detection</b>: determining what kind of project a directory represents (for example,
 *       Maven, Node, Python) and selecting a matching {@code ProjectLayout}.</li>
 *   <li><b>Project modeling</b>: representing a discovered project and its important folders as typed
 *       structures.</li>
 *   <li><b>Scanning and processing</b>: traversing a project root and applying layout-aware processing
 *       rules.</li>
 * </ul>
 *
 * <h2>Typical flow</h2>
 *
 * <ol>
 *   <li>Detect the layout for a root directory.</li>
 *   <li>Create and run a processor to scan the directory tree.</li>
 *   <li>Consume the resulting {@code Project} structure and metadata.</li>
 * </ol>
 *
 * <h2>Example</h2>
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
