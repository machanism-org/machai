/**
 * Detects and describes a project's on-disk layout.
 *
 * <p>This package provides the {@link org.machanism.machai.project.layout.ProjectLayout} abstraction and a set
 * of implementations that infer a project's structure (modules, source roots, test roots, and documentation
 * roots) based on common conventions and build/configuration files.
 *
 * <p>Typical usage is to initialize a layout with a project root directory and then query it for the
 * relevant relative paths. Implementations that traverse the filesystem generally apply
 * {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS} to skip generated artifacts and
 * vendor directories.
 *
 * <h2>Provided implementations</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} - Maven single and multi-module projects</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} - JavaScript/TypeScript workspaces</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} - Python projects</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} - fallback when no specific layout matches</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * ProjectLayout layout = new MavenProjectLayout().projectDir(new File("/workspace"));
 * List<String> modules = layout.getModules();
 * List<String> sources = layout.getSources();
 * List<String> docs = layout.getDocuments();
 * List<String> tests = layout.getTests();
 * }</pre>
 */
package org.machanism.machai.project.layout;

/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND THIS `package-info.java`!**
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * 
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the package's overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 *      
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * 
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * 
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */
