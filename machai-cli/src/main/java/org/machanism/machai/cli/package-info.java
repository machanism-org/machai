/**
 * Command-line interface (CLI) layer for Machai.
 *
 * <p>
 * This package contains Spring Shell commands that expose Machai capabilities as
 * interactive CLI operations. Commands typically:
 *
 * <ul>
 * <li>Read defaults from the persisted configuration managed by {@link org.machanism.machai.cli.ConfigCommand}.</li>
 * <li>Resolve the working/project directory and related options.</li>
 * <li>Delegate the heavy lifting to the underlying Machai modules (Ghostwriter,
 * bindex/picker, and project assembly).</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 * gw --scanDir .\\my-project --model OpenAI:gpt-5.1
 * act commit "and push"
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
