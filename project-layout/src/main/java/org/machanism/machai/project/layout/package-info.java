/**
 * APIs for detecting and describing a repository's on-disk project layout.
 *
 * <p>
 * A {@link org.machanism.machai.project.layout.ProjectLayout} represents a configured project root and exposes
 * conventional locations as paths relative to that root. The concrete implementations adapt the common API to Maven,
 * Gradle, JavaScript/TypeScript, Python, and unknown project structures. Configure the root with
 * {@link org.machanism.machai.project.layout.ProjectLayout#projectDir(java.io.File)} before querying an instance.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Expose production-source, test-source, and documentation directories as root-relative paths.</li>
 *   <li>Discover child modules from build metadata or filesystem conventions where the ecosystem supports it.</li>
 *   <li>Provide project identifiers, names, parent identifiers, and a layout type when the underlying metadata supplies them.</li>
 *   <li>Offer shared path, directory-scanning, exclusion, and temporary-directory utilities through {@link org.machanism.machai.project.layout.ProjectLayout}.</li>
 *   <li>Parse Maven descriptors and serialize Maven models with {@link org.machanism.machai.project.layout.PomReader}.</li>
 * </ul>
 *
 * <h2>Choosing a layout</h2>
 * <p>
 * Use the descriptor-detection methods on the applicable implementation before constructing a specialized layout:
 * {@link org.machanism.machai.project.layout.MavenProjectLayout#isMavenProject(java.io.File)} checks for
 * {@code pom.xml}, {@link org.machanism.machai.project.layout.GradleProjectLayout#isGradleProject(java.io.File)}
 * checks for {@code build.gradle},
 * {@link org.machanism.machai.project.layout.JScriptProjectLayout#isPackageJsonPresent(java.io.File)} checks for
 * {@code package.json}, and
 * {@link org.machanism.machai.project.layout.PythonProjectLayout#isPythonProject(java.io.File)} validates a public,
 * named {@code pyproject.toml} project. When no specialized descriptor applies,
 * {@link org.machanism.machai.project.layout.DefaultProjectLayout} offers a filesystem-based fallback.
 * </p>
 *
 * <h2>Supported layouts</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} reads {@code pom.xml}, including Maven modules,
 *       build source roots, resources, tests, project coordinates, and parent coordinates.</li>
 *   <li>{@link org.machanism.machai.project.layout.GradleProjectLayout} uses the Gradle Tooling API for project and
 *       child-module names and supplies conventional {@code src/main}, {@code src/test}, and {@code src/site} roots.
 *       It does not inspect custom Gradle source sets.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} reads array-form {@code package.json}
 *       workspace globs and identifies matching workspace directories containing their own package descriptor.
 *       Source, test, and documentation discovery return empty collections.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} recognizes public projects described by
 *       {@code pyproject.toml}; source, test, and documentation discovery currently returns empty collections.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} provides a filesystem fallback that treats
 *       non-excluded immediate subdirectories as module candidates and returns empty location collections.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre><code>
 * java.io.File projectDir = new java.io.File("repo");
 * ProjectLayout layout = new MavenProjectLayout().projectDir(projectDir);
 * java.util.Collection&lt;String&gt; sources = layout.getSources();
 * java.util.Collection&lt;String&gt; tests = layout.getTests();
 * java.util.List&lt;String&gt; modules = layout.getModules();
 * </code></pre>
 *
 * <p>
 * Returned paths are intended to be resolved against {@code projectDir}. A layout may return {@code null} for modules
 * when the project is not a parent project; callers should handle that result according to the selected implementation's
 * contract. Some layout methods load and parse their build descriptor when invoked and can report malformed or missing
 * metadata through their documented exceptions.
 * </p>
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
