/**
 * Command-line interface (CLI) commands for the Machai tool.
 *
 * <p>
 * This package provides the Spring Boot / Spring Shell entry point
 * ({@link org.machanism.machai.cli.MachaiCLI}) and a set of interactive shell commands that
 * orchestrate Machai workflows, including Ghostwriter guidance processing and bindex operations.
 * </p>
 *
 * <h2>Commands</h2>
 * <ul>
 *   <li>
 *     Ghostwriter guidance processing and scanning:
 *     {@link org.machanism.machai.cli.GWCommand}
 *   </li>
 *   <li>
 *     Interactive Act-mode execution:
 *     {@link org.machanism.machai.cli.ActCommand}
 *   </li>
 *   <li>
 *     Bindex generation and registration:
 *     {@link org.machanism.machai.cli.BindexCommand}
 *   </li>
 *   <li>
 *     Persistent CLI configuration management:
 *     {@link org.machanism.machai.cli.ConfigCommand}
 *   </li>
 *   <li>
 *     Cleanup of Machai temporary folders:
 *     {@link org.machanism.machai.cli.CleanCommand}
 *   </li>
 * </ul>
 *
 * <p>
 * Most commands resolve defaults (for example, the project directory and GenAI model) from the
 * shared configuration stored in {@code machai.properties}.
 * </p>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * set --key dir --value .\\my-project
 * gw --scanDir .\\my-project --excludes target,.git
 * act commit "and push"
 * bindex --dir .\\my-project
 * clean --dir .\\my-project
 * </pre>
 */
package org.machanism.machai.cli;

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
 *      - Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
