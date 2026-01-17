/**
 * Provides the {@link org.machanism.machai.gw.reviewer.Reviewer} service-provider interface (SPI) and built-in
 * implementations used by the Ghostwriter pipeline to locate and extract {@code @guidance} instructions from
 * project files.
 *
 * <p>The core abstraction in this package is {@link org.machanism.machai.gw.reviewer.Reviewer}. A reviewer
 * determines whether it can process a file based on its type and comment conventions, then extracts any guidance
 * payload and returns a normalized, prompt-ready {@link String} that includes path context.
 *
 * <p>Concrete reviewers in this package support common source and documentation formats (for example Java,
 * TypeScript, Python, HTML, Markdown, and plain text) so that guidance can be discovered consistently across a
 * repository.
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
