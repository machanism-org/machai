/**
 * Provides the reviewer subsystem used by the Ghostwriter pipeline to discover and collect
 * author instructions embedded in project files.
 *
 * <p>The central contract is {@link org.machanism.machai.gw.reviewer.Reviewer}, a service-provider
 * interface (SPI) implemented by format-specific reviewers (for example Java, Markdown, HTML,
 * Python, and TypeScript). Each implementation declares the file extensions it supports and can
 * scan a file to detect occurrences of {@code @guidance} and return a normalized prompt fragment
 * that includes the file's related path for downstream processing.
 *
 * <p>This package also includes a reviewer for dedicated guidance files (for example
 * {@code @guidance.txt}) and helpers used by the pipeline to interpret guidance embedded in
 * different comment syntaxes.
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
