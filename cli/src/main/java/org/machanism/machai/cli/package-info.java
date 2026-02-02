/**
 * Spring Shell-based command-line interface (CLI) for Machai.
 *
 * <p>This package provides the interactive and scripted command surface for Machai via Spring Shell. It contains the
 * application entry point and the command components that expose Machai capabilities through shell commands.
 *
 * <h2>Key concepts</h2>
 * <ul>
 *   <li><strong>Commands</strong>: Spring Shell components that implement CLI verbs such as assembling outputs,
 *       generating indexes, scanning directories, and managing configuration.</li>
 *   <li><strong>Interactive vs. non-interactive</strong>: Commands can be executed within the interactive shell or
 *       invoked directly from a process/script by passing arguments to the CLI entry point.</li>
 * </ul>
 *
 * <h2>Capabilities</h2>
 * <ul>
 *   <li>Discover, select, and assemble reusable "bricks" into generated outputs (e.g., projects or prompts).</li>
 *   <li>Generate and register bindex metadata used for brick discovery and reuse.</li>
 *   <li>Run directory scanning, scoring, and GenAI-backed processing based on configuration.</li>
 *   <li>Clean Machai temporary/output folders and manage configuration defaults used by other commands.</li>
 * </ul>
 *
 * <h2>Primary command components</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.cli.AssemblyCommand} (pick / assembly / prompt)</li>
 *   <li>{@link org.machanism.machai.cli.BindexCommand} (bindex / register)</li>
 *   <li>{@link org.machanism.machai.cli.GWCommand} (gw)</li>
 *   <li>{@link org.machanism.machai.cli.CleanCommand} (clean)</li>
 *   <li>{@link org.machanism.machai.cli.ConfigCommand} (genai / dir / score / conf)</li>
 * </ul>
 *
 * <h2>Entry point</h2>
 * <p>The application entry point is {@link org.machanism.machai.cli.MachaiCLI}.</p>
 *
 * <h2>Usage</h2>
 * <p>Programmatic startup:</p>
 * <pre>
 * public final class Main {
 *   public static void main(String[] args) {
 *     org.machanism.machai.cli.MachaiCLI.main(args);
 *   }
 * }
 * </pre>
 *
 * <p>Example commands:</p>
 * <pre>
 * pick --query "Create a web app" --score 0.9
 * assembly --dir /path/to/out --genai OpenAI:gpt-5.1
 * bindex --dir /path/to/project --update false
 * register --dir /path/to/project --registerUrl http://localhost:8080
 * gw --dir /path/to/project --scan /path/to/project/src/main/java --genai OpenAI:gpt-5.1
 * clean --dir /tmp/machai
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
