/**
 * Provides abstractions and utilities for detecting and processing diverse project layouts
 * across popular build tools and languages, including Java, Node.js, Python, and generic formats.
 * <p>
 * This package enables flexible and extensible management of project structures, including:
 * <ul>
 *   <li>Detection of project types and layouts using various indicators</li>
 *   <li>Scanning directories for modules and source code organization</li>
 *   <li>Delegation of processing actions to appropriate layout implementations</li>
 * </ul>
 * Core classes include:
 * <ul>
 *   <li>{@link org.machanism.machai.project.ProjectLayoutManager}: Detects and instantiates layouts such as Maven, Node.js, Python, or a default generic format.</li>
 *   <li>{@link org.machanism.machai.project.ProjectProcessor}: Abstract processor for scanning files, folders, and modules within a project.</li>
 * </ul>
 * <p>Common usage patterns:</p>
 * <pre>
 *   File dir = new File("/path/to/project");
 *   ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);
 *   // Analyze modules, sources, tests, and documents with ProjectLayout methods
 * </pre>
 * <p>
 * The package supports extension for new languages and build tools, enabling custom layout detection and processing strategies. See the Javadoc for individual classes for detailed API usage.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
package org.machanism.machai.project;

/* @guidance:
*
* **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THIS PACKAGE, INCLUDING THIS `package-info.java`!**
*
* - Use clear and concise descriptions:
*     - Write meaningful summaries that explain the purpose, behavior, and usage of each element.
*     - Avoid vague statements; be specific about functionality and intent.
* - Update `package-info.java`:
*     - Analyze the source code in this package.
*     - Generate comprehensive package-level Javadoc that describes the package’s overall purpose and usage.
*     - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
* - Include usage examples where helpful:
*     - Provide code snippets or examples in Javadoc comments for complex classes or methods.
* - Maintain consistency and formatting:
*     - Follow a consistent style and structure for all Javadoc comments.
*     - Use proper Markdown or HTML formatting for readability.
* - Keep Javadoc up to date with code changes:
*     - Revise Javadoc comments whenever code is modified to ensure documentation remains accurate and current.
* - Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` blocks within Javadoc.
*/
