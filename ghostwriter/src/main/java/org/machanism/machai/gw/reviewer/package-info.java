/**
 * Defines the {@link org.machanism.machai.gw.reviewer.Reviewer} service-provider interface (SPI) and built-in
 * implementations used by Ghostwriter to extract {@code @guidance} instructions from source and documentation files.
 *
 * <p>A {@link org.machanism.machai.gw.reviewer.Reviewer} is responsible for understanding the comment conventions of a
 * specific file format, locating guidance blocks, and producing a normalized prompt fragment (including path context)
 * for downstream processing.
 *
 * <h2>Included reviewers</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.gw.reviewer.JavaReviewer} - Java sources (including {@code package-info.java})</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.MarkdownReviewer} - Markdown files</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.HtmlReviewer} - HTML/XML files</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.PythonReviewer} - Python sources</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TypeScriptReviewer} - TypeScript sources</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TextReviewer} - directory-scoped {@code @guidance.txt} files</li>
 * </ul>
 *
 * <h2>Typical use</h2>
 *
 * <p>Reviewers are typically selected by file extension and invoked by the Ghostwriter pipeline.
 *
 * <pre>{@code
 * Reviewer reviewer = new JavaReviewer();
 * String promptFragment = reviewer.perform(projectDir, file);
 * if (promptFragment != null) {
 *     // Feed into downstream processing.
 * }
 * }</pre>
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
 *          and `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc. 
 */
