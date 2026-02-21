/**
 * APIs for detecting, describing, and working with a repository's on-disk project layout.
 *
 * <p>This package provides the {@link org.machanism.machai.project.layout.ProjectLayout} abstraction, which models a
 * project rooted at a configured base directory and exposes conventional locations (relative to that root) such as:
 *
 * <ul>
 *   <li>main sources and resources (see {@link org.machanism.machai.project.layout.ProjectLayout#getSources()})</li>
 *   <li>test sources and resources (see {@link org.machanism.machai.project.layout.ProjectLayout#getTests()})</li>
 *   <li>documentation (see {@link org.machanism.machai.project.layout.ProjectLayout#getDocuments()})</li>
 *   <li>optionally nested modules (see {@link org.machanism.machai.project.layout.ProjectLayout#getModules()})</li>
 * </ul>
 *
 * <p>Concrete implementations encapsulate ecosystem-specific conventions and configuration sources:
 *
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} parses {@code pom.xml} via
 *       {@link org.machanism.machai.project.layout.PomReader} to resolve sources, tests, and multi-module structure.</li>
 *   <li>{@link org.machanism.machai.project.layout.GragleProjectLayout} uses the Gradle Tooling API to discover modules
 *       and applies standard Gradle directory conventions.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} inspects {@code package.json} workspaces to
 *       detect modules in JS/TS monorepos.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} detects Python projects using
 *       {@code pyproject.toml} metadata.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} provides a minimal fallback when no
 *       ecosystem-specific layout is detected.</li>
 * </ul>
 *
 * <h2>Directory scanning and exclusions</h2>
 * <p>Some implementations scan a repository (for example, to discover nested modules). During scanning, common build,
 * VCS, IDE, and environment directories are typically excluded using
 * {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.
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
