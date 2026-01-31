/**
 * Detects and models the on-disk layout of a software project.
 *
 * <p>This package defines the {@link org.machanism.machai.project.layout.ProjectLayout} abstraction and
 * related implementations used to discover a repository's structure, including:
 * <ul>
 *   <li>the project root directory,</li>
 *   <li>module/workspace structure (for multi-module repositories), and</li>
 *   <li>conventional directories for sources, tests, and documentation.</li>
 * </ul>
 *
 * <p>Layout implementations are ecosystem-specific where possible and typically inspect descriptor files such as:
 * <ul>
 *   <li>Maven: {@code pom.xml} (see {@link org.machanism.machai.project.layout.MavenProjectLayout})</li>
 *   <li>JavaScript/TypeScript: {@code package.json} workspaces
 *       (see {@link org.machanism.machai.project.layout.JScriptProjectLayout})</li>
 *   <li>Python: {@code pyproject.toml} (see {@link org.machanism.machai.project.layout.PythonProjectLayout})</li>
 * </ul>
 *
 * <p>When descriptors are absent or ambiguous, {@link org.machanism.machai.project.layout.DefaultProjectLayout}
 * can be used as a fallback that infers modules by scanning the filesystem while excluding common VCS,
 * dependency, and build-output directories via {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.
 *
 * <h2>Typical usage</h2>
 * <pre>
 * ProjectLayout layout = new MavenProjectLayout().projectDir(new java.io.File("/repo"));
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
 *          and `&gt;` as `&amp;gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
