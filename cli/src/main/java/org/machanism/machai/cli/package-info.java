/**
 * Spring Shell-based command-line interface (CLI) for Machai.
 *
 * <p>This package contains the application entry point and the Spring Shell command components that expose Machai
 * capabilities via an interactive shell or direct invocation from scripts/build tooling.
 *
 * <h2>How to use</h2>
 * <p>Start the CLI by invoking the main class:</p>
 * <pre>
 * public final class Main {
 *   public static void main(String[] args) {
 *     org.machanism.machai.cli.MachaiCLI.main(args);
 *   }
 * }
 * </pre>
 *
 * <p>Within the shell, commands are provided by Spring Shell components in this package (for example, assembly, bindex,
 * configuration, and cleanup commands). Commands can also be executed non-interactively by passing arguments to the
 * entry point.</p>
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
