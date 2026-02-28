/**
 * Provides APIs to detect, describe, and work with a repository's on-disk project layout.
 *
 * <p>
 * This package models a project as a root directory combined with a set of conventional, root-relative paths such as
 * production sources, test sources, resources, and documentation roots. Some layouts also support nested modules and can
 * obtain module information by reading build metadata (for example Maven {@code pom.xml}, Gradle build files, or JS
 * workspace configuration).
 * </p>
 *
 * <h2>Core API</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.ProjectLayout} is the base abstraction implemented by all concrete
 *       layout detectors.</li>
 *   <li>Layout instances are configured with a project root via
 *       {@link org.machanism.machai.project.layout.ProjectLayout#projectDir(java.io.File)}.</li>
 *   <li>Most accessors return root-relative paths (usually using {@code /} separators) that should be resolved against
 *       {@link org.machanism.machai.project.layout.ProjectLayout#getProjectDir()} before filesystem access.</li>
 * </ul>
 *
 * <h2>Implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} reads {@code pom.xml} (via
 *       {@link org.machanism.machai.project.layout.PomReader}) to determine modules and source/test/resource roots.</li>
 *   <li>{@link org.machanism.machai.project.layout.GragleProjectLayout} uses the Gradle Tooling API to detect modules and
 *       applies common Gradle source/test conventions.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} parses {@code package.json} workspaces and
 *       resolves module directories by matching workspace glob patterns.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} detects Python projects using
 *       {@code pyproject.toml} metadata.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} provides a minimal filesystem-based fallback and
 *       treats immediate subdirectories as potential modules.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre><code>
 * java.io.File projectDir = new java.io.File("C:\\repo");
 *
 * ProjectLayout layout = new MavenProjectLayout().projectDir(projectDir);
 * java.util.List&lt;String&gt; modules = layout.getModules();
 * java.util.List&lt;String&gt; sources = layout.getSources();
 * java.util.List&lt;String&gt; tests = layout.getTests();
 * java.util.List&lt;String&gt; docs = layout.getDocuments();
 * </code></pre>
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
