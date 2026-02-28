/**
 * Provides APIs to detect, describe, and work with a repository's on-disk project layout.
 *
 * <p>This package models a project as a root directory plus a set of conventional root-relative paths (sources, tests,
 * resources, documentation) and optionally nested modules. The main entry point is
 * {@link org.machanism.machai.project.layout.ProjectLayout}, which returns these paths as root-relative strings.
 *
 * <h2>Key concepts</h2>
 * <ul>
 *   <li><strong>Root-relative paths:</strong> Layout values are typically expressed relative to the configured project root.</li>
 *   <li><strong>Build-aware detection:</strong> Implementations may parse build metadata (for example {@code pom.xml}) to
 *       resolve non-default source sets and discover modules.</li>
 *   <li><strong>Repository scanning:</strong> Some implementations scan the filesystem to infer structure while excluding
 *       common build/VCS/IDE directories using {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * ProjectLayout layout = new MavenProjectLayout().projectDir(new java.io.File("C:\\repo"));
 *
 * java.util.List&lt;String&gt; modules = layout.getModules();
 * java.util.List&lt;String&gt; sources = layout.getSources();
 * java.util.List&lt;String&gt; tests = layout.getTests();
 * java.util.List&lt;String&gt; docs = layout.getDocuments();
 * </pre>
 *
 * <h2>Implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} resolves paths and modules from {@code pom.xml}
 *       (via {@link org.machanism.machai.project.layout.PomReader}).</li>
 *   <li>{@link org.machanism.machai.project.layout.GragleProjectLayout} applies Gradle conventions to determine sources,
 *       tests, and modules.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} inspects {@code package.json} workspaces to
 *       detect JS/TS monorepo modules.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} detects Python projects using
 *       {@code pyproject.toml} metadata.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} provides a minimal fallback layout when no
 *       specific build metadata is available.</li>
 * </ul>
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
 *      - Generate comprehensive package-level Javadoc that clearly describes the package's overall purpose and usage.
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
