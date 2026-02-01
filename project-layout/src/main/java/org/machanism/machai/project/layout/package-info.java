/**
 * Detects and models the on-disk layout of a software project.
 *
 * <p>This package centers around {@link org.machanism.machai.project.layout.ProjectLayout}, which represents a project
 * root directory and provides a uniform way to:
 * <ul>
 *   <li>identify modules/subprojects (for monorepos and multi-module builds),</li>
 *   <li>report conventional source/test/documentation directories when present,</li>
 *   <li>exclude common non-source directories (VCS metadata, dependency caches, build output) from scans.</li>
 * </ul>
 *
 * <p>Concrete implementations infer structure from common build descriptors:
 * <ul>
 *   <li>Maven via {@code pom.xml} (multi-module resolution, build directories),</li>
 *   <li>JavaScript/TypeScript via {@code package.json} workspaces,
 *   <li>Python via {@code pyproject.toml}.</li>
 * </ul>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.ProjectLayout} - Base abstraction and shared path utilities.</li>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} - Resolves Maven modules and conventional paths.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} - Discovers JS/TS workspaces and modules.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} - Detects Python projects from
 *       {@code pyproject.toml}.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} - Fallback that performs directory scanning and
 *       applies {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.</li>
 *   <li>{@link org.machanism.machai.project.layout.PomReader} - Helper for parsing and resolving effective Maven POM
 *       models.</li>
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
