/**
 * Ghostwriter command-line tooling and document-scanning orchestration.
 *
 * <p>This package provides the command-line entry point and the core orchestration used to scan a
 * project workspace, discover and process documents and source files, extract embedded guidance
 * directives (as plain source comments), and assemble prompt inputs for a configured
 * {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <p>Typical responsibilities include:
 * <ul>
 *   <li>Walking project directories while honoring configured exclusions and boundaries.</li>
 *   <li>Delegating file-type-specific parsing to reviewer implementations.</li>
 *   <li>Aggregating extracted guidance (optionally inheriting from parent directories) and producing
 *       prompt input artifacts for use with an AI provider.</li>
 *   <li>Persisting constructed prompt inputs to a stable location (for example under
 *       {@code .machai/docs-inputs}) to support auditability and repeatable runs.</li>
 * </ul>
 *
 * <p>Key classes include:
 * <ul>
 *   <li>{@link org.machanism.machai.gw.Ghostwriter} — CLI entry point that parses arguments,
 *       resolves a provider, and starts scanning.</li>
 *   <li>{@link org.machanism.machai.gw.FileProcessor} — directory walker and processing orchestrator
 *       that delegates to reviewer implementations and aggregates prompt context.</li>
 * </ul>
 *
 * <p>Examples:
 * <pre>{@code
 * // Scan the current working directory from the CLI.
 * Ghostwriter.main(new String[] {});
 *
 * // Programmatic usage.
 * GenAIProvider provider = GenAIProviderManager.getProvider(null);
 * FileProcessor processor = new FileProcessor(provider);
 * processor.setUseParentsGuidances(true);
 * processor.scanDocuments(new File("/path/to/project/root"));
 * }</pre>
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 */
package org.machanism.machai.gw;

/*-
 * @guidance:
 *
 * **IMPORTANT: ADD JAVADOC TO ALL CLASSES IN THE PACKAGE AND THIS `package-info.java`!**
 * 
 * - Use Clear and Concise Descriptions:
 * 		- Write meaningful summaries that explain the purpose, behavior, and usage of each element.
 * 		- Avoid vague statements; be specific about functionality and intent.
 * 
 * - Update `package-info.java`:
 *      - Analyze the source code within this package.
 *      - Generate comprehensive package-level Javadoc that clearly describes the packages overall purpose and usage.
 *      - Do not include a "Guidance and Best Practices" section in the `package-info.java` file.
 *      - Ensure the package-level Javadoc is placed immediately before the `package` declaration.
 *      
 * -  Include Usage Examples Where Helpful:
 * 		- Provide code snippets or examples in Javadoc comments for complex classes or methods.
 * 
 * -  Maintain Consistency and Formatting:
 * 		- Follow a consistent style and structure for all Javadoc comments.
 * 		- Use proper Markdown or HTML formatting for readability.
 * 
 * - Add Javadoc:
 *     - Review the Java class source code and include comprehensive Javadoc comments for all classes, 
 *          methods, and fields, adhering to established best practices.
 *     - Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values,
 *          and any exceptions thrown.
 * 
 * -  Escape `<` and `>` as `&lt;` and `&gt;` in `<pre>` content for Javadoc.
 */
