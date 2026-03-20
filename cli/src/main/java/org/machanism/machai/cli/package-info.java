/**
 * Spring Shell-based command-line interface (CLI) for Machai.
 *
 * <p>This package contains the Spring Boot entry point and Spring Shell command components that expose Machai
 * functionality through both interactive (REPL) and non-interactive command execution.
 *
 * <h2>Command groups</h2>
 * <ul>
 *   <li><strong>Configuration</strong> – {@link org.machanism.machai.cli.ConfigCommand} persists defaults
 *       (for example, working directory and GenAI provider/model) in {@code machai.properties}.</li>
 *   <li><strong>Indexing and registration</strong> – {@link org.machanism.machai.cli.BindexCommand} generates
 *       and registers bindex metadata for projects.</li>
 *   <li><strong>Picking and assembly</strong> – {@link org.machanism.machai.cli.AssembyCommand} performs
 *       semantic search (“pick”) and assembles a project skeleton using a configured GenAI provider.</li>
 *   <li><strong>Document processing</strong> – {@link org.machanism.machai.cli.GWCommand} scans folders and processes
 *       files using the Ghostwriter pipeline; {@link org.machanism.machai.cli.ActCommand} runs an interactive
 *       predefined action/prompt in “Act mode”.</li>
 *   <li><strong>Cleanup</strong> – {@link org.machanism.machai.cli.CleanCommand} removes Machai temporary directories
 *       (for example, {@code .machai}) under a selected root directory.</li>
 * </ul>
 *
 * <h2>How configuration is resolved</h2>
 * <ul>
 *   <li>The application entry point {@link org.machanism.machai.cli.MachaiCLI} loads optional system properties from
 *       {@code machai.properties} or from a file provided via {@code -Dconfig=...}.</li>
 *   <li>Most commands use defaults persisted by {@link org.machanism.machai.cli.ConfigCommand} when an option
 *       (such as {@code --dir} or {@code --model}) is not provided.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * config genai OpenAI:gpt-5.1
 * config dir .\\my-project
 * bindex --dir .\\my-project
 * pick --query "Create a web app" --score 0.8
 * assembly --dir .\\out
 * gw --scanDir .\\my-project --excludes target,.git
 * act commit "and push"
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
