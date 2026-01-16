/**
 * File reviewers that extract and normalize guidance content from project files.
 *
 * <p>This package provides {@link org.machanism.machai.gw.reviewer.Reviewer} implementations for a variety of
 * file formats (for example Java, Markdown, HTML/XML, TypeScript, Python, and plain text).
 *
 * <p>A reviewer is responsible for scanning an input file for language-appropriate {@code @guidance:} markers and
 * returning a normalized text payload. The normalized output can then be aggregated and consumed by downstream
 * automation.
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * Reviewer reviewer = ...; // chosen based on file extension
 * String extracted = reviewer.perform(projectDir, file);
 * if (extracted != null) {
 *     // consume the normalized guidance
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
