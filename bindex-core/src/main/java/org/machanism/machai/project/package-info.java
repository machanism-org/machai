/**
 * Provides classes and abstractions for detecting and processing project layouts across various build tools and languages.
 * <p>
 * This package enables management and detection of project structures for Java (Maven), Node.js, Python, and generic project formats.
 * Core functionality includes scanning project directories, identifying module layouts, and delegating processing actions.
 * <p>
 * Main classes include:
 * <ul>
 *   <li>{@link org.machanism.machai.project.ProjectLayoutManager} — Detects and instantiates appropriate {@code ProjectLayout} implementations based on directory analysis.</li>
 *   <li>{@link org.machanism.machai.project.ProjectProcessor} — Provides an abstract base for folder and module processing logic.</li>
 * </ul>
 * <p>
 * Usage Example:
 * <pre>
 *   File dir = new File("/path/to/project");
 *   ProjectLayout layout = ProjectLayoutManager.detectProjectLayout(dir);
 * </pre>
 * <p>
 * See individual class Javadocs for specific usage and details.
 *
 * @author machanism
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
