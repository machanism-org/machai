/**
 * Command-line interface (CLI) layer for Machai.
 *
 * <p>
 * This package provides the Spring Shell commands and application bootstrap used
 * to run Machai workflows from an interactive terminal. Commands typically read
 * defaults from {@code machai.properties} via {@link org.machanism.macha.core.commons.configurator.PropertiesConfigurator}
 * (see {@link org.machanism.machai.cli.ConfigCommand}) and delegate the heavy
 * lifting to modules such as Ghostwriter guidance processing and bindex-based
 * library picking/assembly.
 *
 * <h2>What is included</h2>
 * <ul>
 *   <li>Application bootstrap: {@link org.machanism.machai.cli.MachaiCLI}</li>
 *   <li>Ghostwriter pipeline execution: {@link org.machanism.machai.cli.GWCommand} and {@link org.machanism.machai.cli.ActCommand}</li>
 *   <li>bindex operations: {@link org.machanism.machai.cli.BindexCommand} and {@link org.machanism.machai.cli.AssembyCommand}</li>
 *   <li>Local workspace cleanup: {@link org.machanism.machai.cli.CleanCommand}</li>
 *   <li>Persistent CLI configuration: {@link org.machanism.machai.cli.ConfigCommand}</li>
 * </ul>
 *
 * <h2>Typical usage</h2>
 * <pre>
 * config set --key gw.model --value OpenAI:gpt-5.1
 * gw --scanDir .\\my-project --excludes target,.git
 * act commit "and push"
 * bindex --dir .\\my-project
 * pick --query "Create a web app" --score 0.8
 * assembly --dir .\\out
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
