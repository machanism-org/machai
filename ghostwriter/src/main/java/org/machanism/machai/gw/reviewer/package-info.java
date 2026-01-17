/**
 * Provides implementations of {@link org.machanism.machai.gw.reviewer.Reviewer} that detect
 * {@code @guidance} markers in project files and convert the relevant content into normalized, prompt-ready text.
 *
 * <p>Each {@link org.machanism.machai.gw.reviewer.Reviewer} is responsible for identifying guidance markers that are
 * appropriate for a specific file type (for example, HTML comments, Markdown comments, Java block and line comments,
 * and other language-specific conventions). When guidance is present, the reviewer returns a formatted {@link String}
 * that includes the file name/path context and the extracted guidance payload; otherwise it returns {@code null}.
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * Reviewer reviewer = new MarkdownReviewer();
 * String extracted = reviewer.perform(projectDir, file);
 * if (extracted != null) {
 *   // aggregate extracted guidance
 * }
 * }</pre>
 */
package org.machanism.machai.gw.reviewer;

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
 *      - Generate comprehensive package-level Javadoc that clearly describes the package’s overall purpose and usage.
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
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */
