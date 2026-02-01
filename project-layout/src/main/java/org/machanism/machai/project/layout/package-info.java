/**
 * Detects and models the on-disk layout of a software project.
 *
 * <p>This package provides a small set of {@code ProjectLayout} implementations that describe where a project’s
 * sources, tests, and documentation typically live on disk. Layouts are discovered from build descriptors when
 * available (for example {@code pom.xml}, {@code package.json}, or {@code pyproject.toml}) and fall back to sensible
 * conventional defaults when no descriptor is present.
 *
 * <p>A {@link org.machanism.machai.project.layout.ProjectLayout} generally:
 * <ul>
 *   <li>Identifies a project root directory.</li>
 *   <li>Discovers modules (if the build tool supports multi-module/workspace layouts).</li>
 *   <li>Exposes conventional directories (for example source, test, and documentation trees).</li>
 *   <li>Provides a shared set of directories to exclude while scanning the filesystem via
 *       {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.</li>
 * </ul>
 *
 * <h2>Implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} - derives layout information from
 *       {@code pom.xml} (including module discovery and configured source/test directories).</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} - derives layout information from
 *       {@code package.json} and common workspace conventions.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} - derives layout information from
 *       {@code pyproject.toml} and related metadata.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} - fallback that infers modules from
 *       immediate subdirectories while honoring {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>
 * ProjectLayout layout = new MavenProjectLayout().projectDir(new java.io.File("/repo"));
 * java.util.List&lt;String&gt; modules = layout.getModules();
 * java.util.List&lt;String&gt; sources = layout.getSources();
 * </pre>
 */
package org.machanism.machai.project.layout;

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
 *      	- Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&amp;gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
