/**
 * Detects and models the on-disk layout of a software project.
 *
 * <p>The types in this package identify a project's root directory, modules (if any), and conventional
 * subdirectories such as source, test, and documentation trees. Implementations infer this structure from
 * build descriptors (for example {@code pom.xml}, {@code package.json}, or {@code pyproject.toml}) or by
 * applying sensible defaults when no descriptor is available.
 *
 * <h2>Core abstraction</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.ProjectLayout} represents a discovered layout for a project root.
 *       It exposes the root, module list, conventional directories, and a shared set of exclusion directories
 *       ({@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}) used when scanning the filesystem.</li>
 * </ul>
 *
 * <h2>Provided implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} - Maven layouts derived from {@code pom.xml}
 *       (including module discovery and configured source/test directories).</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} - JavaScript/TypeScript layouts derived from
 *       {@code package.json} and workspace conventions.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} - Python layouts derived from
 *       {@code pyproject.toml} and related metadata.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} - Fallback layout that infers modules from
 *       immediate subdirectories while honoring {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
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
 *          and `&gt;` as `&amp;gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
