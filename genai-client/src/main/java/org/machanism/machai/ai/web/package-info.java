/**
 * Provides integration and extension points for AE workspace and prompt-driven workflows
 * in genai-client projects. This package contains core interfaces and implementations for
 * connecting to advanced generative AI engines, managing workspace sessions, running prompt-based
 * recipe workflows, and enabling automated project assembly and enhancement.
 *
 * <p>
 * Core features include:
 * <ul>
 *   <li>Dynamic integration with AE engines via prompt and recipe execution APIs</li>
 *   <li>Support for workspace setup, persistent context, and error handling</li>
 *   <li>Extensible runner classes to facilitate new workflows</li>
 * </ul>
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>
 *   WebProvider provider = new WebProvider();
 *   provider.model("default-config");
 *   provider.setWorkingDir(new File("/your/dir"));
 *   String result = provider.perform();
 * </pre>
 *
 * <p>
 * All public and protected classes, methods, and fields in this package must have comprehensive Javadoc.
 * Where relevant, usage examples should be included in the Javadoc to assist developers.
 * </p>
 *
 * <p>
 * For more information, see the class-level Javadoc of key types such as {@code WebProvider}.
 * </p>
 */
package org.machanism.machai.ai.web;

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
 * 8. Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 *
 * 9. Use the Java version specified in `pom.xml` for code generation.
 *
 * Apply these practices to all Java code within the package to ensure high-quality, maintainable, and user-friendly documentation.
 */
