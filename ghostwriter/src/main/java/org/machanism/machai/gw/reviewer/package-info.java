/**
 * File reviewers for extracting and normalizing documentation guidance from project sources.
 *
 * <p>
 * Reviewers parse supported file types (for example Java, Markdown, HTML/XML, TypeScript, Python, and plain
 * text) and return a normalized guidance representation suitable for downstream documentation and automation
 * workflows.
 *
 * <p>
 * Typical reviewer responsibilities include:
 * <ul>
 *   <li>Identifying guidance blocks using each language's comment and documentation conventions.</li>
 *   <li>Extracting and normalizing guidance into a consistent textual form.</li>
 *   <li>Optionally incorporating directory-level or project-level context when available.</li>
 * </ul>
 *
 * <p>
 * Callers typically select an appropriate {@link org.machanism.machai.gw.reviewer.Reviewer} based on file
 * extension and invoke
 * {@link org.machanism.machai.gw.reviewer.Reviewer#perform(java.io.File, java.io.File)}.
 *
 * <pre>
 * Reviewer reviewer = new JavaReviewer();
 * String guidance = reviewer.perform(projectRoot, sourceFile);
 * if (guidance != null) {
 *     // Use the extracted guidance for documentation.
 * }
 * </pre>
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
 *      - Generate comprehensive package-level Javadoc that clearly describes the packages overall purpose and usage.
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
