/**
 * Command-line entry point and processing pipeline for Ghostwriter document scanning.
 *
 * <p>This package contains the CLI entry point and the orchestration code that scans a project directory,
 * selects eligible files, extracts {@code @guidance:} directives, and prepares prompt context for a
 * configured {@link org.machanism.machai.ai.manager.GenAIProvider}.
 *
 * <h2>Key components</h2>
 * <ul>
 *   <li>{@link org.machanism.machai.gw.Ghostwriter} &mdash; CLI entry point that parses arguments, resolves
 *       the working directory and provider, and triggers the scan.</li>
 *   <li>{@link org.machanism.machai.gw.FileProcessor} &mdash; scanning/processing orchestrator that walks the
 *       directory tree, delegates file selection and content extraction to pluggable reviewers, aggregates
 *       guidance, and writes prompt inputs.</li>
 * </ul>
 *
 * <h2>Scanning behavior</h2>
 * <ul>
 *   <li>Directories and files are traversed recursively while honoring exclusions and project boundaries.</li>
 *   <li>Supported file types are handled by reviewer implementations that extract guidance and build prompt
 *       content.</li>
 *   <li>Guidance can be optionally inherited from parent directories to provide additional context.</li>
 *   <li>Prompt input logs are written under {@code .machai/docs-inputs} in the scanned project directory.</li>
 * </ul>
 *
 * <h2>Programmatic usage</h2>
 * <pre>{@code
 * // Default: scan the current user directory.
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
