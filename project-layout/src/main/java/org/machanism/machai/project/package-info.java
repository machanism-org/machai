/**
 * Coordinates project-layout detection and recursive processing of project
 * modules.
 *
 * <p>The package has two entry points. {@link ProjectLayoutManager} examines a
 * directory and returns a configured
 * {@link org.machanism.machai.project.layout.ProjectLayout}. Detection is
 * deterministic: Maven ({@code pom.xml}) takes precedence over Gradle
 * ({@code build.gradle}), JavaScript or TypeScript ({@code package.json}), and
 * Python ({@code pyproject.toml}). An existing directory without a recognized
 * descriptor uses {@link
 * org.machanism.machai.project.layout.DefaultProjectLayout}; a missing
 * directory results in {@link java.io.FileNotFoundException}.</p>
 *
 * <p>{@link ProjectProcessor} supplies the traversal workflow. It obtains a
 * layout for the root directory, recursively scans each module returned by the
 * layout, and calls the subclass's
 * {@link ProjectProcessor#processFolder(org.machanism.machai.project.layout.ProjectLayout)}
 * hook for a layout that returns {@code null} from
 * {@link org.machanism.machai.project.layout.ProjectLayout#getModules()}. A
 * non-null, empty module list is intentionally treated as having no work and
 * does not invoke the hook. Layouts expose root-relative source, test, and
 * documentation paths, together with metadata such as project names and
 * identifiers, when supported by the underlying build system.</p>
 *
 * <h2>Typical usage</h2>
 * <pre><code>
 * java.io.File projectDir = new java.io.File("path/to/project");
 * org.machanism.machai.project.ProjectProcessor processor = createProcessor();
 * processor.scanFolder(projectDir);
 * </code></pre>
 *
 * <p>Implementations should make folder processing safe to invoke once for
 * every discovered leaf project or module. See the
 * {@link org.machanism.machai.project.layout} package for the concrete layout
 * implementations, descriptor-specific behavior, path conventions, and
 * metadata limitations.</p>
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
