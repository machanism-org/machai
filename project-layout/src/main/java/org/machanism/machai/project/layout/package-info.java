/**
 * APIs for detecting, describing, and working with a repository's on-disk project layout.
 *
 * <p>
 * This package centers on {@link org.machanism.machai.project.layout.ProjectLayout}, an abstraction that models a
 * project as a root directory plus conventional, root-relative locations such as production sources, tests, resources,
 * and documentation roots. Implementations typically infer these locations by applying build-tool conventions and/or
 * inspecting build metadata (for example, Maven {@code pom.xml}, Gradle build files, JavaScript {@code package.json}
 * workspaces, or Python {@code pyproject.toml}).
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Represent the project root directory and resolve root-relative locations for common folders.</li>
 *   <li>Enumerate modules for multi-module repositories and provide module identifiers relative to the project root.</li>
 *   <li>Offer consistent access to sources, tests, resources, and documentation roots across different build systems.</li>
 * </ul>
 *
 * <h2>Terminology</h2>
 * <ul>
 *   <li><strong>Project root</strong>: configured via {@link org.machanism.machai.project.layout.ProjectLayout#projectDir(java.io.File)}.
 *       Most accessors assume the root is configured.</li>
 *   <li><strong>Root-relative paths</strong>: returned locations are typically expressed relative to
 *       {@link org.machanism.machai.project.layout.ProjectLayout#getProjectDir()} and should be resolved against that
 *       directory prior to filesystem access.</li>
 *   <li><strong>Modules</strong>: nested projects discovered by a build tool (for example, Maven reactor modules, Gradle
 *       multi-project builds, JavaScript workspaces). Module identifiers are generally root-relative.</li>
 * </ul>
 *
 * <h2>Provided implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} reads {@code pom.xml} (via
 *       {@link org.machanism.machai.project.layout.PomReader}) to determine modules and conventional
 *       source/test/resource roots.</li>
 *   <li>{@link org.machanism.machai.project.layout.GradleProjectLayout} uses Gradle build metadata to return
 *       conventional source/test/document roots.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} parses {@code package.json} workspaces and
 *       resolves module directories by matching workspace glob patterns.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} detects Python projects using
 *       {@code pyproject.toml} metadata.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} provides a minimal filesystem-based fallback
 *       and may treat immediate subdirectories as potential modules.</li>
 * </ul>
 *
 * <h2>Example</h2>
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
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
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
