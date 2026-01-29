/**
 * Detects and describes a project's on-disk layout.
 *
 * <p>This package provides the {@link org.machanism.machai.project.layout.ProjectLayout} abstraction and
 * implementations that infer a project's directory structure (modules, sources, tests, and documentation)
 * from a project root directory.
 *
 * <h2>Layout detection</h2>
 * <p>Implementations generally use a build/configuration descriptor located in the project root:
 * <ul>
 *   <li>Maven projects via {@code pom.xml} ({@link org.machanism.machai.project.layout.MavenProjectLayout})</li>
 *   <li>JavaScript/TypeScript projects via {@code package.json}
 *       ({@link org.machanism.machai.project.layout.JScriptProjectLayout})</li>
 *   <li>Python projects via {@code pyproject.toml} ({@link org.machanism.machai.project.layout.PythonProjectLayout})</li>
 *   <li>Fallback scanning when no known descriptor is found
 *       ({@link org.machanism.machai.project.layout.DefaultProjectLayout})</li>
 * </ul>
 *
 * <h2>Modules</h2>
 * <p>Some layouts represent multi-module or workspace projects. For example, Maven modules can be obtained from the
 * {@code <modules>} section of the POM (when packaging is {@code pom}), while JavaScript/TypeScript workspaces can be
 * discovered from the {@code workspaces} key in {@code package.json}.
 *
 * <h2>Filesystem traversal</h2>
 * <p>When scanning the filesystem (for example, to locate workspace modules), implementations should avoid traversing
 * common VCS, vendor, and build-output directories using
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
