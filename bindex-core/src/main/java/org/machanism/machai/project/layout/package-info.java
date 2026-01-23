/**
 * Detects and describes a project's on-disk structure.
 *
 * <p>This package defines the {@link org.machanism.machai.project.layout.ProjectLayout} abstraction and concrete
 * implementations that infer a project's directory layout (sources, tests, documents, and optional modules) from a
 * project root.
 *
 * <p>Typical usage starts by selecting an implementation based on the project's build or configuration descriptor
 * (for example {@code pom.xml}, {@code package.json}, or {@code pyproject.toml}) and then asking the layout for
 * well-known directory groups.
 *
 * <h2>Implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} - Maven projects detected by {@code pom.xml}.
 *       Uses {@link org.machanism.machai.project.layout.PomReader} when POM parsing is needed.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} - JavaScript/TypeScript projects detected by
 *       {@code package.json}; can discover workspace modules.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} - Python projects detected by
 *       {@code pyproject.toml} and basic project conventions.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} - Fallback strategy that scans direct child
 *       directories and treats those with a detectable non-default layout as modules.</li>
 * </ul>
 *
 * <h2>Filesystem traversal</h2>
 * <p>When scanning directories, implementations should exclude common VCS, vendor, and build output directories via
 * {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.
 *
 * <h2>Example</h2>
 * {@snippet lang = java:
 * File projectRoot = new File("/workspace/my-project");
 * ProjectLayout layout = new MavenProjectLayout().projectDir(projectRoot);
 *
 * List<String> modules = layout.getModules();
 * List<String> sources = layout.getSources();
 * List<String> tests = layout.getTests();
 * List<String> docs = layout.getDocuments();
 * }
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
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
