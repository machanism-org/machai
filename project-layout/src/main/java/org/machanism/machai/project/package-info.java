/**
 * Coordinates project-layout detection and recursive project processing.
 *
 * <p>{@link org.machanism.machai.project.ProjectLayoutManager} identifies a
 * project from its build or package descriptor and configures the corresponding
 * {@link org.machanism.machai.project.layout.ProjectLayout}. The supported
 * descriptors are checked in this order: Maven ({@code pom.xml}), Gradle
 * ({@code build.gradle}), JavaScript or TypeScript ({@code package.json}), and
 * Python ({@code pyproject.toml}). Existing directories without a recognized
 * descriptor use {@code DefaultProjectLayout}; a missing directory causes
 * {@link java.io.FileNotFoundException}.</p>
 *
 * <p>{@link org.machanism.machai.project.ProjectProcessor} uses the selected
 * layout to process either the current project or each reported child module.
 * Subclasses implement
 * {@link org.machanism.machai.project.ProjectProcessor#processFolder(org.machanism.machai.project.layout.ProjectLayout)}
 * and can use the layout's root-relative source, test, and documentation paths
 * to perform repository-specific work. A layout may return {@code null} for
 * modules when it is not a parent project.</p>
 *
 * <h2>Typical usage</h2>
 * <pre><code>
 * java.io.File projectDir = new java.io.File("path/to/project");
 * org.machanism.machai.project.ProjectProcessor processor = ...;
 * processor.scanFolder(projectDir);
 * </code></pre>
 *
 * <p>Implementations should make folder processing safe to invoke once for
 * every discovered project or module. See the
 * {@link org.machanism.machai.project.layout} package for the concrete layout
 * implementations and their metadata-specific behavior.</p>
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
