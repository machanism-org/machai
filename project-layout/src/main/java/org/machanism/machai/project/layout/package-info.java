/**
 * Detects and models a repository's on-disk layout so callers can consistently locate
 * source, test, resource, and documentation directories, and optionally discover modules/workspaces.
 *
 * <p>The central abstraction is {@link org.machanism.machai.project.layout.ProjectLayout}. A caller selects an
 * implementation based on build metadata present at the repository root (for example {@code pom.xml} for Maven,
 * {@code package.json} for JavaScript workspaces, or {@code pyproject.toml} for Python projects), sets the project
 * directory, and then queries the layout for directories to scan.
 *
 * <h2>Key concepts</h2>
 * <ul>
 *   <li><b>Project root</b>: the repository directory configured via
 *       {@link org.machanism.machai.project.layout.ProjectLayout#projectDir(java.io.File)}.</li>
 *   <li><b>Modules/workspaces</b>: optional subprojects discovered from build descriptors via
 *       {@link org.machanism.machai.project.layout.ProjectLayout#getModules()}.</li>
 *   <li><b>Directory sets</b>: conventional paths returned by
 *       {@link org.machanism.machai.project.layout.ProjectLayout#getSources()},
 *       {@link org.machanism.machai.project.layout.ProjectLayout#getTests()},
 *       {@link org.machanism.machai.project.layout.ProjectLayout#getDocuments()}.</li>
 *   <li><b>Exclusions</b>: shared directory names that should be skipped during filesystem walks via
 *       {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * ProjectLayout layout = new MavenProjectLayout()
 *         .projectDir(new java.io.File("/repo"));
 *
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
 *          and `&gt;` as `&amp;gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
