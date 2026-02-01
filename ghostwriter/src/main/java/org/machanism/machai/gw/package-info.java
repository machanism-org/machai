/**
 * Command-line tooling for scanning a workspace (single-module or multi-module) and preparing inputs for
 * GenAI-assisted code review and code generation workflows.
 *
 * <p>This package contains the main CLI entry point ({@link org.machanism.machai.gw.Ghostwriter}) and orchestration
 * components (for example, {@link org.machanism.machai.gw.FileProcessor}) that:
 *
 * <ul>
 *   <li>discover file-type-specific {@link org.machanism.machai.gw.reviewer.Reviewer} implementations (typically via
 *       {@link java.util.ServiceLoader});</li>
 *   <li>traverse one or more modules and select candidate files for processing;</li>
 *   <li>extract and merge guidance with bundled templates and optional user instructions; and</li>
 *   <li>build prompt inputs which can be persisted for inspection or sent to a configured
 *       {@link org.machanism.machai.ai.manager.GenAIProvider}.</li>
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