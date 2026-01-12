/**
 * Provides layout structures and utilities for identifying modules, sources, documents, and test directories in different types of software projects.
 * <p>
 * This package defines core abstractions and implementations for handling conventional project organization, including default, Maven, JavaScript/TypeScript, and Python layouts. It supplies mechanisms to scan project roots, recognize appropriate directory structures, and convert absolute paths to relative ones for convenient usage.
 * <p>
 * Typical usage involves selecting a layout handler (such as {@link MavenProjectLayout} or {@link DefaultProjectLayout}), passing the root directory, and then retrieving lists of modules, sources, documentation, and tests.
 * <pre>
 * {@code
 *   ProjectLayout layout = new MavenProjectLayout().projectDir(new File("/repo"));
 *   List<String> sources = layout.getSources();
 * }
 * </pre>
 * <p>
 * Supported layouts include:
 * <ul>
 *   <li>{@link DefaultProjectLayout} – for generic repo analysis</li>
 *   <li>{@link MavenProjectLayout} – for Maven-based projects</li>
 *   <li>{@link JScriptProjectLayout} – for JavaScript/TypeScript projects (package.json workspaces)</li>
 *   <li>{@link PythonProjectLayout} – for Python projects (pyproject.toml, setup.py)</li>
 * </ul>
 * Layout implementations may override methods to identify modules, sources, documents, or tests according to specific conventions and build tools.
 * <p>
 * Directory exclusion rules are standardized in {@link ProjectLayout#EXCLUDE_DIRS}.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
package org.machanism.machai.project.layout;

/*
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE, UPDATE `package-info.java` AND UNIT TESTS FOR THIS PACKAGE!**	
 *
 * - Use Clear and Concise Descriptions:
 *		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 *		- Avoid vague statements; be specific about functionality and intent.
 * - Update `package-info.java`. It should contains all required description 
 * 		- It should include package-level Javadoc in a `package-info.java` file to describe the package’s purpose and usage.
 * 		- Do not create "Guidance and Best Practices" section in `package-info.java` file.
 * - Describe Parameters, Return Values, and Exceptions:
 *		- Use `@param` tags to document all method parameters.
 *		- Use `@return` tags to describe return values.
 *		- Use `@throws` or `@exception` tags to explain when exceptions are thrown.
 * -  Include Usage Examples Where Helpful:
 *		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * -  Maintain Consistency and Formatting:
 *		- Follow a consistent style and structure for all Javadoc comments.
 *		- Use proper Markdown or HTML formatting for readability.
 * -  Update Javadoc with Code Changes:
 *		- Revise Javadoc comments whenever code is modified to ensure documentation remains accurate and up to date.
 * -  Leverage Javadoc Tools:
 *		- Use IDE features or static analysis tools to check for missing or incomplete Javadoc.
 *		- Generate and review Javadoc HTML output regularly to verify quality and completeness.
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 * -  Use the Java version specified in `pom.xml` for code generation.
 */

