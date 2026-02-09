/**
 * Spring Shell-based command-line interface (CLI) for Machai.
 *
 * <p>This package contains the Spring Boot entry point and Spring Shell command components that expose Machai
 * features through an interactive REPL and non-interactive command execution.
 *
 * <h2>Key types</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.cli.MachaiCLI} – application entry point that boots Spring Boot and loads
 *       optional system properties from {@code machai.properties} (or from the file provided via the
 *       {@code -Dconfig=...} system property).</li>
 *   <li>{@link org.machanism.machai.cli.ConfigCommand} – manages persistent CLI defaults such as working directory
 *       ({@code dir}), GenAI provider/model ({@code genai}), and semantic-search score threshold ({@code score}).</li>
 *   <li>{@link org.machanism.machai.cli.AssemblyCommand} – performs library picking (semantic search) and assembles an
 *       application skeleton using a configured GenAI provider.</li>
 *   <li>{@link org.machanism.machai.cli.BindexCommand} – generates bindex metadata for projects and registers bindex
 *       files in an external registry.</li>
 *   <li>{@link org.machanism.machai.cli.GWCommand} – processes documents/files using the Ghostwriter pipeline.</li>
 *   <li>{@link org.machanism.machai.cli.CleanCommand} – deletes Machai temporary directories (for example
 *       {@code .machai}) under a selected root directory.</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 *
 * <pre>
 * config genai OpenAI:gpt-5.1
 * config dir .\\my-project
 * bindex --dir .\\my-project
 * pick --query "Create a web app" --score 0.9
 * assembly --dir .\\out
 * gw --dir .\\my-project --scan .\\my-project\\docs
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
