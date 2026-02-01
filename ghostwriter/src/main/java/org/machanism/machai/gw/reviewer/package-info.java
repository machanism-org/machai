/**
 * Defines the {@code org.machanism.machai.gw.reviewer} package, which contains the reviewer subsystem responsible
 * for locating and extracting {@code @guidance} instruction blocks from project files.
 *
 * <p>The central abstraction is {@link org.machanism.machai.gw.reviewer.Reviewer}. Reviewer implementations are
 * typically responsible for:
 *
 * <ul>
 *   <li>declaring which file extensions they support,</li>
 *   <li>parsing file contents according to the target format's comment conventions, and</li>
 *   <li>returning discovered {@code @guidance} blocks for downstream processing.</li>
 * </ul>
 *
 * <p>This package includes built-in {@code Reviewer} implementations for common formats such as Java source files
 * (including {@code package-info.java}), TypeScript, Python, Markdown, HTML/XML, and plain-text
 * {@code @guidance.txt} files.
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
