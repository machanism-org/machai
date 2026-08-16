/**
 * Provides the project-inspection API used to detect a repository's layout and
 * process its folders and modules.
 *
 * <p>The package separates layout detection from layout processing:
 * {@link org.machanism.machai.project.ProjectLayoutManager} selects a
 * {@link org.machanism.machai.project.layout.ProjectLayout} implementation for
 * a project directory, while {@link org.machanism.machai.project.ProjectProcessor}
 * traverses the detected project and delegates folder handling to subclasses.
 * Layout implementations expose root-relative source, test, and documentation
 * locations and may also expose child module paths.</p>
 *
 * <h2>Layout detection</h2>
 * <p>{@code ProjectLayoutManager} checks supported project descriptors in the
 * following order: Maven ({@code pom.xml}), Gradle ({@code build.gradle}),
 * JavaScript or TypeScript ({@code package.json}), and Python
 * ({@code pyproject.toml}). If no specialized descriptor is recognized but the
 * directory exists, it uses a generic default layout. A missing project
 * directory results in {@link java.io.FileNotFoundException}.</p>
 *
 * <h2>Processing model</h2>
 * <p>A processor first obtains the layout for the supplied root directory. If
 * the layout reports module paths, each module is scanned recursively;
 * otherwise the processor handles the current layout directly through
 * {@link org.machanism.machai.project.ProjectProcessor#processFolder(org.machanism.machai.project.layout.ProjectLayout)}.
 * Implementations should therefore make their folder-processing operation
 * safe to invoke once for every discovered project.</p>
 *
 * <h2>Typical usage</h2>
 * <pre><code>
 * java.io.File projectDir = new java.io.File("path/to/project");
 * org.machanism.machai.project.ProjectLayoutManager
 *     .detectProjectLayout(projectDir);
 *
 * org.machanism.machai.project.ProjectProcessor processor = ...;
 * processor.scanFolder(projectDir);
 * </code></pre>
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
