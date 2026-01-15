/**
 * Spring Shell-based command-line interface (CLI) for Machai.
 *
 * <p>This package contains interactive commands (typically implemented as Spring Shell
 * {@code @ShellComponent}s) that orchestrate Machai workflows such as:</p>
 *
 * <ul>
 *   <li>Picking libraries ("bricks") from a bindex registry using natural-language queries.</li>
 *   <li>Assembling a project based on a prompt and selected bricks.</li>
 *   <li>Generating and registering bindex metadata.</li>
 *   <li>Processing project documents/sources with a configured GenAI provider.</li>
 *   <li>Cleaning temporary or working directories created during CLI runs.</li>
 * </ul>
 *
 * <p>The primary entry point is {@link org.machanism.machai.cli.MachaiCLI}.</p>
 *
 * <h2>Usage</h2>
 *
 * <p>Programmatic startup:</p>
 *
 * <pre>
 * {@code
 * public final class Main {
 *   public static void main(String[] args) {
 *     org.machanism.machai.cli.MachaiCLI.main(args);
 *   }
 * }
 * }
 * </pre>
 *
 * <p>Typical interactive commands (examples):</p>
 *
 * <pre>
 * {@code
 * # pick libraries for a requirement description
 * pick "Create a web app"
 *
 * # assemble a project into a directory
 * assembly --dir /path/to/out --genai OpenAI:gpt-5.1
 *
 * # generate/register bindex metadata and process sources/documents
 * bindex --dir /path/to/project
 * process --scan /path/to/project/src/main/java --genai OpenAI:gpt-5.1
 *
 * # clean temporary output
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
