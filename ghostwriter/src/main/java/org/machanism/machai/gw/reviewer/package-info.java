/**
 * Package org.machanism.machai.gw.reviewer.
 *
 * This package provides file reviewers that extract and normalize {@code @guidance} content from project sources.
 *
 * <p>
 * Implementations typically support one or more file formats (for example Java, Markdown, HTML/XML,
 * TypeScript, Python, and plain text), locate guidance blocks using the conventions of the target language,
 * and produce a normalized textual representation suitable for downstream automation.
 *
 * <p>
 * Callers generally select an appropriate {@link org.machanism.machai.gw.reviewer.Reviewer} based on file
 * type and invoke
 * {@link org.machanism.machai.gw.reviewer.Reviewer#perform(java.io.File, java.io.File)}.
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
