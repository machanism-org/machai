/**
 * Package org.machanism.machai.gw.reviewer
 * <p>
 * Provides reviewer implementations for analyzing source files and extracting custom documentation guidance for automated workflows.
 * <p>
 * <b>Supported formats:</b> Java, Python, TypeScript, HTML/XML, Markdown, and plain text. The reviewers in this package parse files, locate @guidance documentation, and drive code documentation automation.
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 *   Reviewer reviewer = new JavaReviewer();
 *   String guidance = reviewer.perform(new File("/project/root"), new File("src/Example.java"));
 *   if (guidance != null) {
 *       // use extracted guidance for documentation
 *   }
 * </pre>
 * <p>
 * <b>Reviewer Implementations:</b> All reviewers implement the {@link org.machanism.machai.gw.reviewer.Reviewer} interface.
 * <ul>
 *   <li>{@link org.machanism.machai.gw.reviewer.JavaReviewer}</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.PythonReviewer}</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TypeScriptReviewer}</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.HtmlReviewer}</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.MarkdownReviewer}</li>
 *   <li>{@link org.machanism.machai.gw.reviewer.TextReviewer}</li>
 * </ul>
 * <p>
 * <b>Documentation Guidance:</b> See <b>@guidance</b> section below for documentation requirements and usage practices.
 * <p>
 * <b>Javadoc Practices:</b>
 * <ul>
 *   <li>Document every public/protected class, interface, method, and field using clear, comprehensive Javadoc.</li>
 *   <li>Write meaningful summaries and describe parameters, return values, exceptions.</li>
 *   <li>Include usage examples in comments where useful.</li>
 *   <li>Escape &lt; and &gt; as &amp;lt; and &amp;gt; in &lt;pre&gt; blocks.</li>
 *   <li>Follow the Java version specified in pom.xml.</li>
 *   <li>Update Javadoc with code changes and regularly review output.</li>
 * </ul>
 * <p>
 * <b>Package location:</b> <code>ghostwriter/src/main/java/org/machanism/machai/gw/reviewer</code>
 */
package org.machanism.machai.gw.reviewer;

/*
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND UNIT TESTS FOR THIS PACKAGE!**	
 *
 * 1. Document All Public and Protected Elements:
 *    - Ensure every public and protected class, interface, method, and field has a comprehensive Javadoc comment.
 *    - Include package-level Javadoc in a `package-info.java` file to describe the packages purpose and usage.
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
 * 8. Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 *
 * 9. Use the Java version specified in `pom.xml` for code generation.
 *
 * Apply these practices to all Java code within the package to ensure high-quality, maintainable, and user-friendly documentation.
 */
