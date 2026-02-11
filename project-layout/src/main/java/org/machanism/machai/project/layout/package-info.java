/**
 * APIs for detecting and modeling a repository's on-disk project layout.
 *
 * <p>This package defines {@link org.machanism.machai.project.layout.ProjectLayout}, an abstraction
 * that represents a project rooted at a configured base directory and exposes conventional locations
 * for main sources/resources, test sources/resources, documentation, and (optionally) nested modules.
 * Implementations encapsulate ecosystem-specific conventions and configuration sources.
 *
 * <h2>Provided layouts</h2>
 * <ul>
 *   <li>Maven projects via {@link org.machanism.machai.project.layout.MavenProjectLayout} (parses
 *       {@code pom.xml} using {@link org.machanism.machai.project.layout.PomReader}).</li>
 *   <li>JavaScript/TypeScript workspaces via {@link org.machanism.machai.project.layout.JScriptProjectLayout}
 *       (reads {@code package.json}).</li>
 *   <li>Python projects via {@link org.machanism.machai.project.layout.PythonProjectLayout} (inspects
 *       {@code pyproject.toml}).</li>
 *   <li>A minimal fallback via {@link org.machanism.machai.project.layout.DefaultProjectLayout}.</li>
 * </ul>
 *
 * <h2>Repository scanning and exclusions</h2>
 * <p>When scanning a repository for nested modules, implementations typically exclude common build,
 * VCS, and environment directories using {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.
 *
 * <h2>Typical usage</h2>
 * <pre>
 * ProjectLayout layout = new MavenProjectLayout()
 *         .projectDir(new java.io.File("C:\\repo"));
 *
 * java.util.List&lt;String&gt; modules = layout.getModules();
 * java.util.List&lt;String&gt; sources = layout.getSources();
 * java.util.List&lt;String&gt; tests = layout.getTests();
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
