/**
 * Provides CLI (Command Line Interface) tools for interacting with GenAI-powered automation,
 * project assembly, bindex management, library selection, and document processing in Machai.
 *
 * <p>
 * Main features include:
 * <ul>
 *   <li>Project assembly and library picking using GenAI</li>
 *   <li>Bindex generation/registration</li>
 *   <li>Cleaning Machai temp directories</li>
 *   <li>GenAI document scanning, processing</li>
 *   <li>Bootstrapping the CLI shell</li>
 * </ul>
 *
 * <b>Documentation Standards & Best Practices</b>
 * <ul>
 *   <li>All public and protected classes, interfaces, methods, fields require comprehensive Javadoc.</li>
 *   <li>Describe parameters, return values, and exceptions clearly using <code>@param</code>, <code>@return</code>, <code>@throws</code> tags.</li>
 *   <li>Provide usage examples in <code>&lt;pre&gt;</code> blocks (escape &lt; and &gt;).</li>
 *   <li>Update Javadoc with code modifications.</li>
 *   <li>Maintain formatting and style consistency.</li>
 *   <li>Review Javadoc output with IDE/static tools for completeness.</li>
 *   <li>Use Java version 17 (see pom.xml).</li>
 * </ul>
 *
 * See individual class/test Javadocs for details.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
/* @guidance:
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
 * 8. Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 *
 * 9. Use the Java version specified in `pom.xml` for code generation.
 *
 * Apply these practices to all Java code within the package to ensure high-quality, maintainable, and user-friendly documentation.
 */
package org.machanism.machai.cli;
