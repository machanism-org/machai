/**
 * Detects and models the on-disk layout of a software project.
 *
 * <p>This package provides {@link org.machanism.machai.project.layout.ProjectLayout} and several concrete
 * implementations that infer a project's structure (modules and conventional directories) from common build
 * descriptors.
 *
 * <h2>What a layout provides</h2>
 * <ul>
 *   <li><strong>Project root:</strong> {@link org.machanism.machai.project.layout.ProjectLayout#projectDir(java.io.File)}
 *       sets the directory used as the base for all lookups.</li>
 *   <li><strong>Modules/subprojects:</strong> {@link org.machanism.machai.project.layout.ProjectLayout#getModules()}
 *       returns module paths when the project type supports it (for example Maven multi-module builds or JS/TS
 *       workspaces).</li>
 *   <li><strong>Conventional directories:</strong> {@link org.machanism.machai.project.layout.ProjectLayout#getSources()},
 *       {@link org.machanism.machai.project.layout.ProjectLayout#getTests()}, and
 *       {@link org.machanism.machai.project.layout.ProjectLayout#getDocuments()} return paths relative to the project
 *       root when implementations can determine them.</li>
 *   <li><strong>Scan exclusions:</strong> {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS} defines
 *       directories that should be ignored by directory-based discovery (VCS metadata, dependency caches, IDE
 *       directories, build output, etc.).</li>
 * </ul>
 *
 * <h2>Implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} - Reads {@code pom.xml} (optionally building an
 *       effective model) to resolve modules and to extract source/test/resource directories from the Maven build
 *       section.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} - Reads {@code package.json} and discovers
 *       workspace modules by scanning for nested {@code package.json} files while excluding
 *       {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} - Detects Python projects via
 *       {@code pyproject.toml} and related metadata.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} - Fallback that lists immediate
 *       subdirectories as modules, excluding {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.</li>
 *   <li>{@link org.machanism.machai.project.layout.PomReader} - Helper that parses and optionally computes effective
 *       Maven models.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * ProjectLayout layout = new MavenProjectLayout().projectDir(new java.io.File("/repo"));
 * java.util.List&amp;lt;String&amp;gt; modules = layout.getModules();
 * java.util.List&amp;lt;String&amp;gt; sources = layout.getSources();
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
