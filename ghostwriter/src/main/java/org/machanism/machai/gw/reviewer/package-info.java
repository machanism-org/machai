/**
 * Provides reviewer interfaces and concrete implementations for processing source and documentation files
 * in various formats, including Java, Python, TypeScript, HTML, Markdown, and text files. Reviewers extract,
 * analyze, and transform guidance information annotated in project files (such as comments, directives, or
 * special guidance files) for use in automated documentation workflows.
 * <p>
 * Key classes in this package:
 * <ul>
 *   <li>{@link Reviewer}: The base interface for file reviewers supporting guidance extraction from project files.</li>
 *   <li>{@link JavaReviewer}: Processes <code>.java</code> files and extracts guidance from annotated comments, including <code>package-info.java</code>.</li>
 *   <li>{@link HtmlReviewer}: Extracts guidance blocks from <code>.html</code>, <code>.htm</code>, and <code>.xml</code> files.</li>
 *   <li>{@link MarkdownReviewer}: Supports <code>.md</code> files and retrieves documented guidance from Markdown content.</li>
 *   <li>{@link PythonReviewer}: Extracts guidance annotations from <code>.py</code> files using Python comment conventions.</li>
 *   <li>{@link TypeScriptReviewer}: Processes <code>.ts</code> files and extracts documentation guidance from TypeScript sources.</li>
 *   <li>{@link TextReviewer}: Handles generic <code>.txt</code> files, including propagation context for documentation.</li>
 * </ul>
 * <p>
 * Reviewers utilize project structure and annotation tags to deliver accurate, context-propagated documentation fragments.
 * Implementations support extensibility for additional file formats.
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * Reviewer reviewer = new JavaReviewer();
 * String guidance = reviewer.perform(projectRoot, file);
 * if (guidance != null) {
 *     // Use the extracted guidance for documentation
 * }
 * </pre>
 * <p>
 * For details on supported formats and conventions,
 * refer to individual reviewer class documentation.
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
 *      - Generate comprehensive package-level Javadoc that clearly describes the package�s overall purpose and usage.
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
 * -  Update Javadoc with Code Changes:
 * 		- Revise Javadoc comments whenever code is modified to ensure documentation remains accurate and up to date.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */