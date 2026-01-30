/**
 * Detects and models the on-disk layout of a software project.
 *
 * <p>The types in this package identify common project roots (sources, tests, resources, documentation) and, when
 * applicable, discover module/workspace subprojects under a single repository root. Layout detection is generally
 * driven by ecosystem-specific descriptor files (for example {@code pom.xml} for Maven, {@code package.json} for
 * JavaScript/TypeScript, and {@code pyproject.toml} for Python), with a fallback implementation that scans the
 * filesystem.
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.ProjectLayout} – base abstraction; provides the project root and
 *       path utilities as well as a shared directory exclusion list.</li>
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
 * <p>When scanning directories, implementations should avoid walking into common VCS, vendor, and build-output
 * folders using {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.
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
