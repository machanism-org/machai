/**
 * APIs for detecting, describing, and working with a repository's on-disk project layout.
 *
 * <p>
 * This package centers on {@link org.machanism.machai.project.layout.ProjectLayout}, an abstraction that models a
 * project as a root directory plus conventional, root-relative locations such as source, test, resource, and
 * documentation roots. Implementations determine these locations either from build-tool conventions or by inspecting
 * build metadata such as Maven {@code pom.xml}, Gradle build files, JavaScript {@code package.json}, or Python
 * {@code pyproject.toml}.
 * </p>
 *
 * <h2>Core concepts</h2>
 * <ul>
 *   <li><strong>Project directory</strong>: the filesystem root from which all returned paths are interpreted.</li>
 *   <li><strong>Root-relative locations</strong>: most accessor methods return relative paths that should be resolved
 *       against the configured project directory before direct filesystem use.</li>
 *   <li><strong>Modules</strong>: child projects discovered from build metadata or directory conventions for
 *       multi-module repositories.</li>
 * </ul>
 *
 * <h2>Package responsibilities</h2>
 * <ul>
 *   <li>Provide a common interface for querying standard project folders independent of build system.</li>
 *   <li>Support discovery of module identifiers and module directories for multi-project builds.</li>
 *   <li>Offer specialized implementations for common ecosystems while preserving a shared access pattern.</li>
 * </ul>
 *
 * <h2>Implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} derives layout information from Maven
 *       project structure and {@code pom.xml} metadata.</li>
 *   <li>{@link org.machanism.machai.project.layout.GragleProjectLayout} exposes conventional Gradle-oriented
 *       project locations.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} resolves JavaScript workspace modules from
 *       {@code package.json} configuration.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} identifies Python project structure using
 *       {@code pyproject.toml} conventions.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} acts as a fallback implementation when no
 *       tool-specific model is available.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre><code>
 * java.io.File projectDir = new java.io.File("repo");
 *
 * ProjectLayout layout = new MavenProjectLayout().projectDir(projectDir);
 * java.util.List&lt;String&gt; modules = layout.getModules();
 * java.util.Collection&lt;String&gt; sources = layout.getSources();
 * java.util.Collection&lt;String&gt; tests = layout.getTests();
 * java.util.Collection&lt;String&gt; documents = layout.getDocuments();
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
