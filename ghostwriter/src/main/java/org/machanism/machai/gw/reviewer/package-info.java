/**
 * Provides file reviewer implementations for various source formats including Java, Python, TypeScript, HTML, Markdown, and plain text.
 * <p>
 * Reviewers in this package are responsible for analyzing project files of supported types and extracting custom documentation guidance.
 * This guidance, typically annotated with the <code>@guidance</code> tag in comments, is used to automate documentation workflows and improve code quality.
 * <p>
 * Common reviewer logic includes reading files, parsing for annotated documentation fragments, and formatting outputs for further processing.
 * <p>
 * Supported reviewers:
 * <ul>
 *   <li>{@link org.machanism.machai.gw.reviewer.JavaReviewer}: Reviews Java source and package-info files.</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.PythonReviewer}: Reviews Python files.</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TypeScriptReviewer}: Reviews TypeScript files.</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.HtmlReviewer}: Reviews HTML and XML files.</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.MarkdownReviewer}: Reviews Markdown files.</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TextReviewer}: Reviews plain text files for guidance propagation.</li>
 * </ul>
 * Reviewers implement the {@link org.machanism.machai.gw.reviewer.Reviewer} interface for standardized access and automation.
 * <p>
 * When adding or updating code in this package, ensure every public or protected class, interface, method, and field has a clear, comprehensive Javadoc.
 * See @guidance for detailed documentation standards.
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 *   Reviewer reviewer = new JavaReviewer();
 *   String guidance = reviewer.perform(new File("/project/root"), new File("src/Example.java"));
 *   if (guidance != null) {
 *       // use extracted guidance for documentation
 *   }
 * </pre>
 */
package org.machanism.machai.gw.reviewer;

/*
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND UNIT TESTS FOR THIS PACKAGE!**	
 *
 * 1. Document All Public and Protected Elements:
 *    - Ensure every public and protected class, interface, method, and field has a comprehensive Javadoc comment.
 *    - Include package-level Javadoc in a `package-info.java` file to describe the package’s purpose and usage.
 *
 * 2. Use Clear and Concise Descriptions:
 *    - Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 *    - Avoid vague statements; be specific about functionality and intent.
 *
 * 3. Describe Parameters, Return Values, and Exceptions:
 *    - Use `@param` tags to document all method parameters.
 *    - Use `@return` tags to describe return values.
 *    - Use `@throws` or `@exception` tags to explain when exceptions are thrown.
 *
 * 4. Include Usage Examples Where Helpful:
 *    - Provide code snippets or examples in Javadoc comments for complex classes or methods.
 *
 * 5. Maintain Consistency and Formatting:
 *    - Follow a consistent style and structure for all Javadoc comments.
 *    - Use proper Markdown or HTML formatting for readability.
 *
 * 6. Update Javadoc with Code Changes:
 *    - Revise Javadoc comments whenever code is modified to ensure documentation remains accurate and up to date.
 *
 * 7. Leverage Javadoc Tools:
 *    - Use IDE features or static analysis tools to check for missing or incomplete Javadoc.
 *    - Generate and review Javadoc HTML output regularly to verify quality and completeness.
 *
 * 8. Note: `@guidance` is not a Javadoc tag. Do not use it within Javadoc comments.
 *
 * 9. Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 *
 * 10. Use the Java version specified in `pom.xml` for code generation.
 *
 * Apply these practices to all Java code within the package to ensure high-quality, maintainable, and user-friendly documentation.
 */
