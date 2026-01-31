/**
 * Detects and models the on-disk layout of a software project.
 *
 * <p>This package provides the {@link org.machanism.machai.project.layout.ProjectLayout} abstraction and concrete
 * implementations that infer repository structure from build descriptors.
 * Implementations can:
 * <ul>
 *   <li>detect whether a directory is a supported project type (for example Maven, JS/TS, or Python),</li>
 *   <li>discover workspace/module structure in monorepos and multi-module builds, and</li>
 *   <li>report conventional source, test, and documentation directories when available.</li>
 * </ul>
 *
 * <h2>Implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} reads {@code pom.xml} to resolve modules and
 *       Maven build directories.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} reads {@code package.json} workspaces to
 *       discover JS/TS modules.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} inspects {@code pyproject.toml} metadata to
 *       determine whether a directory represents a Python project.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} is a fallback that scans immediate
 *       subdirectories and excludes common VCS, dependency, and build-output directories via
 *       {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.</li>
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
