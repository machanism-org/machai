/**
 * Detects and describes a project's on-disk layout.
 *
 * <p>This package provides {@link org.machanism.machai.project.layout.ProjectLayout} implementations that identify a
 * project's root directory, its modules (if any), and well-known root directories such as source code, test code, and
 * documentation. Layout detection is typically based on the presence of build/configuration files (for example
 * {@code pom.xml}, {@code package.json}, or {@code pyproject.toml}), from which module boundaries and standard folder
 * conventions can be derived.
 *
 * <h2>Provided layouts</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout} - Maven projects detected via {@code pom.xml}
 *   (may parse a POM via {@link org.machanism.machai.project.layout.PomReader}).</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout} - JavaScript/TypeScript projects detected via
 *   {@code package.json} (reads workspace modules).</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout} - Python projects detected via
 *   {@code pyproject.toml} (basic metadata-based detection).</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout} - Fallback layout that scans direct child
 *   directories and treats those with a detectable layout as modules.</li>
 * </ul>
 *
 * <p>Filesystem traversal should ignore common vendor, VCS, and build output directories using
 * {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * File projectRoot = new File("/workspace/my-project");
 *
 * ProjectLayout layout = new MavenProjectLayout().projectDir(projectRoot);
 * List<String> modules = layout.getModules();
 * List<String> sources = layout.getSources();
 * List<String> tests = layout.getTests();
 * List<String> docs = layout.getDocuments();
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
 * 		- 
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * 		
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 
 */
