/**
 * Command-line tooling for scanning a project workspace and preparing inputs for GenAI-assisted code review and code
 * generation workflows.
 *
 * <p>
 * This package provides the CLI entry point ({@link org.machanism.machai.gw.Ghostwriter}) and the core orchestration
 * ({@link org.machanism.machai.gw.FileProcessor}) used to:
 * </p>
 *
 * <ul>
 *   <li>Discover file-type-specific {@link org.machanism.machai.gw.reviewer.Reviewer} implementations (typically via
 *       {@link java.util.ServiceLoader}).</li>
 *   <li>Traverse a single-module or multi-module workspace to select candidate files and directories.</li>
 *   <li>Extract per-file guidance blocks and merge them with bundled templates and optional user instructions.</li>
 *   <li>Build provider prompts and, optionally, persist the full prompt inputs for inspection.</li>
 * </ul>
 */
package org.machanism.machai.gw;

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
 * 		- Use proper Markdown or HTML formatting for readability.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */