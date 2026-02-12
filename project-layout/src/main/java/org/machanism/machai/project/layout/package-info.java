/**
 * APIs for detecting and modeling a repository's on-disk project layout.
 *
 * <p>This package defines {@link org.machanism.machai.project.layout.ProjectLayout}, an abstraction representing a
 * project rooted at a configured base directory. A {@code ProjectLayout} exposes conventional locations (relative to
 * the project root) for:
 * <ul>
 *   <li>main sources and resources</li>
 *   <li>test sources and resources</li>
 *   <li>documentation</li>
 *   <li>(optionally) nested modules</li>
 * </ul>
 *
 * <p>Implementations encapsulate ecosystem-specific conventions and configuration sources, such as build descriptors
 * (for example, {@code pom.xml}), workspace manifests (for example, {@code package.json}), or Python metadata (for
 * example, {@code pyproject.toml}). A minimal default implementation is provided for repositories that do not match a
 * specific ecosystem.
 *
 * <h2>Repository scanning and exclusions</h2>
 * <p>When scanning for nested modules, implementations typically skip common build, VCS, and environment directories
 * using {@link org.machanism.machai.project.layout.ProjectLayout#EXCLUDE_DIRS}.
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
