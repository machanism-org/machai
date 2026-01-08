/**
 * Provides classes for document processing and code review automation in projects.
 * <p>
 * This package contains the main classes and utilities to scan project directories,
 * orchestrate document preparation, and interact with AI-powered reviewing tools. Core classes
 * include {@link FileProcessor} for documentation input generation and {@link Ghostwriter}
 * for initializing project-wide document review operations.
 * <br/><br/>
 * General usage involves creating an instance of {@link FileProcessor}, optionally configuring guidance,
 * and running scans against source directories to collect, analyze, and process documentation-related files.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   GenAIProvider provider = GenAIProviderManager.getProvider(null);
 *   DocsProcessor docsProcessor = new DocsProcessor(provider);
 *   docsProcessor.scanDocuments(new File("/path/to/project"));
 * }
 * </pre>
 *
 * For further configuration, see the individual Javadocs of each class.
 *
 * @author Machanism Team
 * @since 0.0.2
 */
package org.machanism.machai.gw;

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