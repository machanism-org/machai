/**
 * Spring Shell-based command-line interface (CLI) for Machai.
 *
 * <p>This package contains the Spring Boot entry point and Spring Shell command components that expose
 * Machai features through an interactive REPL or non-interactive command execution.
 *
 * <h2>Key components</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.cli.MachaiCLI} – application entry point that boots Spring Boot and loads
 *       optional system properties from {@code machai.properties}.</li>
 *   <li>{@link org.machanism.machai.cli.ConfigCommand} – reads/writes persistent CLI defaults such as the
 *       working directory, GenAI provider/model, and semantic-search score threshold.</li>
 *   <li>{@link org.machanism.machai.cli.AssembyCommand} – picks libraries (Bindexes) from a prompt and assembles
 *       a project using a GenAI provider.</li>
 *   <li>{@link org.machanism.machai.cli.BindexCommand} – generates and registers bindex metadata for projects.</li>
 *   <li>{@link org.machanism.machai.cli.GWCommand} – processes documents/files with the Ghostwriter pipeline.</li>
 *   <li>{@link org.machanism.machai.cli.CleanCommand} – removes Machai temporary directories (e.g. {@code .machai}).</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <p>The CLI is usually started via the {@code MachaiCLI} main class and then commands are invoked within the
 * Spring Shell environment:</p>
 *
 * <pre>
 * config genai OpenAI:gpt-5.1
 * bindex --dir ./my-project
 * pick --query "Create a web app" --score 0.9
 * assembly --dir ./out
 * gw --dir ./my-project --scan ./my-project/docs
 * clean --dir ./my-project
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
