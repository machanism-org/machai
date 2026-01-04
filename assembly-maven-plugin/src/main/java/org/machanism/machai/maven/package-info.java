/**
 * The <code>org.machanism.machai.maven</code> package provides Maven plugin components enabling
 * AI-powered project assembly and automated library recommendations. Its core Mojo interacts
 * with users and AI providers to generate project structures and dependency sets based on user prompts
 * or specification files (e.g., <code>project.txt</code>), integrating advanced automation and interactivity.
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Interactive prompts and AI-driven project assembly.</li>
 *   <li>Automated recommendation of Java libraries via AI chat models.</li>
 *   <li>Flexible configuration using parameters such as chat models, score thresholds, and prompt files.</li>
 *   <li>Consistent and extensible architecture for custom project assembly workflows.</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * mvn org.machanism.machai.maven:assembly -Dassembly.inputs.only=false
 * </pre>
 * <p>
 * For advanced users, customize the chat models and scoring parameters to leverage different AI providers
 * or control the recommendation quality.
 *
 *
 * <h2>Best Practices</h2>
 * <ul>
 *   <li>Document all public and protected elements with comprehensive Javadoc comments.</li>
 *   <li>Use meaningful summaries and detailed descriptions for all classes, methods, and fields.</li>
 *   <li>Update documentation with every code change to maintain accuracy.</li>
 *   <li>Review Javadoc output regularly for completeness and quality.</li>
 *   <li>Never remove <code>@guidance</code> comments; preserve them for project standards compliance.</li>
 * </ul>
 *
 * @see org.machanism.machai.maven.Assembly
 *
 * @guidance
 */
package org.machanism.machai.maven;

/*
 * @guidance: 
 * 
 * 1. **Document All Public and Protected Elements:** - Ensure every public and
 * protected class, interface, method, and field has a comprehensive Javadoc
 * comment. - Include package-level Javadoc in a `package-info.java` file to
 * describe the package’s purpose and usage.
 * 
 * 2. **Use Clear and Concise Descriptions:** - Write meaningful summaries that
 * explain the purpose, behavior, and usage of each element. - Avoid vague
 * statements; be specific about functionality and intent.
 * 
 * 3. **Describe Parameters, Return Values, and Exceptions:** - Use `@param`
 * tags to document all method parameters. - Use `@return` tags to describe
 * return values. - Use `@throws` or `@exception` tags to explain when
 * exceptions are thrown.
 * 
 * 4. **Include Usage Examples Where Helpful:** - Provide code snippets or
 * examples in Javadoc comments for complex classes or methods.
 * 
 * 5. **Maintain Consistency and Formatting:** - Follow a consistent style and
 * structure for all Javadoc comments. - Use proper Markdown or HTML formatting
 * for readability.
 * 
 * 6. **Update Javadoc with Code Changes:** - Revise Javadoc comments whenever
 * code is modified to ensure documentation remains accurate and up to date.
 * 
 * 7. **Leverage Javadoc Tools:** - Use IDE features or static analysis tools to
 * check for missing or incomplete Javadoc. - Generate and review Javadoc HTML
 * output regularly to verify quality and completeness.
 * 
 * Apply these practices to all Java code within the package to ensure
 * high-quality, maintainable, and user-friendly documentation.
 */