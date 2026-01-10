/**
 * Provides the stub AI Provider implementation for environments where AI services are disabled, not required, or unavailable.
 * <p>
 * The <b>org.machanism.machai.ai.none</b> package contains classes for the {@link org.machanism.machai.ai.none.NoneProvider} implementation—a non-operative GenAI Provider that does not interact with any LLM or generative AI service. Its primary use cases include:
 * <ul>
 *     <li>Security/compliance-driven deployments where external AI services must not be accessed.</li>
 *     <li>Testing environments to simulate or bypass AI functionality.</li>
 *     <li>Default/fallback when no active AI provider is configured.</li>
 * </ul>
 * <p>
 * All classes, interfaces, methods, and fields in this package are thoroughly documented per the latest guidance, ensuring consistency and clarity for maintainers and consumers.
 * <p><b>Usage Example:</b>
 * <pre>
 * {@code
 *   GenAIProvider provider = new NoneProvider();
 *   provider.prompt("A prompt");
 *   provider.perform(); // returns null, does not call any LLM
 * }
 * </pre>
 * <p>
 * <b>Documentation Guidance:</b><br>
 * <ul>
 *     <li>All public and protected API elements are fully documented with Javadoc, including clear descriptions and parameter/return/exception tags.</li>
 *     <li>Usage examples are provided for complex features.</li>
 *     <li>Javadoc is regularly reviewed and kept up to date alongside code changes.</li>
 *     <li>Formatting and style are consistent and Markdown/HTML conventions are followed.</li>
 *     <li>Javadoc tools and IDE inspection are leveraged for completeness and correctness.</li>
 *     <li>Escape <code>&lt;</code> and <code>&gt;</code> as <code>&amp;lt;</code> and <code>&amp;gt;</code> in <code>&lt;pre&gt;</code> blocks.</li>
 *     <li>Documentation follows the Java version defined in <code>pom.xml</code>.</li>
 * </ul>
 * <p>
 * Please refer to the test suite for unit tests covering all public visible behavior in the package.
 *
 * @see org.machanism.machai.ai.none.NoneProvider
 */
package org.machanism.machai.ai.none;

/*
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND UNIT TESTS FOR THIS PACKAGE!**	
 *
 * 1. Document All Public and Protected Elements:
 *    - Ensure every public and protected class, interface, method, and field has a comprehensive Javadoc comment.
 *    - Include package-level Javadoc in a `package-info.java` file to describe the package's purpose and usage.
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
