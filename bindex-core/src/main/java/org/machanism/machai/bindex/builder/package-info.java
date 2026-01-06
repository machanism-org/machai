/**
 * Provides builder implementations for generating BIndex documents in support of semantic, AI-powered indexing operations.
 * <p>
 * This package includes specialized builders for Maven, Python, JScript/TypeScript/Vue, and generic project types,
 * encapsulating logic for manifest extraction, source aggregation, rules composition, and AI context construction.
 * <p>
 * Common builder features:
 * <ul>
 *     <li>Integration with GenAIProvider for prompt-based index generation</li>
 *     <li>Reading project manifests (pom.xml, pyproject.toml, package.json) and source files</li>
 *     <li>Builder factory handling for different layouts</li>
 *     <li>Extensible base builder (BindexBuilder) for custom logic</li>
 * </ul>
 * <b>Usage Example:</b>
 * <pre>
 *     MavenBindexBuilder builder = new MavenBindexBuilder(layout);
 *     builder.genAIProvider(provider);
 *     BIndex bindex = builder.build();
 * </pre>
 *
 * Important Guidance:
 * <ul>
 *   <li>All public/protected classes, methods, and fields must be fully documented with comprehensive Javadoc.</li>
 *   <li>Describe parameters, return values, and exceptions for methods.</li>
 *   <li>Maintain consistent formatting and update Javadoc as code evolves.</li>
 * </ul>
 *
 * @author Viktor Tovstyi
 */
package org.machanism.machai.bindex.builder;

/*
 * @guidance: 
 * 
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND UNIT TESTS FOR THIS PACKAGE!**	
 *
 * 1. **Document All Public and Protected Elements:** - Ensure every public and
 * protected class, interface, method, and field has a comprehensive Javadoc
 * comment. 
 * 
 * 2. **Ensure `package-info.java` contains all required description** - It should include 
 * package-level Javadoc in a `package-info.java` file to describe the package’s purpose and usage.
 * 
 * 3. **Use Clear and Concise Descriptions:** - Write meaningful summaries that
 * explain the purpose, behavior, and usage of each element. - Avoid vague
 * statements; be specific about functionality and intent.
 * 
 * 4. **Describe Parameters, Return Values, and Exceptions:** - Use `@param`
 * tags to document all method parameters. - Use `@return` tags to describe
 * return values. - Use `@throws` or `@exception` tags to explain when
 * exceptions are thrown.
 * 
 * 5. **Include Usage Examples Where Helpful:** - Provide code snippets or
 * examples in Javadoc comments for complex classes or methods.
 * 
 * 6. **Maintain Consistency and Formatting:** - Follow a consistent style and
 * structure for all Javadoc comments. - Use proper Markdown or HTML formatting
 * for readability.
 * 
 * 7. **Update Javadoc with Code Changes:** - Revise Javadoc comments whenever
 * code is modified to ensure documentation remains accurate and up to date.
 * 
 * 8. **Leverage Javadoc Tools:** - Use IDE features or static analysis tools to
 * check for missing or incomplete Javadoc. - Generate and review Javadoc HTML
 * output regularly to verify quality and completeness.
 * 
 * Apply these practices to all Java code within the package to ensure
 * high-quality, maintainable, and user-friendly documentation.
 */
