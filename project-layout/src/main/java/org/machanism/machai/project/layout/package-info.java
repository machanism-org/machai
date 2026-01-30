/**
 * Detects and models the on-disk layout of a software project.
 *
 * <p>This package provides a {@link org.machanism.machai.project.layout.ProjectLayout} abstraction and
 * ecosystem-specific implementations that infer a repository's structure (project root, module/workspace
 * subprojects, and conventional directories such as sources, tests, resources, and documentation).
 *
 * <p>Detection is typically driven by well-known descriptor files for an ecosystem (for example
 * {@code pom.xml} for Maven, {@code package.json} for JavaScript/TypeScript workspaces, and
 * {@code pyproject.toml} for Python). Where no descriptor is available (or detection is ambiguous), a
 * default implementation may fall back to filesystem heuristics.
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.ProjectLayout} – base abstraction; provides the project root,
 *       common directory queries, and a shared directory exclusion list used during traversal.</li>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} – Maven layout derived from {@code pom.xml}.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} – workspace/module discovery from
 *       {@code package.json}.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} – Python project detection via
 *       {@code pyproject.toml}.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} – fallback layout that attempts to infer
 *       modules by scanning the filesystem.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * ProjectLayout layout = new MavenProjectLayout().projectDir(new java.io.File("/repo"));
 * java.util.List&lt;String&gt; modules = layout.getModules();
 * java.util.List&lt;String&gt; sources = layout.getSources();
 * </pre>
 *
 * <h2>Filesystem traversal</h2>
 * <p>Implementations that scan the filesystem should avoid walking into common VCS, vendor, and build-output
 * directories by honoring {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.
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
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
