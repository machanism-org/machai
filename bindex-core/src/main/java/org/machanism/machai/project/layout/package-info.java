/**
 * Detects and describes a project's on-disk layout.
 *
 * <p>The types in this package provide the {@link org.machanism.machai.project.layout.ProjectLayout} abstraction
 * and concrete implementations that infer module boundaries and common root directories (sources, tests, and
 * documentation) from a project root directory.
 *
 * <p>Layout detection is typically performed by checking for build/configuration files:
 * <ul>
 *   <li>{@code pom.xml} for {@link org.machanism.machai.project.layout.MavenProjectLayout}</li>
 *   <li>{@code package.json} for {@link org.machanism.machai.project.layout.JScriptProjectLayout}</li>
 *   <li>{@code pyproject.toml} for {@link org.machanism.machai.project.layout.PythonProjectLayout}</li>
 * </ul>
 * When none match, {@link org.machanism.machai.project.layout.DefaultProjectLayout} can be used as a fallback
 * that attempts to identify module directories by scanning immediate children.
 *
 * <p>Implementations that traverse the filesystem should exclude common vendor, VCS, and build output directories
 * using {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.
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
