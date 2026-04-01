/**
 * Command-line interface (CLI) for running Machai and Ghostwriter workflows.
 *
 * <p>
 * This package contains the Spring Boot application entry point and the Spring Shell commands that expose
 * Machai functionality via an interactive shell. Commands generally read and persist defaults
 * (for example, working directory, GenAI provider/model, and Ghostwriter instructions) via the shared
 * {@code machai.properties} configuration.
 * </p>
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.cli.MachaiCLI} - Spring Boot application entry point.</li>
 *   <li>{@link org.machanism.machai.cli.GWCommand} - Ghostwriter guidance scanning and processing.</li>
 *   <li>{@link org.machanism.machai.cli.ActCommand} - Interactive Act-mode execution.</li>
 *   <li>{@link org.machanism.machai.cli.ConfigCommand} - Persistent CLI configuration management.</li>
 *   <li>{@link org.machanism.machai.cli.CleanCommand} - Cleanup of Machai temporary folders.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * config dir .\\my-project
 * gw --scanDir .\\my-project --excludes target,.git
 * act commit "and push"
 * clean --dir .\\my-project
 * </pre>
 */
package org.machanism.machai.cli;

/*-
 * @guidance:
 *
 * **IMPORTANT: ADD OR UPDATE JAVADOC TO ALL CLASSES IN THE FOLDER AND THIS `package-info.java`!**	
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
