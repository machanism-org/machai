/**
 * Detects and models a repository's on-disk layout so callers can consistently locate source, test, resource, and
 * documentation directories, and optionally discover modules/workspaces.
 *
 * <p>The primary abstraction is {@link org.machanism.machai.project.layout.ProjectLayout}. Concrete implementations
 * encapsulate ecosystem-specific conventions (for example Maven, JavaScript/TypeScript workspaces, or Python) and expose
 * a uniform API for directory discovery.
 *
 * <h2>Key concepts</h2>
 * <ul>
 *   <li><strong>Project root</strong>: configured via
 *       {@link org.machanism.machai.project.layout.ProjectLayout#projectDir(java.io.File)}.</li>
 *   <li><strong>Modules/workspaces</strong>: optional discovery of nested projects (for example Maven multi-module projects or
 *       JS/TS workspaces).</li>
 *   <li><strong>Directory sets</strong>: conventional locations for sources, tests, and documentation.
 *       Implementations may consult build metadata files such as {@code pom.xml} or {@code package.json}.</li>
 *   <li><strong>Exclusions</strong>: a shared list of directories to ignore during scans (see
 *       {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}).</li>
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
