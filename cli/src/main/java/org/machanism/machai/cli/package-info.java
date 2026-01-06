/**
 * Provides CLI (Command Line Interface) commands and tools for interacting with GenAI-powered automation,
 * bindex management, library picking, and document processing.
 * <p>
 * The classes in this package offer advanced shell commands enabling users and tools to interact with
 * various aspects of Machai applications, including project assembly, bindex creation/registration, and
 * document scanning using AI guidance. Shell commands follow Spring Shell conventions.
 * <p>
 * <b>Main commands:</b>
 * <ul>
 *   <li><b>AssembyCommand</b> - Pick libraries and assemble projects via GenAI.</li>
 *   <li><b>BindexCommand</b> - Generate and register bindex files for project libraries.</li>
 *   <li><b>DocsCommand</b> - Scan and process documents leveraging GenAI provider.</li>
 *   <li><b>MachaiCLI</b> - The main entry point (application bootstrap class).</li>
 * </ul>
 * <p>
 * <b>Best Practices:</b>
 * <ul>
 *   <li>All public/protected elements must include comprehensive Javadoc.</li>
 *   <li>Parameters, return values, and exceptions are documented.</li>
 *   <li>Usage examples are provided in Javadoc where relevant.</li>
 *   <li>Modifications to code should be reflected in the Javadoc immediately.</li>
 * </ul>
 * <p>
 * See individual class Javadocs for further details and usage instructions.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 *
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
package org.machanism.machai.cli;
