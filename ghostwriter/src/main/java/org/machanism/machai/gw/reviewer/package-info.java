/**
 * Provides the reviewer subsystem for discovering and extracting {@code @guidance} instruction blocks from project
 * files.
 *
 * <p>The primary entry point is {@link org.machanism.machai.gw.reviewer.Reviewer}. Implementations typically:
 *
 * <ul>
 *   <li>declare the file extensions they support via {@link org.machanism.machai.gw.reviewer.Reviewer#getSupportedFileExtensions()},</li>
 *   <li>scan file contents using the target format's comment rules to locate {@code @guidance} markers, and</li>
 *   <li>return a formatted prompt fragment (including path context and, where applicable, file content) for downstream
 *       processing.</li>
 * </ul>
 *
 * <p>This package contains built-in {@code Reviewer} implementations for multiple common formats, including:
 *
 * <ul>
 *   <li>Java source files (including {@code package-info.java}),</li>
 *   <li>TypeScript ({@code .ts}),</li>
 *   <li>Python ({@code .py}),</li>
 *   <li>Markdown ({@code .md}),</li>
 *   <li>HTML/XML ({@code .html}, {@code .htm}, {@code .xml}), and</li>
 *   <li>plain-text guidance files ({@code @guidance.txt}).</li>
 * </ul>
 */
package org.machanism.machai.gw.reviewer;

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
 *      - Generate comprehensive package-level Javadoc that clearly describes the packages overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 *     - When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` 
 *          and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
