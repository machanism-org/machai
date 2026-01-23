/**
 * Provides the {@link org.machanism.machai.gw.reviewer.Reviewer} service-provider interface (SPI) and
 * implementations that scan project files for {@code @guidance} directives.
 *
 * <p>Reviewers are typically selected by file extension and invoked as part of the Ghostwriter pipeline to
 * extract guidance embedded in source or documentation files.
 *
 * <h2>Supported formats</h2>
 *
 * <ul>
 *   <li>{@link org.machanism.machai.gw.reviewer.JavaReviewer} for Java sources (including {@code package-info.java})</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.MarkdownReviewer} for Markdown files</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.HtmlReviewer} for HTML/XML files</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TypeScriptReviewer} for TypeScript sources</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.PythonReviewer} for Python sources</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TextReviewer} for directory-level guidance files</li>
 * </ul>
 *
 * <h2>Typical use</h2>
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
 
 */
