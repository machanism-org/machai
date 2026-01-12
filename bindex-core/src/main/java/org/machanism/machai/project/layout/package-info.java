/**
 * Provides project layout abstractions and implementations for various build and source structures.
 * <p>
 * This package contains utilities and classes to analyze, describe, and interact with the file
 * layout of software projects. It supports popular build systems and languages, including Maven,
 * JavaScript/TypeScript, Python, and default/custom directory structures. The primary functionality
 * includes module detection, source/document/test directory identification, and project model
 * parsing.
 * <p>
 * <strong>Main Classes:</strong>
 * <ul>
 *   <li>{@link org.machanism.machai.project.layout.ProjectLayout}: Abstract base class for defining project layout contracts and utilities.</li>
 *   <li>{@link org.machanism.machai.project.layout.DefaultProjectLayout}: Handles generic filesystem-based modules and excludes standard build directories.</li>
 *   <li>{@link org.machanism.machai.project.layout.MavenProjectLayout}: Detects Maven modules, sources, documents, and tests via <code>pom.xml</code> and effective model parsing.</li>
 *   <li>{@link org.machanism.machai.project.layout.JScriptProjectLayout}: Supports JavaScript/TypeScript projects, reading workspace modules from <code>package.json</code> files.</li>
 *   <li>{@link org.machanism.machai.project.layout.PythonProjectLayout}: Analyzes Python projects, focusing on <code>pyproject.toml</code> and associated metadata.</li>
 *   <li>{@link org.machanism.machai.project.layout.PomReader}: Utility for parsing Maven <code>pom.xml</code> files and producing model representations.</li>
 * </ul>
 * <strong>Usage Example:</strong>
 * <pre>
 * ProjectLayout layout = new MavenProjectLayout().projectDir(new File("/workspace"));
 * List&lt;String&gt; modules = layout.getModules();
 * List&lt;String&gt; sources = layout.getSources();
 * List&lt;String&gt; docs = layout.getDocuments();
 * List&lt;String&gt; tests = layout.getTests();
 * </pre>
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