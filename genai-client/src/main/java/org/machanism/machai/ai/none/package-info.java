/**
 * Provides classes and interfaces representing a non-operational AI Provider implementation.
 * <p>
 * This package contains the {@link NoneProvider} class, which is a stub implementation of the {@link org.machanism.machai.ai.manager.GenAIProvider} interface
 * and does not connect to or operate any Large Language Model (LLM) or AI service. {@code NoneProvider} is intended to be used as a placeholder
 * or for scenarios where AI services are intentionally disabled.
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * {@code
 *   GenAIProvider provider = new NoneProvider();
 *   provider.prompt("A prompt");
 *   provider.perform(); // returns null, does not call any LLM
 * }
 * </pre>
 * <p>
 * All classes, interfaces, methods, and fields in this package should be thoroughly documented following the project guidance. This ensures clarity for users and maintainers.
 *
 * @see org.machanism.machai.ai.manager.GenAIProvider
 */
package org.machanism.machai.ai.none;

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
