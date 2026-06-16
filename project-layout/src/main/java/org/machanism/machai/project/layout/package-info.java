/**
 * APIs for detecting, describing, and working with a repository's on-disk project layout.
 *
 * <p>
 * This package defines {@link org.machanism.machai.project.layout.ProjectLayout}, a common abstraction for
 * representing a project as a root directory together with conventional, root-relative locations such as source,
 * test, and documentation directories. Implementations encapsulate the rules used by different ecosystems to infer
 * those locations from directory conventions or build metadata.
 * </p>
 *
 * <h2>Core concepts</h2>
 * <ul>
 *   <li><strong>Project directory</strong>: the filesystem root against which all discovered relative locations are resolved.</li>
 *   <li><strong>Root-relative path</strong>: layout accessors typically return path relative to the project directory rather than absolute filesystem locations.</li>
 *   <li><strong>Modules</strong>: child projects that may be declared by build metadata or discovered from common multi-module conventions.</li>
 * </ul>
 *
 * <h2>Package responsibilities</h2>
 * <ul>
 *   <li>Provide a uniform way to query source, test, and documentation folders across build systems.</li>
 *   <li>Expose module identifiers and module directories for repositories that contain nested projects.</li>
 *   <li>Offer specialized implementations for common build ecosystems while preserving a consistent programming model.</li>
 * </ul>
 *
 * <h2>Implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} reads Maven-oriented layout information from conventional structure and {@code pom.xml} metadata.</li>
 *   <li>{@link org.machanism.machai.project.layout.GradleProjectLayout} represents Gradle-style project locations and module structure.</li>
 *   <li>{@link org.machanism.machai.project.layout.JSProjectLayout} resolves JavaScript project folders and workspace modules from {@code package.json} configuration.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} identifies Python project structure from {@code pyproject.toml} conventions.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} provides a fallback implementation when no ecosystem-specific model applies.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
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
