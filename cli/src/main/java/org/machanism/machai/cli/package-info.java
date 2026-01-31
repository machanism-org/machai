/**
 * Spring Shell-based command-line interface (CLI) for Machai.
 *
 * <p>This package contains the Spring Shell components and application entry point that expose Machai
 * functionality through an interactive or non-interactive command-line experience. Commands orchestrate
 * use-cases such as selecting and assembling libraries ("bricks"), generating and registering bindex metadata,
 * scanning source/document trees, and applying a configured GenAI model.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Search and select bricks, then assemble projects or prompts.</li>
 *   <li>Generate and register bindex metadata.</li>
 *   <li>Scan directories, score content, and run GenAI processing based on configuration.</li>
 *   <li>Clean Machai temporary and output folders.</li>
 *   <li>Manage configuration defaults used by other commands.</li>
 * </ul>
 *
 * <h2>Command groups</h2>
 * <ul>
 *   <li><b>pick / assembly / prompt</b> ({@link org.machanism.machai.cli.AssemblyCommand})</li>
 *   <li><b>bindex / register</b> ({@link org.machanism.machai.cli.BindexCommand})</li>
 *   <li><b>gw</b> ({@link org.machanism.machai.cli.GWCommand})</li>
 *   <li><b>clean</b> ({@link org.machanism.machai.cli.CleanCommand})</li>
 *   <li><b>genai / dir / score / conf</b> ({@link org.machanism.machai.cli.ConfigCommand})</li>
 * </ul>
 *
 * <h2>Entry point</h2>
 * <p>The application entry point is {@link org.machanism.machai.cli.MachaiCLI}.</p>
 *
 * <h2>Usage</h2>
 * <pre>
 * public final class Main {
 *   public static void main(String[] args) throws Exception {
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
