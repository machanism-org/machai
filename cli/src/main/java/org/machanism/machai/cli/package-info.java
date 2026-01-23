/**
 * Spring Shell-based command-line interface (CLI) for Machai.
 *
 * <p>This package contains Spring Shell command components (typically annotated with
 * {@code @org.springframework.shell.standard.ShellComponent}) that expose Machai functionality through an
 * interactive shell and non-interactive invocation.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Translate user input (command options/arguments) into calls to Machai services.</li>
 *   <li>Coordinate GenAI provider configuration and execution for project-centric workflows.</li>
 *   <li>Support tasks such as library selection, project assembly, metadata indexing, and document/source
 *       processing.</li>
 * </ul>
 *
 * <h2>Entry point</h2>
 * <p>The application entry point is {@link org.machanism.machai.cli.MachaiCLI}.</p>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * // Interactive shell (examples)
 * pick --query "Create a web app" --score 0.9
 * assembly --dir /path/to/out --genai OpenAI:gpt-5.1
 * bindex --dir /path/to/project --update false
 * gw --dir /path/to/project --scan /path/to/project/src/main/java --genai OpenAI:gpt-5.1
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
 
 */
