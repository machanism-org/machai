/**
 * Spring Shell-based command-line interface (CLI) for Machai.
 *
 * <p>This package contains {@code @ShellComponent}-annotated command classes that expose Machai
 * functionality through an interactive shell (and non-interactive invocation via Spring Shell).
 * Commands coordinate GenAI providers and Machai services to perform project-centric tasks such as
 * library selection, project assembly, metadata indexing, and document/source processing.
 *
 * <h2>Key commands</h2>
 * <ul>
 *   <li><b>pick / assembly / prompt</b> ({@link org.machanism.machai.cli.AssembyCommand})
 *   for picking "bricks" (libraries) from a bindex registry and assembling a project.</li>
 *   <li><b>bindex / register</b> ({@link org.machanism.machai.cli.BindexCommand})
 *   for generating and registering bindex metadata.</li>
 *   <li><b>gw</b> ({@link org.machanism.machai.cli.GWCommand})
 *   for scanning and processing files with a configured GenAI model.</li>
 *   <li><b>clean</b> ({@link org.machanism.machai.cli.CleanCommand})
 *   for removing Machai temporary folders.</li>
 *   <li><b>genai / dir / score / conf</b> ({@link org.machanism.machai.cli.ConfigCommand})
 *   for configuring defaults used by other commands.</li>
 * </ul>
 *
 * <p>The application entry point is {@link org.machanism.machai.cli.MachaiCLI}.</p>
 *
 * <h2>Usage</h2>
 *
 * <p>Programmatic startup:</p>
 * <pre>
 * {@code
 * public final class Main {
 *   public static void main(String[] args) throws Exception {
 *     org.machanism.machai.cli.MachaiCLI.main(args);
 *   }
 * }
 * }
 * </pre>
 *
 * <p>Typical interactive commands (examples):</p>
 * <pre>
 * {@code
 * # pick libraries for a requirement description
 * pick --query "Create a web app" --score 0.9
 *
 * # assemble a project into a directory
 * assembly --dir /path/to/out --genai OpenAI:gpt-5.1
 *
 * # generate and/or register bindex metadata
 * bindex --dir /path/to/project --update false
 * register --dir /path/to/project --registerUrl http://localhost:8080
 *
 * # process sources/documents
 * gw --dir /path/to/project --scan /path/to/project/src/main/java --genai OpenAI:gpt-5.1
 *
 * # clean temporary output folders
 * clean --dir /tmp/machai
 * }
 * </pre>
 */
package org.machanism.machai.cli;

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
 *      - Generate comprehensive package-level Javadoc that clearly describes the packages overall purpose and usage.
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
